var app = require('app');
var jag = require('jag');

app.serve(function (req, res) {
    jag.serve(req, res);
});

