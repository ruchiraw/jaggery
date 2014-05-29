
var io = require('io');
//var app = require('app');
var JAGGERY_CONFIG = 'jaggery.conf';
var JAGGERY_CONFIG_PATH = '/' + JAGGERY_CONFIG;
var jaggery = global.jaggery;
var context = jaggery.get('context');
var config = context.getResourceAsStream(JAGGERY_CONFIG_PATH);
if (!config) {
    throw new Error(JAGGERY_CONFIG + ' file cannot be found at ' + JAGGERY_CONFIG_PATH +
        ' for the Jaggery App : ' + context.getContextPath());
}

try {
    config = JSON.parse(io.streamgify(config));
} catch (e) {
    console.log('Error while parsing ' + JAGGERY_CONFIG + ' at ' + JAGGERY_CONFIG_PATH +
        ' for the Jaggery App : ' + context.getContextPath());
    throw e;
}

var main = context.getRealPath('/') + '/' + (config.main || 'index.js');
var requir = global.requirer(global.resolver(main));
var app = requir('app');
requir('/index');

module.exports = function (options) {
    //jaggery.get('engine').put(Packages.javax.script.ScriptEngine.FILENAME, '/Users/ruchira/sources/github/forks/jaggery/apps/tomgery/index.js');
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







