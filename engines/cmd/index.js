var jaggery = global.jaggery;
var engine = jaggery.get('engine');
var writer = jaggery.get('writer');

var File = Packages.java.io.File;
var SEPARATOR = File.separator;
var path = new File('').getAbsolutePath() + SEPARATOR + '/index.js';

var print = function (obj) {
    writer.println(obj);
};

var requir = global.requirer(path, global.resolver);

engine.put('print', print);
engine.put('require', requir);

module.exports = function (options) {
    try {
        engine.eval(options.get('source'));
    } catch (e) {
        print(e.message);
    }
};
