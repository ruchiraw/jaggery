var bar = require('bar');

module.exports = function (name) {
    var m1 = 'hello ' + name + ' : by foo';
    var m2 = bar.greet('jaggery');
    return m1 + '\n' + m2;
};