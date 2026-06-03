const functions = require('firebase-functions');
const admin = require('firebase-admin');
const nodemailer = require('nodemailer');

admin.initializeApp();

const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: 'Lethao4725@gmail.com', // Thay bằng email của bạn
        pass: 'cxqawqaegusotabo'    // Thay bằng mã 16 ký tự mật khẩu ứng dụng
    }
});

/**
 * 1. HÀM GỬI EMAIL: Tự động chạy khi có bản ghi mới trong 'mail_queue'
 */
exports.sendEmailReminder = functions.region('asia-southeast1')
    .database.ref('/mail_queue/{pushId}')
    .onCreate(async (snapshot, context) => {
        const data = snapshot.val();
        const mailOptions = {
            from: '"Minlish App 🐰" <Lethao4725@gmail.com>',
            to: data.to,
            subject: data.message.subject,
            text: data.message.text
        };

        try {
            await transporter.sendMail(mailOptions);
            return snapshot.ref.remove();
        } catch (error) {
            console.error('Lỗi khi gửi mail:', error);
            return null;
        }
    });

/**
 * 2. HÀM LẬP LỊCH: Tự động chạy vào 8:00 sáng mỗi ngày (Giờ Việt Nam)
 * Nhiệm vụ: Tìm tất cả người dùng bật thông báo và bỏ vào mail_queue
 */
exports.dailyReminderScheduler = functions.pubsub.schedule('55 17 * * *')
    .timeZone('Asia/Ho_Chi_Minh')
    .onRun(async (context) => {
        const usersSnapshot = await admin.database().ref('/users').once('value');
        const users = usersSnapshot.val();

        if (!users) return null;

        const updates = {};
        const message = "Bạn ơi ! Bạn đã quên minlish rồi sao. Hãy ghé ứng dụng để học tập nào";

        for (const key in users) {
            const user = users[key];
            const profile = user.userProfile;

            // Kiểm tra điều kiện: Người dùng bật cả Nhắc nhở và Email
            if (profile && profile.dailyReminder === true && profile.emailNotification === true) {
                const mailData = {
                    to: user.account.email,
                    message: {
                        subject: "Nhắc nhở học tập hàng ngày 🐰",
                        text: message
                    },
                    timestamp: admin.database.ServerValue.TIMESTAMP
                };

                const newPostKey = admin.database().ref().child('mail_queue').push().key;
                updates[`/mail_queue/${newPostKey}`] = mailData;
            }
        }

        if (Object.keys(updates).length === 0) return null;
        return admin.database().ref().update(updates);
    });
