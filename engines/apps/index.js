var require = (function (jaggery) {

    var engine = jaggery.get('engine'),
        reader = jaggery.get('reader');

    var FILENAME = javax.script.ScriptEngine.FILENAME;

    return function (module) {
        var script = reader.getScript(module);
        var r = script.getReader();
        var ch;
        var s = '';
        while ((ch = r.read()) != -1) {
            s += new java.lang.Character(ch);
        }
        var old = engine.get(FILENAME);
        engine.put(FILENAME, script.getId());
        try {
            engine.eval(s);
        } finally {
            engine.put(FILENAME, old);
        }
    };
}(jaggery));

var application = (function () {
    var fn;
    return {
        serve: function (f) {
            return f ? (fn = f) : fn;
        }
    }
}());

require('/index.js');


var exec = function (options) {
    //print(require);
    var fn = application.serve();
    fn(options.request, options.response);
    //require('/jaggery.conf');
    /*for (var option in options) {
     //print(option);
     }*/
};