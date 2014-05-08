var require = (function (reader) {

    return function (module) {
        var r = reader.getReader(module);
        var ch;
        var s = '';
        while ((ch = r.read()) != -1) {
            s += new java.lang.Character(ch);
        }
        //print(s);
        eval(s);
        //print(s);
    };
}(reader));

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