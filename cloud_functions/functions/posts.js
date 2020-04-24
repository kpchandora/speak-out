const functions = require('firebase-functions');
const admin = require('firebase-admin');

const db = admin.firestore()

exports.onNewPostCreation = function (snapshot, context) {
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
}


exports.getProfilePosts = async function (data, context) {

    console.log("In getProfilePosts: " + data.userId)

    try {
        const posts = await db.collection('posts').where('userId', '==', data.userId)
            .orderBy('timeStampLong', 'desc')
            .get()

        console.log("Posts: " + posts)

        if (posts.empty) {
            console.log("Posts empty")
            return []
        } else {
            const postsList = []
            const promises = []
            posts.forEach(doc => {
                var newDoc = doc.data()
                promises.push(db.doc(`post_likes/${doc.id}`).get().then(value => {
                    if (value.exists) {
                        console.log(value.data().usersMap)
                        if (value.data().usersMap) {
                            console.log("Is likes by user: " + (data.userId in value.data().usersMap))
                            newDoc['isLikedBySelf'] = data.userId in value.data().usersMap
                        } else {
                            console.log("Not liked by user")
                            newDoc['isLikedBySelf'] = false
                        }
                    } else {
                        console.log("Not exists")
                        newDoc['isLikedBySelf'] = false
                    }
                    postsList.push(newDoc)
                }))
            })
            return Promise.all(promises).then(r => {
                console.log("PostsList: " + postsList)
                return postsList
            }).catch(error => {
                return new functions.https.HttpsError(error)
            })
        }
    } catch (error) {
        return new functions.https.HttpsError(error)
    }
}
