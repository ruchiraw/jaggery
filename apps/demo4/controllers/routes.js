var router = require('router');

/**
 * route definition for users
 */
router.app.get('/users/:id', function (req, res) {
    res.write('hello user ' + req.params.id);
});

/**
 * route definition for apps
 */
router.app.get('/apps/:id', function (req, res) {
    res.write('hello app ' + req.params.id);
});
