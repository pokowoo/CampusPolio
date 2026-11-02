[1mdiff --git a/src/main/resources/application-prod.yml b/src/main/resources/application-prod.yml[m
[1mindex 43a6337..bb6937d 100644[m
[1m--- a/src/main/resources/application-prod.yml[m
[1m+++ b/src/main/resources/application-prod.yml[m
[36m@@ -54,13 +54,10 @@[m [mspringdoc:[m
   override-with-generic-response: false[m
 cloud:[m
   aws:[m
[31m-    region: ${AWS_REGION}[m
[31m-[m
[32m+[m[32m    region: ap-northeast-2[m
     s3:[m
[31m-      bucket: ${AWS_S3_BUCKET}[m
[32m+[m[32m      bucket: campuspolio-storage-2026[m
[32m+[m
 [m
[31m-    credentials:[m
[31m-      access-key: ${AWS_ACCESS_KEY}[m
[31m-      secret-key: ${AWS_SECRET_KEY}[m
 claude:[m
   api-key: ${CLAUDE_API_KEY}[m
