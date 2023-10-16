#!/bin/bash

echo ">>> 새로운 WAS 실행"

# 현재 메인 WAS의 포트 번호를 읽어온다
CURRENT_PORT=$(cat /home/ec2-user/service_url.inc | grep -Po '[0-9]+' | tail -1)
TARGET_PORT=0

if [ "${CURRENT_PORT}" -eq 8081 ]; then
  echo "> 현재 실행 중인 WAS의 포트 번호는 ${CURRENT_PORT} 입니다."
  TARGET_PORT=8082
elif [ "${CURRENT_PORT}" -eq 8082 ]; then
  echo "> 현재 실행 중인 WAS의 포트 번호는 ${CURRENT_PORT} 입니다."
  TARGET_PORT=8081
else
  echo "> 현재 실행 중인 WAS가 없어 8081 포트로 지정합니다."
  TARGET_PORT=8081
fi

TARGET_PID=$(lsof -Fp -i TCP:${TARGET_PORT} | grep -Po 'p[0-9]+' | grep -Po '[0-9]+')

if [ -n "${TARGET_PID}" ]; then
  echo "> 새로운 WAS의 포트 번호인 ${TARGET_PORT} 에서 기존에 실행 중인 프로세스를 kill 합니다."
  sudo kill "${TARGET_PID}"
fi

nohup java -jar -Dspring.config.location=classpath:/application.yml,classpath:/application-real.yml,/home/ec2-user/app/application-real-db.yml,/home/ec2-user/app/application-real-s3.yml,/home/ec2-user/app/application-real-auth.yml \
                -Dspring.profiles.active=real \
                -Dserver.port=${TARGET_PORT} /home/ec2-user/app/plango-backend/build/libs/* > /home/ec2-user/app/nohup.out 2>&1 &
echo "> 이제 새로운 WAS가 ${TARGET_PORT} 에서 실행됩니다."
exit 0
