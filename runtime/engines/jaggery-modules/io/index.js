var StringWriter = Packages.java.io.StringWriter;
var BufferedReader = Packages.java.io.BufferedReader;
var InputStreamReader = Packages.java.io.InputStreamReader;
var Array = Packages.java.lang.reflect.Array;
var Character = Packages.java.lang.Character;
var File = Packages.java.io.File;

exports.streamgify = function (is) {
    if (!is) {
        return '';
    }
    var writer = new StringWriter();
    var buffer = Array.newInstance(Character.TYPE, 1024);
    try {
        var reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        var n;
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }
    } finally {
        is.close();
    }
    return writer.toString();
};

exports.current = function() {
    return new File('').getAbsolutePath();
};