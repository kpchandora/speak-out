const functions = require('firebase-functions');
const admin = require('firebase-admin');
if (admin.apps.length === 0) {
    admin.initializeApp({});
}

// exports.user = require('./user')
const posts = require('./posts')
exports.posts



exports.getLikesDetails = functions.https.onCall((data, context) => {
    console.log('getLikesDetails in index.js')
    return posts.getLikesDetails(data, context)
})

exports.getFollowings = functions.https.onCall((data, context) => {
    console.log('getFollowings in index.js')
    return posts.getFollowings(data, context)
})

exports.getFollowers = functions.https.onCall((data, context) => {
    console.log('getFollowers in index.js')
    return posts.getFollowers(data, context)
})
