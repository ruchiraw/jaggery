var pipe = require('pipe');
var app = require('app');

pipe.plug(require('router'));

pipe.plug(function (error, req, res, ses, hand) {
    print(JSON.stringify(error));
});

require('./controllers/routes.js');

app.serve(function (req, res) {
    //print('hello world');
    //res.write('hello world');
    pipe.resolve(req, res);
});

