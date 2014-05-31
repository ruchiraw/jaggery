var log = require('log');
var app = require('app');
var pipe = require('pipe');

/**
 * adding router as a plugin
 */
pipe.plug(require('router'));

/**
 * error handling pipe
 */
pipe.plug(function (error, req, res, ses, hand) {
    log.error(JSON.stringify(error));
});

/**
 * registers routes to be served
 */
require('./controllers/routes.js');

/**
 * registering request callback
 */
app.serve(function (req, res) {
    pipe.resolve(req, res);
});

