FROM openjdk:11-jdk

COPY build/libs/*.jar build.jar

ENTRYPOINT [ \
"java", \
"-jar", \
"-Dspring.config.location=classpath:/application.yml,classpath:/application-real.yml,/home/ec2-user/app/application-real-db.yml,/home/ec2-user/app/application-real-s3.yml,/home/ec2-user/app/application-real-auth.yml", \
"-Dspring.profiles.active=real", \
"-Dserver.port=8080", \
"build.jar" \
]
