const functions = require('firebase-functions');
const admin = require('firebase-admin');
if (admin.apps.length === 0) {
    admin.initializeApp({});
}

// exports.user = require('./user')
exports.posts = require('./posts')
