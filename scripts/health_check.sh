#!/bin/bash

echo ">>> 새롭게 실행된 WAS의 Health Check"

# 현재 메인 WAS의 포트 번호를 읽어온다
CURRENT_PORT=$(cat /home/ec2-user/service_url.inc | grep -Po '[0-9]+' | tail -1)
TARGET_PORT=0

if [ "${CURRENT_PORT}" -eq 8081 ]; then
    TARGET_PORT=8082
elif [ "${CURRENT_PORT}" -eq 8082 ]; then
    TARGET_PORT=8081
else
    echo "> 현재 실행 중인 WAS가 없습니다."
    exit 1
fi


echo "> 새로운 포트 'http://127.0.0.1:${TARGET_PORT}' 의 health check를 시작합니다..."

for RETRY_COUNT in $(seq 1 10);
do
    echo "> #${RETRY_COUNT} 번째 시도..."
    RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}"  http://127.0.0.1:${TARGET_PORT}/health)

    if [ "${RESPONSE_CODE}" -eq 200 ]; then
        echo "> 새로운 WAS가 정상적으로 실행되고 있습니다."
        exit 0
    elif [ "${RETRY_COUNT}" -eq 10 ]; then
        echo "> health check에 실패했습니다."
        exit 1
    fi
    sleep 10
done
