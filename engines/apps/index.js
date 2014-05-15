var application = (function () {
    var fn;
    return {
        serve: function (f) {
            return f ? (fn = f) : fn;
        }
    }
}());

print('---------1');

var require = (function (jaggery, application) {

    var engine = jaggery.get('engine'),
        reader = jaggery.get('reader'),
        modules = {};

    var FILENAME = javax.script.ScriptEngine.FILENAME;

    var resolvePath = function (id) {
        return id;
    };

    return function (id) {
        var path = resolvePath(id);
        var module = modules[path];
        if (module) {
            return module.exports;
        }
        var script = reader.getScript(path);
        var r = script.getReader();
        var ch;
        var source = '';
        while ((ch = r.read()) != -1) {
            source += new java.lang.Character(ch);
        }
        var old = engine.get(FILENAME);
        engine.put(FILENAME, script.getId());
        try {
            var fn = engine.eval('(function(exports, require, module, filename, application) {' + source + '\n})');
            //var fn = new Function('exports', 'require', 'module', 'filename', 'application', source);
            module = {
                exports: {}
            };
            fn.call({}, module.exports, require, module, script.getId(), application);
            modules[path] = module;
            return module.exports;
        } finally {
            engine.put(FILENAME, old);
        }
    };
}(jaggery, application));

require('/index.js');


var exec = function (options) {
    var index = require('/index.js');
    print(index.a);
    index.b();
    var fn = application.serve();
    fn(options.request, options.response);
};