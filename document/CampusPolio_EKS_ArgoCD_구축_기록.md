# CampusPolio EKS · Argo CD 배포 환경 구축 기록

> 작성 기준: 2026-11-09까지의 채팅에서 확인된 설정과 작업 이력\
> 목적: EKS 환경을 정리하거나 추후 재구축할 때 참고할 수 있도록 인프라,
> GitOps, CI/CD, 인증 및 문제 해결 과정을 보존한다.\
> 주의: 대화에서 확인되지 않은 세부 설정은 추정하지 않고 `확인 필요`로
> 표시했다.

## 1. 구축 배경 및 목적

CampusPolio는 대학생 프로젝트를 아카이빙하고 포트폴리오로 관리하는
플랫폼이다. 기존에는 AWS EC2와 Docker Compose로 Spring Boot 백엔드를
배포했다.

Kubernetes 기반 컨테이너 오케스트레이션과 GitOps를 학습하기 위해 Amazon
EKS 환경을 별도로 구축했다. 주요 목표는 다음과 같다.

-   Amazon EKS 및 Managed Node Group 구성
-   Amazon ECR을 통한 Backend 이미지 저장
-   GitHub Actions를 통한 이미지 빌드 및 Push 자동화
-   Argo CD를 통한 GitOps 배포 자동화
-   AWS Load Balancer Controller와 Ingress를 통한 외부 HTTPS 연결
-   EKS Pod Identity를 통한 AWS IAM 권한 연결
-   기존 RDS MySQL 및 S3 연동
-   JWT 로그인·로그아웃·Refresh Token 갱신 검증

## 2. 전체 아키텍처

### 2.1 논리 구성도

``` mermaid
flowchart TB
    USER[사용자 브라우저]
    DNS[api.campuspolio.cloud]
    ALB[AWS Application Load Balancer]
    subgraph AWS["AWS ap-northeast-2"]
      subgraph VPC["CampusPolio VPC"]
        subgraph EKS["CampusPolio-eks"]
          ING[Ingress]
          SVC[Backend Service - ClusterIP]
          P1[Backend Pod 1]
          P2[Backend Pod 2]
          ARGO[Argo CD]
          LBC[AWS Load Balancer Controller]
        end
        RDS[(Amazon RDS MySQL)]
        S3[(Amazon S3)]
      end
      ECR[Amazon ECR]
    end
    GH[GitHub Repository]
    GHA[GitHub Actions]

    USER --> DNS --> ALB --> ING --> SVC
    SVC --> P1
    SVC --> P2
    P1 --> RDS
    P2 --> RDS
    P1 --> S3
    P2 --> S3
    GH --> GHA --> ECR
    GHA --> GH
    ARGO --> GH
    ARGO --> EKS
    LBC --> ALB
```

위 그림은 구성요소의 역할을 나타내는 논리도다. 실제 라우팅·서브넷
연결·보안 그룹 규칙 전체를 재현한 네트워크 다이어그램은 아니다.

### 2.2 구성요소 역할

  구성요소                       역할
  ------------------------------ -----------------------------------------------
  Amazon EKS                     Kubernetes Control Plane 관리
  Managed Node Group             Backend Pod 등 워크로드 실행
  Amazon ECR                     Backend 컨테이너 이미지 저장
  GitHub Actions                 Gradle 빌드, Docker 이미지 생성 및 ECR Push
  Argo CD                        Git 선언 상태를 Kubernetes 클러스터에 동기화
  AWS Load Balancer Controller   Kubernetes Ingress와 AWS ALB 연동
  ALB                            외부 HTTPS 트래픽 수신 및 Backend 전달
  Amazon RDS MySQL               CampusPolio 영속 데이터 저장
  Amazon S3                      파일·이미지 등 객체 저장
  EKS Pod Identity               Kubernetes ServiceAccount와 AWS IAM Role 연결

## 3. AWS 및 EKS 인프라

### 3.1 기본 정보

  항목                 채팅에서 확인된 값
  -------------------- -------------------------
  AWS 리전             `ap-northeast-2`
  AWS 계정 ID          `034317881539`
  VPC 이름             `CampusPolio-vpc`
  VPC CIDR             `10.0.0.0/16`
  EKS 클러스터         `CampusPolio-eks`
  Managed Node Group   `CampusPolio-nodegroup`
  인스턴스 유형        `t3.medium`
  노드 AMI             Amazon Linux 2023
  노드 디스크          20 GiB
  노드 배치            Private Subnet 2개

**현재 상태 관련 중요 기록:** 2026-11-03 대화에서 Node Group 설정은
`min=0`, `max=2`, `desired=0`으로 확인됐다. 과거 실습 중 2개 노드를
사용했던 이력과 현재 Desired Capacity 0 상태를 구분해야 한다. Node
Group은 존재하지만 현재 Worker Node가 실행 중이지 않을 수 있다.

### 3.2 서브넷

  구분             Subnet ID                    가용 영역
  ---------------- ---------------------------- -------------------
  Public Subnet    `subnet-085e7036d9a43f890`   `ap-northeast-2a`
  Private Subnet   `subnet-0e672fce8bdbe47ef`   `ap-northeast-2a`
  Private Subnet   `subnet-029774a8241c28de8`   `ap-northeast-2b`

구성 의도는 외부 진입 리소스(ALB 등)는 Public Subnet, EKS Worker Node는
Private Subnet에 배치하는 형태다. 실제 ALB 배치 및 모든 라우팅 테이블
규칙은 별도 확인이 필요하다.

### 3.3 NAT Gateway

  항목             값
  ---------------- -------------------------
  NAT Gateway ID   `nat-046d84cb7481de02b`
  Public IP        `43.203.93.108`
  Private IP       `10.0.3.192`

NAT Gateway는 Private Subnet 리소스의 외부 통신에 사용한다. EKS를 장기간
사용하지 않을 경우에도 NAT Gateway 시간당 비용과 데이터 처리 비용이
발생할 수 있어 삭제 여부를 검토하기로 했다.

### 3.4 EKS Add-ons 및 부가 구성

대화에서 설치 또는 활성화한 것으로 언급된 항목:

-   Amazon VPC CNI
-   CoreDNS
-   kube-proxy
-   Amazon EKS Pod Identity Agent
-   Node Monitoring Agent
-   Metrics Server
-   External DNS

각 Add-on의 정확한 버전과 현재 활성 상태는 별도 확인이 필요하다.

## 4. Kubernetes 리소스

### 4.1 Namespace

  Namespace   용도
  ----------- ---------------------
  `default`   CampusPolio Backend
  `argocd`    Argo CD 구성요소

### 4.2 Backend Deployment

  항목                 값
  -------------------- ------------------------------
  Deployment 이름      `campuspolio-backend`
  Namespace            `default`
  당시 replicas 설정   `2`
  Container Port       `8080`
  ServiceAccount       `campuspolio-backend-sa`
  Spring Profile       `prod`
  Secret 참조          `campuspolio-backend-secret`
  ECR Repository       `campuspolio-backend`

대화에서 확인된 Deployment 설정 형태:

``` yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: campuspolio-backend
  namespace: default
spec:
  replicas: 2
  selector:
    matchLabels:
      app: campuspolio-backend
  template:
    metadata:
      labels:
        app: campuspolio-backend
    spec:
      serviceAccountName: campuspolio-backend-sa
      containers:
        - name: campuspolio-backend
          image: 034317881539.dkr.ecr.ap-northeast-2.amazonaws.com/campuspolio-backend:<TAG>
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "prod"
          envFrom:
            - secretRef:
                name: campuspolio-backend-secret
```

`<TAG>`는 설명용 자리표시자다. 실제 배포에서는 GitHub Actions가 생성한
Commit SHA 기반 Tag가 반영됐다. Secret의 실제 키와 값은 보안상 문서에
포함하지 않는다.

### 4.3 Service

Backend Service는 `ClusterIP` 유형으로 구성했다. Ingress 및 ALB에서
전달된 트래픽을 Backend Pod로 전달하는 내부 서비스 역할을 한다. 정확한
Service YAML 전문과 `targetPort`는 저장소 파일에서 확인해야 한다.

### 4.4 Ingress 및 ALB

-   Kubernetes Ingress와 AWS Load Balancer Controller를 연동했다.
-   API 도메인으로 `api.campuspolio.cloud`를 사용했다.
-   ACM 인증서를 ALB HTTPS Listener에 연결하는 구성을 진행했다.
-   HTTP에서 HTTPS로 리다이렉트하는 설정을 다뤘다.
-   인증서 전파 및 Ingress 설정을 점검한 뒤 HTTPS 연결을 정상화했다.

대화에서 확인된 API ALB DNS:

``` text
k8s-default-campuspo-bbe2b29bc5-765466277.ap-northeast-2.elb.amazonaws.com
```

대화에서 언급된 ACM 인증서 ARN:

``` text
arn:aws:acm:ap-northeast-2:034317881539:certificate/d2711776-7aeb-4233-8cfc-ffb7c85fe906
```

인증서 ARN은 과거 설정 과정에서 언급된 값이다. 현재 인증서 상태 및 실제
Ingress 연결 여부는 AWS 콘솔과 YAML로 재확인해야 한다.

`hello.campuspolio.cloud`용 별도 Hello World 실습 ALB도 존재했던 것으로
기록되어 있으며, 현재 삭제 여부는 확인되지 않았다.

## 5. Argo CD 및 GitOps

### 5.1 설치 및 실행 위치

Argo CD는 `argocd` Namespace에 설치했다. 설치 후 관련 Pod가 실행되는
것을 확인했고 대시보드에도 접근했다.

대화에서 언급된 구성요소는 Argo CD Server, Application Controller, Repo
Server, Redis 등이다. 설치에 사용한 정확한 Helm Chart 버전 또는 설치
매니페스트는 현재 대화 기록에서 확인되지 않는다.

### 5.2 Application 설정

Backend용 Argo CD Application:

  항목                    설정
  ----------------------- ------------------------------------------
  Application 이름        `campuspolio-backend`
  Git Repository          `https://github.com/pokowoo/CampusPolio`
  Target Branch           `feature/eks-cicd`
  Manifest Path           `eks-lab/backend`
  Destination Namespace   `default`
  Sync Policy             Automated
  Prune                   활성화
  Self Heal               활성화

### 5.3 Sync 동작

-   **Automated Sync**: Git Repository의 매니페스트 변경을 감지해 자동
    동기화한다.
-   **Prune**: Git 선언에서 삭제된 리소스를 클러스터에서도 삭제하도록
    한다.
-   **Self Heal**: Git 선언 상태와 실제 클러스터 상태가 달라졌을 때 Git
    선언 상태로 복구하도록 한다.

초기 Hello World Application은 `OutOfSync` 및 `Missing` 상태를 보인 적이
있다. Repository 주소, Branch, 경로와 매니페스트를 점검하며 동기화
과정을 진행했다.

### 5.4 GitOps 배포 흐름

``` mermaid
flowchart TD
    DEV[Backend 코드 수정]
    PUSH[GitHub Push]
    GHA[GitHub Actions]
    BUILD[Gradle Build]
    DOCKER[Docker Image Build]
    ECR[ECR Push]
    MANIFEST[Deployment YAML Image Tag 변경]
    COMMIT[Manifest Commit 및 Push]
    ARGO[Argo CD 변경 감지]
    SYNC[Automated Sync]
    DEPLOY[Kubernetes Deployment 갱신]
    POD[새 Backend Pod 실행]

    DEV --> PUSH --> GHA --> BUILD --> DOCKER --> ECR
    ECR --> MANIFEST --> COMMIT --> ARGO --> SYNC --> DEPLOY --> POD
```

Argo CD가 이미지를 직접 빌드하는 것은 아니다. GitHub Actions가 이미지
빌드와 ECR Push를 수행하고 매니페스트의 이미지 Tag를 갱신하면, Argo CD가
그 변경을 클러스터에 반영한다.

## 6. GitHub Actions 및 Amazon ECR

### 6.1 관련 Workflow

  --------------------------------------------------------------------------------
  파일                                         목적
  -------------------------------------------- -----------------------------------
  `.github/workflows/deploy.yml`               기존 EC2 Docker Hub 및 Docker
                                               Compose 배포

  `.github/workflows/build-and-push-ecr.yml`   EKS Hello World 실습용 ECR
                                               빌드·Push

  `.github/workflows/deploy-eks-backend.yml`   CampusPolio Backend의 EKS 배포
  --------------------------------------------------------------------------------

기존 EC2 배포 Workflow인 `deploy.yml`은 EKS 실습 후 EC2로 복귀할 때
사용할 fallback으로 유지하기로 했다.

### 6.2 Backend EKS Workflow 처리 과정

대화에서 확인된 `deploy-eks-backend.yml` 구성:

1.  `feature/eks-cicd` Branch Push 시 Workflow 실행
2.  Backend 소스 및 빌드 파일 변경 경로를 기준으로 실행
3.  Java 21 환경 구성
4.  Gradle 빌드
5.  Docker Image 빌드
6.  GitHub OIDC를 이용한 AWS Role 인증
7.  ECR 로그인
8.  `campuspolio-backend` Repository로 이미지 Push
9.  Deployment YAML의 이미지 Tag를 Commit SHA로 변경
10. 변경된 Manifest를 GitHub에 Commit 및 Push
11. Argo CD가 Manifest 변경을 감지하고 배포

테스트를 제외한 빌드 명령:

``` bash
./gradlew clean build -x test
```

### 6.3 ECR 주소 및 이미지 Tag

``` text
034317881539.dkr.ecr.ap-northeast-2.amazonaws.com/campuspolio-backend:<TAG>
```

이미지 Tag는 Git Commit SHA를 사용했다. 이를 통해 배포 이미지와 소스
Commit을 추적하고 특정 버전으로 Rollback하기 쉽게 구성했다.

### 6.4 GitHub Actions OIDC Role

Role 이름:

``` text
GitHubActions-ECR-Push-Role
```

Role ARN:

``` text
arn:aws:iam::034317881539:role/GitHubActions-ECR-Push-Role
```

GitHub Actions가 OIDC 토큰을 발급하고 AWS STS의
`AssumeRoleWithWebIdentity`를 통해 임시 자격 증명을 받는 구조다. 장기
AWS Access Key를 Workflow에 직접 넣는 방식이 아니다.

### 6.5 ECR Push 권한 오류 해결

초기 IAM Policy가 Hello World Repository만 허용해 Backend 이미지
Push에서 권한 오류가 발생했다. `campuspolio-backend` Repository ARN을
추가하고 기존 `campuspolio-hello` 권한을 유지해 해결했다.

대화에서 정리한 Policy 핵심:

``` json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "ECRAuthorization",
      "Effect": "Allow",
      "Action": ["ecr:GetAuthorizationToken"],
      "Resource": "*"
    },
    {
      "Sid": "ECRPushImage",
      "Effect": "Allow",
      "Action": [
        "ecr:BatchCheckLayerAvailability",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:CompleteLayerUpload",
        "ecr:PutImage",
        "ecr:BatchGetImage"
      ],
      "Resource": [
        "arn:aws:ecr:ap-northeast-2:034317881539:repository/campuspolio-hello",
        "arn:aws:ecr:ap-northeast-2:034317881539:repository/campuspolio-backend"
      ]
    }
  ]
}
```

실제 IAM Policy의 최신 상태는 AWS IAM 콘솔에서 다시 확인해야 한다.

## 7. EKS Pod Identity 및 S3 접근

### 7.1 구성 정보

  항목             값
  ---------------- -------------------------------
  IAM Role         `CampusPolio-Backend-PodRole`
  Namespace        `default`
  ServiceAccount   `campuspolio-backend-sa`
  인증 방식        EKS Pod Identity

Backend Deployment에서 다음 ServiceAccount를 지정했다.

``` yaml
spec:
  template:
    spec:
      serviceAccountName: campuspolio-backend-sa
```

Backend 애플리케이션은 AWS SDK의 `DefaultCredentialsProvider`를
사용하도록 구성했다. Pod 내부에 AWS Access Key를 직접 저장하지 않는
방식이다.

### 7.2 검증

STS `GetCallerIdentity` 호출을 통해 Pod에 연결된 IAM Role이 정상
적용되는 것을 확인했다. S3 연동을 위한 IAM 권한 구성도 진행했다. 정확한
S3 Bucket 이름과 최종 IAM Policy 전문은 대화 기록에서 확인되지 않는다.

## 8. RDS 및 JWT 인증

### 8.1 RDS 연결

-   기존 CampusPolio RDS MySQL을 사용했다.
-   RDS Public Access는 Disabled 상태로 유지했다.
-   RDS Security Group에서 EKS Node Security Group을 통한 TCP 3306
    접근을 허용했다.
-   EKS Backend에서 RDS 연결을 확인했다.

RDS의 정확한 보안 그룹 ID와 전체 인바운드 규칙은 별도 확인이 필요하다.

### 8.2 JWT 인증 구조

  항목                      설정
  ------------------------- ----------------------------
  Access Token 만료         15분
  Refresh Token 만료        14일
  전달 방식                 HttpOnly Cookie
  Access Token 검증         Spring Security JWT Filter
  Refresh Token 저장        MySQL
  Refresh Token DB 저장값   SHA-256 Hash
  Refresh Token 갱신        Rotation

Access Token은 Backend에서 서명과 만료 시간을 검증한다. Refresh Token은
DB에 저장된 해시를 이용해 검증 및 회전 처리하도록 구성했다.

### 8.3 검증 결과

EKS 환경에서 다음 기능이 정상 동작하는 것을 확인했다.

-   JWT 로그인
-   JWT 로그아웃
-   Refresh Token 갱신 및 Rotation

Refresh 응답 본문에는 사용자 프로필 정보가 반환되고 새 토큰은
`Set-Cookie` 헤더로 전달되는 구조를 확인했다.

## 9. 구축 과정의 문제 및 해결

### 9.1 Worker Node 조인 실패

**증상** - Node Group 생성 실패 - `kubectl get nodes`에서 노드가
표시되지 않음 - 인스턴스가 EKS에 조인하지 못함

**조치** - Node Group 설정 및 IAM 권한 점검 - Node Group 재생성 및 설정
조정 - 이후 `kubectl get nodes`에서 Node `Ready` 상태 확인

초기에는 `t2.micro`를 사용하려 했고, 이후 `t3.medium` 기반 Node Group을
구성했다.

### 9.2 GitHub Actions ECR Push 권한 오류

**원인** - IAM Policy가 Hello World ECR Repository만 허용 - Backend
Repository Push 권한 부족

**해결** - `campuspolio-backend` Repository ARN을 IAM Policy에 추가 -
기존 `campuspolio-hello` 권한 유지

### 9.3 Argo CD Application의 OutOfSync / Missing

**증상** - Application이 `OutOfSync` 또는 `Missing` 상태로 표시됨

**점검 항목** - Repository URL - Target Branch - Manifest 경로 -
Manifest의 리소스 이름과 Namespace - 실제 클러스터 리소스 존재 여부

### 9.4 HTTPS 및 ALB 연결 문제

**조치** - ACM 인증서 발급 및 DNS 전파 상태 점검 - Ingress의 ALB HTTPS
Listener 설정 확인 - HTTP → HTTPS 리다이렉트 설정 확인 - DNS와 ALB 연결
상태 점검 - 이후 API HTTPS 연결 정상화

### 9.5 EKS Pod의 AWS 권한 연결

**해결** - Pod Identity Agent 설치 - IAM Role 생성 - ServiceAccount
연결 - STS `GetCallerIdentity`로 Role 적용 확인

## 10. 저장소 경로 및 관련 파일

GitHub Repository:

``` text
https://github.com/pokowoo/CampusPolio
```

실습 Branch:

``` text
feature/eks-cicd
```

Backend Kubernetes Manifest 경로:

``` text
eks-lab/backend/
```

대화에서 확인된 파일:

``` text
eks-lab/backend/
├── backend-deployment.yaml
├── backend-service.yaml
├── serviceaccount.yaml
└── backend-ingress.yaml
```

Workflow:

``` text
.github/workflows/
├── deploy.yml
├── build-and-push-ecr.yml
└── deploy-eks-backend.yml
```

로컬 프로젝트 경로:

``` text
D:\2026java\CampusPolio
```

파일이 현재 저장소에 존재하는지, 대화 이후 수정됐는지는 GitHub에서
확인해야 한다.

## 11. EC2 복귀 및 EKS 정리 계획

EKS 배포 실습과 JWT 인증 검증을 완료한 후 기존 EC2 기반 환경으로
복귀하기로 했다.

### 11.1 EC2 복귀 순서

1.  EC2에 접속해 `docker ps`로 컨테이너 상태 확인
2.  EC2 Nginx 및 HTTPS 인증서 확인
3.  EC2 환경변수의 JWT Secret 및 RDS 설정 확인
4.  EC2에서 로그인·로그아웃·Refresh Token 동작 확인
5.  `api.campuspolio.cloud` DNS를 EC2 Elastic IP로 변경
6.  EC2 HTTPS 및 API 정상 동작 확인
7.  EKS ALB 등 불필요한 리소스 정리
8.  EKS Cluster 삭제 여부 결정

EC2 전환 검증 전에는 EKS ALB나 클러스터를 먼저 삭제하지 않는다.

### 11.2 EKS 유지와 삭제의 차이

EKS는 EC2처럼 단순 Stop 상태로 전환하는 기능이 없다.

-   Node Group의 Desired Capacity를 0으로 설정하면 Worker Node 컴퓨팅
    비용을 줄일 수 있다.
-   Node가 0개여도 EKS Cluster 관리 비용은 발생한다.
-   NAT Gateway와 ALB도 남아 있으면 별도 비용이 발생한다.
-   Argo CD는 Node가 없으면 Pod로 실행되지 않는다.
-   ECR Repository는 EKS Cluster와 별개이므로 이미지 보관을 위해 유지할
    수 있다.

현재 Node Group은 대화상 `min=0`, `max=2`, `desired=0`으로 확인됐다.

장기간 사용하지 않을 경우 Kubernetes YAML, GitHub Actions Workflow, IAM
구성 및 네트워크 정보를 보관하고 EKS Cluster를 삭제하는 방안을
검토하기로 했다.

### 11.3 재구축을 위해 보관할 자료

-   `eks-lab/backend/` Kubernetes Manifest
-   `.github/workflows/deploy-eks-backend.yml`
-   GitHub OIDC Role Trust Policy
-   ECR Push IAM Policy
-   Pod Identity Role 및 Policy
-   VPC·Subnet·Route Table·Security Group 설정
-   Argo CD Application 설정
-   Ingress 및 ACM 연결 설정
-   구축 과정에서 발생한 오류와 해결 과정

## 12. 주요 점검 명령어

### Kubernetes 기본 상태

``` bash
kubectl get nodes
kubectl get pods -A
kubectl get svc -A
kubectl get ingress -A
```

### Backend 상태

``` bash
kubectl get deployment campuspolio-backend
kubectl get pods -l app=campuspolio-backend
kubectl describe deployment campuspolio-backend
kubectl logs -l app=campuspolio-backend
```

### Argo CD 상태

``` bash
kubectl get pods -n argocd
kubectl get applications -n argocd
```

### kubeconfig 컨텍스트

``` bash
kubectl config current-context
kubectl config get-contexts
```

## 13. 확인되지 않은 항목 및 보완 목록

다음 항목은 채팅 기록만으로 정확한 전문이나 현재 상태를 확정할 수
없으므로 실제 AWS 콘솔 또는 GitHub 파일을 확인해 보완해야 한다.

-   EKS Kubernetes 버전 및 현재 클러스터 상태
-   EKS Add-on의 정확한 버전
-   Argo CD 설치 방식, Chart 버전, 설치 명령
-   Argo CD Application YAML 원문
-   Backend Service 및 Ingress YAML 전체 내용
-   ALB Listener, Target Group, Health Check 상세 설정
-   ACM 인증서의 현재 상태 및 실제 연결 리소스
-   GitHub Actions Workflow 전체 원문
-   GitHub OIDC Trust Policy의 실제 Subject 조건
-   Pod Identity IAM Policy 전문 및 S3 Bucket 이름
-   RDS Security Group의 전체 규칙
-   EKS 및 부가 리소스의 현재 삭제 여부

> **보안 주의:** AWS Access Key, Secret Key, JWT Secret, DB 비밀번호,
> 쿠키 서명 키 등 비밀값은 문서나 Git Repository에 평문으로 저장하지
> 않는다.
