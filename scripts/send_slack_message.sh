#!/bin/bash

# 첫 번째 인자를 MESSAGE 변수에 저장
MESSAGE="$1"

# Slack Webhook URL
WEBHOOK_URL="https://hooks.slack.com/services/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"

# JSON 데이터 생성
DATA="{\"text\": \"$MESSAGE\"}"

# POST 요청
curl -X POST -H "Content-Type: application/json" -d "$DATA" "$WEBHOOK_URL"
