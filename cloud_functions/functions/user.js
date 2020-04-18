const functions = require('firebase-functions');
const admin = require('firebase-admin');

const db = admin.firestore()

// exports.onUserDataChanged = functions.firestore.document('users/{userId}')
//     .onUpdate(async (change, context) => {
//         console.log('onUserDataChanged')
//         const oldData = change.before.data()
//         const newData = change.after.data()

//         if (newData.lastUpdated && oldData.lastUpdated && (newData.lastUpdated != oldData.lastUpdated)) {
//             console.log('Timestamp updated')
//             return null
//         }

//         var changedDataMap = new Map()

//         if (oldData.name != newData.name) {
//             changedDataMap.set("name", newData.name)
//         }

//         // console.log("OldData: ", oldData.name, oldData.lastUpdated)
//         // console.log("NewData: ", newData.name, newData.lastUpdated)
//         // console.log("UserId: ", newData.userId)
//         // console.log("Changed Map: ", changedDataMap)

//         if (changedDataMap.size == 0) {
//             console.log('No data updated')
//             return null
//         }


//         const details = {}

//         changedDataMap.forEach((value, key) => {
//             details[key] = value
//         })

//         const batch = db.batch()


//         const posts = await db.collection('posts').where('userId', '==', newData.userId)
//             .get()

//         if (!posts.empty) {
//             posts.forEach(doc => {
//                 // console.log('PostId: ', doc.id)
//                 batch.update(doc.ref, details)
//             })
//         }

//         return batch.commit().then((value) => {
//             return change.after.ref.update({
//                 lastUpdated: Date.now()
//             }).then((v) => {
//                 console.log("Successfully updated all values")
//             })
//         }).catch((error) => {
//             console.log("Error While Updating UserDetails: ", error)
//         })
//     })