#!/bin/bash

echo ">>> 기존 WAS와 프록시 포트 스위칭"

# 현재 메인 WAS의 포트 번호를 읽어온다
CURRENT_PORT=$(cat /home/ec2-user/service_url.inc  | grep -Po '[0-9]+' | tail -1)
TARGET_PORT=0

echo "> 현재 Nginx의 활성 프록시 포트는 ${CURRENT_PORT} 입니다."

if [ "${CURRENT_PORT}" -eq 8081 ]; then
    TARGET_PORT=8082
elif [ "${CURRENT_PORT}" -eq 8082 ]; then
    TARGET_PORT=8081
else
    echo "> 현재 실행 중인 WAS가 없습니다."
    exit 1
fi

# 프록시 포트를 TARGET_PORT로 변경한다
echo "set \$service_url http://127.0.0.1:${TARGET_PORT};" | tee /home/ec2-user/service_url.inc

echo "> 새로운 Nginx 활성 프록시 포트는 ${TARGET_PORT} 입니다."

# Slack으로 Nginx 상태 알리기
/home/ec2-user/app/scripts/send_slack_message.sh "새로운 Nginx 활성 프록시 포트는 ${TARGET_PORT} 입니다."

# Reload nginx
sudo service nginx reload

echo "> Nginx가 리로드 되었습니다."
