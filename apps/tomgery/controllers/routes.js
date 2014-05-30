var router = require('router');

router.app.get('/users/:id', function (req, res) {
    //print('Hello World!' + req.params.id);
    var x = new Date().getTime() * new Date().getTime();
    require('./foo');
    require('router');
    res.write('hello user');
});

router.app.get('/apps/:id', function (req, res) {
    res.write('hello app');
});
