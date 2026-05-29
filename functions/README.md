# Firebase Cloud Functions - Hướng dẫn Deploy

## Giới thiệu
Sử dụng **SendGrid** để gửi email - Free tier: **100 emails/ngày**

## Prerequisites

1. Cài đặt Node.js 18+
2. Cài đặt Firebase CLI:
```bash
npm install -g firebase-tools
```

3. Login Firebase:
```bash
firebase login
```

## Đăng ký SendGrid (Miễn phí)

1. Vào https://signup.sendgrid.com/
2. Chọn **Free Plan**
3. Xác minh email
4. Vào https://app.sendgrid.com/settings/api_keys
5. Tạo API Key mới
6. Copy API Key (bắt đầu bằng `SG.`)

## Cấu hình Environment Variables

### Cách 1: Runtime environment variables (cho local development)
```bash
cd functions
npm install
SENDGRID_API_KEY="SG.your_api_key_here" SENDGRID_FROM_EMAIL="noreply@bunnyenglish.app" npx firebase deploy --only functions
```

### Cách 2: Cloud Functions Environment (Production)
```bash
firebase functions:config:set sendgrid.apikey="SG.your_api_key_here" sendgrid.from="noreply@bunnyenglish.app"
npx firebase deploy --only functions
```

## Deploy Functions

```bash
cd functions
npm install
firebase deploy --only functions
```

## Testing Local

```bash
firebase emulators:start --only functions
```

## Kiểm tra Logs

```bash
firebase functions:log
```

## Giới hạn SendGrid Free Plan

- **100 emails/ngày**
- 1 sender verification required
- Cần xác minh sender email/domain

## Troubleshooting

### Lỗi Authentication
- Kiểm tra API Key đã đúng chưa
- Đảm bảo sender email đã được verify

### Lỗi Rate Limit
- Free plan giới hạn 100 emails/ngày
- Theo dõi usage tại https://app.sendgrid.com/statistics

### Kiểm tra Firebase Project
```bash
firebase projects:list
firebase use minlish-1e2ec
```
