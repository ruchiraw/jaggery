var app = require('app');

/**
 * registering request callback
 */
app.serve(function (req, res) {
    res.write('hello jaggery');
});

