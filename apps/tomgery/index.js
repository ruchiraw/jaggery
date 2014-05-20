//var pipe = require('pipe');

/*var router = require('./router/router.js');

 router.app.get('/users/:id', function (req) {
 print('Hello World!' + req.params.id);
 });

 pipe.plug(router);

 pipe.plug(function (error, req, res, ses, hand) {
 print(JSON.stringify(error));
 });*/

application.serve(function (req, res) {
    print('hello ===================== ruchira');
    var r = {
        getRequestURI: function () {
            return '/tomgery/users/1';
        },
        getMethod: function () {
            return 'GET';
        }
    };
    //pipe.resolve(r, res);
    //throw new Error("foo");
});

/*var Log = require('log');
 var log = new Log('hello');
 log.info('ruchira wageesha');*/

require('foo');

