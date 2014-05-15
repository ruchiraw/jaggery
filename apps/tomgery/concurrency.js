var ScriptEngineManager = Packages.javax.script.ScriptEngineManager;

var Executors = Packages.java.util.concurrent.Executors;
var TimeUnit = Packages.java.util.concurrent.TimeUnit;
var Callable = Packages.java.util.concurrent.Callable;
var System = Packages.java.lang.System;
var StringBuilder = Packages.java.lang.StringBuilder;

var ArrayList = java.util.ArrayList;

var manager = new ScriptEngineManager();
var engine = manager.getEngineByName('js');

var script = new StringBuilder("i = 0;")
    .append("i += 1;")
    .append("shortly_later = new Date()/1000 + Math.random;") // 0..1 sec later
    .append("while( (new Date()/1000) < shortly_later) { Math.random() };") //prevent optimizations
    .append("i += 1;")
    .toString();

var onePlusOne = engine.compile(script);

var addition = new Callable({
    call: function () {
        try {
            //return onePlusOne.eval();
            var fn = function() {
                m = 0;
                m += 1;
                shortly_later = new Date()/1000 + Math.random;
                while( (new Date()/1000) < shortly_later) { Math.random() };
                m += 1;
                return m;
            };
            return fn();
        }
        catch (e) {
            throw new Error(e);
        }
    }
});

var executor = Executors.newCachedThreadPool();
var results = new ArrayList();

for (var i = 0; i < 50; i++) {
    results.add(executor.submit(addition));
}

var miscalculations = 0;
for (var i = 0; i < results.size(); i++) {
    var jsResult = results.get(i).get();

    if (jsResult != 2) {
        System.out.println("Incorrect result from js, expected 1 + 1 = 2, but got " + jsResult);
        miscalculations += 1;
    }
}

executor.awaitTermination(1, TimeUnit.SECONDS);
executor.shutdownNow();

System.out.println("Overall: " + miscalculations + " wrong values for 1 + 1.");