var jaggery = global.jaggery;
var engine = jaggery.get('engine');
var writer = jaggery.get('writer');

var print = function (obj) {
    writer.println(obj);
};

module.exports = function (options) {
    return engine.eval(options.get('source'));
};
