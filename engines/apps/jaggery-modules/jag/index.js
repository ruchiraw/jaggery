var io = require('io');

var ByteArrayInputStream = Packages.java.io.ByteArrayInputStream;
var ByteArrayOutputStream = Packages.java.io.ByteArrayOutputStream;
var InputStreamReader = Packages.java.io.InputStreamReader;
var PrintStream = Packages.java.io.PrintStream;
var StringBuilder = Packages.java.lang.StringBuilder;
var Character = Packages.java.lang.Character;

var jaggery = global.jaggery;
var engine = jaggery.get('engine');
var context = jaggery.get('context');
var contextPath = context.getContextPath();
var length = contextPath.length();

var path = function (req) {
    var path = req.getRequestURI();
    return path.substring(length);
};

var cast = function (ch) {
    return Character.valueOf(ch);
};

var code = function (ch) {
    return (ch == -1) ? ch : String.fromCharCode(ch);
};

var parse = function (stream) {
    try {
        var opened = false;
        var isExpression = false;
        var str;
        var output = new ByteArrayOutputStream();
        var source = new PrintStream(output);
        var html = new StringBuilder();
        var jsExp = new StringBuilder();
        var inputReader = new InputStreamReader(stream, "utf-8");
        var ch = code(inputReader.read());
        while (ch != -1) {
            if (ch == '<') {
                ch = code(inputReader.read());
                if (ch == '%') {
                    opened = true;
                    str = html.toString();
                    //as it is html, we can avoid adding empty print("") calls
                    if (!str.equals("")) {
                        source.append("print(\"").append(str).append("\");");
                        html = new StringBuilder();
                    }
                    ch = code(inputReader.read());
                    if (ch == '=') {
                        isExpression = true;
                    } else {
                        continue;
                    }
                } else {
                    if (opened) {
                        if (isExpression) {
                            jsExp.append("<");
                        } else {
                            source.append("<");
                        }
                    } else {
                        html.append('<');
                    }
                    continue;
                }
                ch = code(inputReader.read());
            } else if (ch == '%') {
                ch = code(inputReader.read());
                if (ch == '>') {
                    opened = false;
                    if (isExpression) {
                        isExpression = false;
                        //if it need, we can validate "jsExp" here or let the compiler to do it.
                        source.append("print(").append(jsExp).append(");");
                        jsExp = new StringBuilder();
                    }
                } else {
                    if (opened) {
                        source.append('%');
                    } else {
                        html.append('%');
                    }
                    continue;
                }
                ch = code(inputReader.read());
            } else {
                if (opened) {
                    if (isExpression) {
                        jsExp.append(cast(ch));
                    } else {
                        source.append(cast(ch));
                    }
                    ch = code(inputReader.read());
                } else {
                    var next = code(inputReader.read());
                    if (ch == '"') {
                        html.append('\\').append('\"');
                    } else if (ch == '\\') {
                        html.append('\\').append('\\');
                    } else if (ch == '\r') {
                        html.append('\\').append('r');
                    } else if (ch == '\n') {
                        source.append("print(\"").append(html.toString());
                        if (next != -1) {
                            source.append('\\').append('n');
                        }
                        source.append("\");").append('\n');
                        html = new StringBuilder();
                    } else if (ch == '\t') { // Not sure we need this
                        html.append('\\').append('t');
                    } else {
                        html.append(cast(ch));
                    }
                    ch = next;
                }
            }
        }
        str = html.toString();
        if (!str.equals("")) {
            source.append("print(\"").append(str).append("\");");
        }
        str = jsExp.toString();
        if (!str.equals("")) {
            source.append("print(").append(str).append(");");
        }
        return new ByteArrayInputStream(output.toByteArray());
    } catch (e) {
        throw new Error(e);
    }
};

exports.serve = function (req, res) {
    var is = context.getResourceAsStream(path(req));
    if (!is) {
        //TODO: send proper error codes
        return;
    }
    var source = io.streamgify(parse(is));
    var prefix = '(function(req, res) { var print = function(content) { res.write(content); };';
    var suffix = '})';
    var fn = engine.eval(prefix + source + suffix);
    fn.apply({}, [req, res]);
};