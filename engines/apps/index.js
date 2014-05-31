var io = require('io');

var JAGGERY_CONFIG = 'jaggery.conf';
var JAGGERY_CONFIG_PATH = '/' + JAGGERY_CONFIG;
var builtins = ['app', 'jag'];
var builtin = function (mod) {
    return builtins.indexOf(mod) !== -1;
};

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

var main = config.main || 'index.js';
var current = context.getRealPath('/') + '/' + main;

var requir = global.requirer(current, function (curr) {
    return function (mod) {
        if (!builtin(mod)) {
            try {
                return global.resolver(curr)(mod);
            } catch (e) {
                //ignore this, this means, local overriding hasn't found
            }
        }
        return global.resolver(__filename)(mod);
    };
}, global);

//these are required to initialize the app and jag modules
var app = requir('app');
var jag = requir('jag');

requir('./' + main);

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







