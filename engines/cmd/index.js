var jaggery = global.jaggery;
var engine = jaggery.get('engine');
var writer = jaggery.get('writer');

var File = Packages.java.io.File;
var SEPARATOR = File.separator;
var prefix = '(function(print, require){';
var suffix = '})';

var print = function (obj) {
    writer.println(obj);
};

var requir = global.requirer(new File('').getAbsolutePath() + SEPARATOR + '/index.js', global.resolver);

module.exports = function (options) {
    var fn = engine.eval(prefix + options.get('source') + suffix);
    fn(print, requir);
};
