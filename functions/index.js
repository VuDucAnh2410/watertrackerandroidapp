const functions = require("firebase-functions");
const sgMail = require("@sendgrid/mail");

// Khởi tạo SendGrid với API key
sgMail.setApiKey(functions.config().sendgrid.apikey);

// Cloud Function sendOTPEmail hiện có (giả định đã tồn tại)
exports.sendOTPEmail = functions.https.onCall((data, context) => {
  // Logic gửi email OTP hiện có của bạn
});

// Cloud Function mới sendWelcomeEmail
exports.sendWelcomeEmail = functions.https.onCall(async (data, context) => {
  const { email } = data;

  if (!email) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Email là bắt buộc"
    );
  }

  const msg = {
    to: email,
    from: "your-verified-email@domain.com", // Thay bằng email người gửi đã xác minh
    subject: "Chào mừng đến với Water Tracker!",
    text: "Cảm ơn bạn đã đăng ký với Water Tracker! Hãy duy trì thói quen uống nước và tận hưởng việc theo dõi lượng nước của bạn.",
    html: "<p>Cảm ơn bạn đã đăng ký với <strong>Water Tracker</strong>!</p><p>Hãy duy trì thói quen uống nước và tận hưởng việc theo dõi lượng nước của bạn.</p>",
  };

  try {
    await sgMail.send(msg);
    return { success: true };
  } catch (error) {
    console.error("Lỗi gửi email chào mừng:", error);
    throw new functions.https.HttpsError(
      "internal",
      `Không thể gửi email chào mừng: ${error.message}`
    );
  }
});
