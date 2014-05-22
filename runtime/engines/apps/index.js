jaggery.get('engine').put(Packages.javax.script.ScriptEngine.FILENAME, '/Users/ruchira/sources/github/forks/jaggery/apps/tomgery/index.js');

var app = require('app');
require('/index');

module.exports = function (options) {
    var fn = app.serve();
    var req = {
        getRequestURI: function () {
            return options.get('request').getRequestURI();
        },
        getContextPath: function () {
            return options.get('request').getContextPath();
        },
        getMethod: function () {
            return options.get('request').getMethod();
        }
    };

    var res = {
        write: function (s) {
            options.get('response').getOutputStream().print(s);
        }
    };

    fn.call(app, req, res);
};







