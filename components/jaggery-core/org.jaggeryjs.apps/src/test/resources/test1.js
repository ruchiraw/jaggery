var require = (function (reader) {

    return function (module) {
        var r = reader.getReader(module);
        var ch;
        var s = '';
        while((ch = r.read()) != -1) {
            s += new java.lang.Character(ch);
        }
        //print(s);
    };
}(reader));

var application = {
    serve: function(fn) {

    }
};


var exec = function (options) {
    //print(require);
    require('/Users/ruchira/sources/github/jaggery/components/jaggery-core/org.jaggeryjs.jaggery.core/pom.xml');
    for (var option in options) {
        //print(option);
    }
};