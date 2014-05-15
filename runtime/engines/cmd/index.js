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

var print = (function (jaggery) {
    var writer = jaggery.get('writer'),
        sep = jaggery.get('separator');
    return function (obj) {
        writer.println(obj);
    };
}(jaggery));

var exec = (function (jaggery) {
    var engine = jaggery.get('engine');
    return function (options) {
        return engine.eval(options.get('source'));
    };
}(jaggery));