const functions = require('firebase-functions');
const admin = require('firebase-admin');
if (admin.apps.length === 0) {
    admin.initializeApp({});
}

// exports.user = require('./user')
const posts = require('./posts')

const user = require('./user')


exports.getLikesDetails = functions.https.onCall((data, context) => {
    console.log('getLikesDetails in index.js')
    return user.getLikesDetails(data, context)
})

exports.getFollowings = functions.https.onCall((data, context) => {
    console.log('getFollowings in index.js')
    return user.getFollowings(data, context)
})

exports.getFollowers = functions.https.onCall((data, context) => {
    console.log('getFollowers in index.js')
    return user.getFollowers(data, context)
})

exports.getProfilePosts = functions.https.onCall((data, context) => {
    console.log('getProfilePosts in index.js')
    return posts.getProfilePosts(data, context)
})

exports.onNewPostCreation = functions.firestore.document('posts/{postId}').onCreate((snapshot, context) => {
    console.log('onNewPostCreation in index.js')
    return posts.onNewPostCreation(snapshot, context)
})