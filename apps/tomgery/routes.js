
var router = require('router');


router.app.get('/users/:id', function (req, res) {
    //print('Hello World!' + req.params.id);
    var x = new Date().getTime() * new Date().getTime();
    res.write('hello world');
});
