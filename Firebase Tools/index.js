/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

// const {setGlobalOptions} = require("firebase-functions");
// const {onRequest} = require("firebase-functions/https");
// const logger = require("firebase-functions/logger");

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
//setGlobalOptions({ maxInstances: 10 });

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

const { onDocumentDeleted, onDocumentUpdated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();

/**
 * Triggered when a course is deleted from a business's Courses subcollection.
 * Sends an FCM notification to all clients enrolled in that course.
 */
exports.notifyCourseDeleted = onDocumentDeleted(
  "businesses/{businessId}/courses/{courseId}",
  async (event) => {
    const deletedCourse = event.data ? event.data.data() : null; // Data of the deleted course
    const courseId = event.params.courseId;

    console.log(`Course deleted: ${courseId}`, deletedCourse);

    // Find all enrollments for this course
    const enrollmentsSnapshot = await admin.firestore()
      .collection("enrollment")
      .where("course.id", "==", courseId)
      .get();

    if (enrollmentsSnapshot.empty) {
      console.log("No enrollments found for this course.");
      return;
    }

    const messaging = admin.messaging();
    const promises = [];

    for (const doc of enrollmentsSnapshot.docs) {
      const enrollment = doc.data();
      const clientId = enrollment.client.id; // try uid if id does not work

      // Fetch client document to get FCM token
      const clientDoc = await admin.firestore()
        .collection("clients")
        .doc(clientId)
        .get();

      if (!clientDoc.exists) {
        console.log(`No client found with ID: ${clientId}`);
        continue;
      }

      const clientData = clientDoc.data();
      if (!clientData.fcmToken) {
        console.log(`Client ${clientId} has no FCM token`);
        continue;
      }

      // Create the push notification payload
      const payload = {
        notification: {
          title: "Course Cancelled",
          body: deletedCourse?.name 
            ? `The course "${deletedCourse.name}" has been cancelled.`
            : `A course you were enrolled in has been cancelled.`,
        },
        token: clientData.fcmToken,
      };

      promises.push(messaging.send(payload));
    }

    // Send all notifications
    await Promise.all(promises);
    console.log("Delete notifications sent successfully.");
  }
);

/**
 * Triggered when a course is updated from a business's Courses subcollection.
 * Sends an FCM notification to all clients enrolled in that course.
 */
exports.notifyCourseUpdated = onDocumentUpdated(
  "businesses/{businessId}/courses/{courseId}",
  async (event) => {
    try {
      const beforeUpdatedCourse = event.data.before ? event.data.before.data() : null; // snappshot of data before update
      const afterUpdatedCourse = event.data.after ? event.data.after.data() : null; // snapshot of data after update
      const courseId = event.params.courseId;

      if (beforeUpdatedCourse == null || afterUpdatedCourse == null)
      {
        console.log(`Course updated failed - after or before is null`);
        return;
      }

      console.log(`Course updated: ${courseId}`, { before: beforeUpdatedCourse, after: afterUpdatedCourse });

      // Find all enrollments for this course
      const enrollmentsSnapshot = await admin.firestore()
        .collection("enrollment")
        .where("course.id", "==", courseId)
        .get();

      if (enrollmentsSnapshot.empty) {
        console.log("No enrollments found for this course.");
        return;
      }

      const messaging = admin.messaging();
      const promises = [];

      for (const doc of enrollmentsSnapshot.docs) {
        const enrollment = doc.data();
        const clientId = enrollment.client.id; 

        // Fetch client document to get FCM token
        const clientDoc = await admin.firestore()
          .collection("clients")
          .doc(clientId)
          .get();

        if (!clientDoc.exists) {
          console.log(`No client found with ID: ${clientId}`);
          continue;
        }

        const clientData = clientDoc.data();
        if (!clientData.fcmToken) {
          console.log(`Client ${clientId} has no FCM token`);
          continue;
        }

        // Create the push notification payload
        const payload = {
          notification: {
            title: "Course Updated",
            body: `The course "${afterUpdatedCourse.name}" has been updated.`,
          },
          token: clientData.fcmToken,
        };

        promises.push(messaging.send(payload));
      }

      // Send all notifications
      await Promise.all(promises);
      console.log("Update notifications sent successfully.");
    } catch (err) {
      console.error("Error in notifyCourseUpdated:", err);
    }
  }
);
