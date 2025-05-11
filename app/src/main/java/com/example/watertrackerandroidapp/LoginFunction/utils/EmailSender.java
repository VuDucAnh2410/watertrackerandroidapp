package com.example.watertrackerandroidapp.LoginFunction.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {
    private static final String TAG = "EmailSender";

    // Thay thế bằng email Gmail của bạn
    private static final String EMAIL_FROM = "your_email@gmail.com";
    // Thay thế bằng mật khẩu ứng dụng (không phải mật khẩu Gmail)
    private static final String EMAIL_PASSWORD = "your_app_password";

    public interface EmailCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public static void sendEmail(String recipientEmail, String subject, String messageBody, EmailCallback callback) {
        new SendEmailTask(recipientEmail, subject, messageBody, callback).execute();
    }

    private static class SendEmailTask extends AsyncTask<Void, Void, Boolean> {
        private String recipientEmail;
        private String subject;
        private String messageBody;
        private EmailCallback callback;
        private String errorMessage;

        public SendEmailTask(String recipientEmail, String subject, String messageBody, EmailCallback callback) {
            this.recipientEmail = recipientEmail;
            this.subject = subject;
            this.messageBody = messageBody;
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Properties properties = new Properties();
                properties.put("mail.smtp.host", "smtp.gmail.com");
                properties.put("mail.smtp.port", "587");
                properties.put("mail.smtp.auth", "true");
                properties.put("mail.smtp.starttls.enable", "true");
                properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
                properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
                properties.put("mail.smtp.socketFactory.port", "587");
                properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

                Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                    }
                });

                // Bật chế độ debug
                session.setDebug(true);

                MimeMessage mimeMessage = new MimeMessage(session);
                mimeMessage.setFrom(new InternetAddress(EMAIL_FROM));
                mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
                mimeMessage.setSubject(subject);
                mimeMessage.setText(messageBody);

                Transport.send(mimeMessage);
                return true;
            } catch (MessagingException e) {
                Log.e(TAG, "Error sending email", e);
                errorMessage = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                callback.onSuccess();
            } else {
                callback.onFailure(errorMessage);
            }
        }
    }
}
