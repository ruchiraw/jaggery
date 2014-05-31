var app = require('app');

var foo = require('foo');
var bar = require('bar');

/**
 * registering request callback
 */
app.serve(function (req, res) {
    res.write(foo('jaggery'));
    res.write('\n');
    res.write(bar.greet('jaggery'));
});

