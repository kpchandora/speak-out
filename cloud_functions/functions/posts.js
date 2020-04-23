const functions = require('firebase-functions');
const admin = require('firebase-admin');

const db = admin.firestore()

exports.onNewPostCreation = functions.firestore.document('posts/{postId}')
    .onCreate((snapshot, context) => {
        console.log('onNewPostCreation called')
        const singlePost = snapshot.data()
        // const postId = singlePost.postId
        // const userId = singlePost.userId
        const tagsList = singlePost.tags

        const batch = db.batch()
        // const usersPostRef = db.doc(`users/${userId}/posts/${postId}`)
        // batch.set(usersPostRef, { timeStamp: Date.now() })

        if (tagsList) {
            console.log("TagsList", tagsList)
            for (index = 0; index < tagsList.length; index++) {
                batch.update(db.doc(`tags/${tagsList[index]}`), {
                    used: admin.firestore.FieldValue.increment(1)
                })
                console.log("Tags", tagsList[index])
            }
        }

        // batch.update(snapshot.ref, { timeStamp: Date.now() })

        return batch.commit().catch((e) => {
            console.log("Error ", e)
        })
    })

exports.getPostsByUserId = functions.https.onRequest(async (req, res) => {

    console.log("In getPostsByUserId")

    try {
        const posts = await db.collection('posts').where('userId', '==', req.body.userId)
            .get()

        if (posts.empty) {
            res.send('No Posts')
        } else {
            const postsList = []
            const promises = []
            posts.forEach(doc => {
                var newDoc = doc.data()
                promises.push(db.doc(`post_likes/${doc.id}/users/${newDoc.userId}`).get().then(value => {
                    newDoc['isLikedBySelf'] = value.exists
                    postsList.push(newDoc)
                }))
            })
            Promise.all(promises).then(r => {
                res.status(200).send(postsList)
            }).catch(error => {
                res.status(500).send({ error: error })
            })
        }
    } catch (error) {
        res.status(500).send({ error: error })
    }
})

// exports.isFollowedByUser = functions.https.onRequest((req, res) => {
//     console.log("In isFollowedByUser")
//     const selfUserId = req.body.selfUserId
//     const otherUserId = req.body.otherUserId

//     db.doc(`followingsRefs/${selfUserId}`).get().then(snapshot => {
//         if (snapshot.exists) {
//             if (snapshot.data().refs) {
//                 res.send({
//                     success: true,
//                     isFollowing: (otherUserId in snapshot.data().refs)
//                 })
//             }
//         }
//         res.send({
//             success: true,
//             isFollowing: false
//         })
//     }).catch(e => {
//         res.send({
//             success: false,
//             isFollowing: false
//         })
//     })
// })

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

