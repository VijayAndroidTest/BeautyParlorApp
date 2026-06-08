const functions = require("firebase-functions");
const admin = require("firebase-admin");

// Initialize the Firebase Admin SDK to allow this function to interact with
// your Firebase project's services, like Firestore.
admin.initializeApp();

// --- START: Referral Bonus Function ---
// This function is triggered when a new document is created in the 'users'
// collection.
exports.awardReferralBonus = functions.firestore
  .document("users/{newUserId}")
  .onCreate(async (snap, context) => {
    const newUser = snap.data();

    // Assumes a 'referralCode' field in the new user's document
    const referralCode = newUser.referralCode;

    // Check if the new user's document contains a referral code
    if (referralCode) {
      // Find the user who referred them using the referralCode
      const referredUserRef = admin.firestore()
        .collection("users")
        .where("mobileNumber", "==", referralCode)
        .limit(1);

      const referredUserSnapshot = await referredUserRef.get();

      // Check if a matching referrer was found
      if (!referredUserSnapshot.empty) {
        const referredUserDoc = referredUserSnapshot.docs[0];
        const newReferralPoints = 200; // Define your bonus amount

        // Use a transaction to safely update points for the referrer
        return admin.firestore().runTransaction(async (transaction) => {
          const freshReferrerDoc = await transaction
            .get(referredUserDoc.ref);
          const freshPoints = freshReferrerDoc.data().points || 0;

          // Award points to the referrer
          transaction.update(referredUserDoc.ref, {
            points: freshPoints + newReferralPoints,
          });

          // Add the new user to the referrer's 'referredUsers'
          // subcollection for tracking
          transaction.set(
            referredUserDoc.ref
              .collection("referredUsers")
              .doc(context.params.newUserId),
            {
              referredUserMobile: newUser.mobileNumber,
              referralDate: admin.firestore.Timestamp.now(),
              pointsAwarded: newReferralPoints,
            },
          );
        });
      } else {
        functions.logger.log("Referral code not found:", referralCode);
        return null;
      }
    }
    return null;
  });
// --- END: Referral Bonus Function ---

// --- START: Booking Alarm Function ---
// This function is triggered when a new document is created in the 'bookings'
// collection.
exports.onNewBooking = functions.firestore
  .document("bookings/{bookingId}")
  .onCreate(async (snap, context) => {
    const newBooking = snap.data();

    // Log the booking details to the Firebase Functions logs for monitoring.
    // This serves as a basic "alarm" you can view in the Firebase console.
    functions.logger.info("🔔 New Appointment Booked!", {
      bookingId: context.params.bookingId,
      service: newBooking.subItemName,
      date: newBooking.appointmentDate,
      time: newBooking.appointmentTime,
      customer: newBooking.userName,
      phone: newBooking.userPhone,
    });

    // You can add logic here to send a push notification to your admin app
    // or trigger a third-party service (like Twilio for SMS or a call).

    // Example: Sending a silent push notification to an admin app
    const message = {
      notification: {
        title: "New Booking Alert!",
        body: `A new appointment has been booked by ` +
              `${newBooking.userName} for ${newBooking.subItemName}.`,
      },
      data: {
        bookingId: context.params.bookingId,
      },
      token: "your_admin_app_fcm_token", // FCM token for the admin device
    };

    try {
      // Send the message
      const response = await admin.messaging().send(message);
      functions.logger.info("Successfully sent message:", response);
    } catch (error) {
      functions.logger.error("Error sending message:", error);
    }

    return null;
  });
// --- END: Booking Alarm Function --