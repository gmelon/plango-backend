#!/bin/bash

echo ">>> 새로운 WAS 실행"

# 현재 실행 중인 환경을 조회
CURRENT_PORT=$(cat /home/ec2-user/service_url.inc  | grep -Po '[0-9]+' | tail -1)

if [ "${CURRENT_PORT}" -eq 8081 ]; then
    # BLUE 실행 중
    NEW_CONTAINER=green
    OLD_CONTAINER=blue
    NEW_PORT=8082
elif [ "${CURRENT_PORT}" -eq 8082 ]; then
    # GREEN 실행 중
    NEW_CONTAINER=blue
    OLD_CONTAINER=green
    NEW_PORT=8081
else
    # 기본 값 - BLUE 실행
    NEW_CONTAINER=blue
    OLD_CONTAINER=green
    NEW_PORT=8081
fi

# 새로운 docker-compose up
echo "> ${NEW_CONTAINER} 실행"
sudo docker-compose -p plango-"${NEW_CONTAINER}" -f /home/ec2-user/app/plango-backend/scripts/docker-compose."${NEW_CONTAINER}".yml up -d --build

# 새로운 환경 health check
for RETRY_COUNT in $(seq 1 10);
do
    echo "> #${RETRY_COUNT} 번째 시도..."
    RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}"  http://127.0.0.1:${NEW_PORT}/health)

    if [ "${RESPONSE_CODE}" -eq 200 ]; then
        echo "> 새로운 WAS가 정상적으로 실행되고 있습니다."

        # Slack으로 서버 실행 성공 알림 보내기
        /home/ec2-user/app/scripts/send_slack_message.sh "서버 실행에 성공했습니다."
        break

    elif [ "${RETRY_COUNT}" -eq 10 ]; then
        echo "> health check에 실패했습니다."

        # Slack으로 서버 실행 실패 알림 보내기
        /home/ec2-user/app/scripts/send_slack_message.sh "서버 실행에 실패했습니다."

        # health check 실패 시 배포 강제 종료
        exit 1
    fi
    sleep 10
done

# health check 성공, nginx 프록시 포트 변경
echo "set \$service_url http://127.0.0.1:${NEW_PORT};" | tee /home/ec2-user/service_url.inc
echo "> 새로운 Nginx 활성 프록시 포트는 ${NEW_PORT} 입니다."

# Slack으로 Nginx 상태 알리기
/home/ec2-user/app/scripts/send_slack_message.sh "새로운 Nginx 활성 프록시 포트는 ${NEW_PORT} 입니다."

# Reload nginx
sudo service nginx reload

# 기존에 실행 중이었던 환경 종료
sudo docker-compose -p plango-${OLD_CONTAINER} -f /home/ec2-user/app/plango-backend/scripts/docker-compose.${OLD_CONTAINER}.yml down

# 종료된 컨테이너와 이미지 삭제
sudo docker container prune -f

exit 0
