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


exports.getFollowers = async function (data, context) {
    console.log('In getFollowers: ', data.userId)
    try {
        const followers = await db.collection(`followers/${data.userId}/users`).get()
        if (followers.empty) {
            return []
        } else {
            const followersList = []
            const promises = []
            followers.forEach(doc => {
                promises.push(db.doc(`user_details/${doc.id}`).get().then(value => {
                    const data = value.data()
                    followersList.push({
                        name: data.name,
                        userId: data.userId,
                        photoUrl: data.photoUrl,
                        username: data.username
                    })
                }))
            })
            return Promise.all(promises).then(r => {
                return followersList
            }).catch(error => {
                return functions.https.HttpsError(error)
            })
        }
    } catch (error) {
        return functions.https.HttpsError(error)
    }
}

exports.getFollowings = async function (data, context) {
    console.log('In getFollowings: ', data.userId)
    try {
        const followings = await db.collection(`followings/${data.userId}/users`).get()
        if (followings.empty) {
            return []
        } else {
            const followingsList = []
            const promises = []
            followings.forEach(doc => {
                promises.push(db.doc(`user_details/${doc.id}`).get().then(value => {
                    const data = value.data()
                    followingsList.push({
                        name: data.name,
                        userId: data.userId,
                        photoUrl: data.photoUrl,
                        username: data.username
                    })
                }))
            })
            return Promise.all(promises).then(r => {
                return followingsList
            }).catch(error => {
                return functions.https.HttpsError(error)
            })
        }
    } catch (error) {
        return functions.https.HttpsError(error)
    }
}

exports.getLikesDetails = async function (data, context) {
    console.log('In getLikesDetails new: ', data.postId)

    try {
        const usersDocument = await db.collection(`/post_likes/${data.postId}/users`).get()
        if (usersDocument.empty) {
            return []
        } else {
            const usersList = []
            const promises = []
            usersDocument.forEach(doc => {
                promises.push(db.doc(`user_details/${doc.id}`).get().then(value => {
                    const data = value.data()
                    usersList.push({
                        name: data.name,
                        userId: data.userId,
                        photoUrl: data.photoUrl,
                        username: data.username
                    })
                }))
            })
            return Promise.all(promises).then(r => {
                return usersList
            }).catch(error => {
                return functions.https.HttpsError(error)
            })
        }
    } catch (error) {
        return functions.https.HttpsError(error)
    }
}

