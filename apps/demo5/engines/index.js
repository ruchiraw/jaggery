var console = {
    log: function (s) {
        java.lang.System.out.println(s || '');
    }
};

//TODO: get rid of or cache io operations which occurs during require
var require = (function (jaggery) {
    var FILENAME = Packages.javax.script.ScriptEngine.FILENAME,
        FileReader = Packages.java.io.FileReader,
        File = Packages.java.io.File,
        SEPARATOR = File.separator,
        BufferedReader = Packages.java.io.BufferedReader,
        engine = jaggery.get('engine'),
        home = jaggery.get('home'),
        modulesDir = 'jaggery-modules',
        modules = {},
        prefix = '(function(exports,module,require,__filename,__dirname, global){',
        suffix = '})',
        files = {};

    var normalize = function (path) {
        return path.replace(new RegExp(SEPARATOR, 'ig'), '/');
    };

    var denormalize = function (path) {
        return path.replace(/\//ig, SEPARATOR);
    };

    /**
     * @param p1 system dependent path /a/b/ or c:\a\b\
     * @param p2 a.js or ./a.js or ../a.js or /a.js
     * @returns {*}
     */
    var joinPaths = function (p1, p2) {
        //TODO: replace this with a proper implementation
        if (!p1.match(/[\\/]$/)) {
            p1 += SEPARATOR;
        }
        if (p2.match(/^[.]\//)) {
            return p1 + p2.substring(2);
        }
        if (p2.match(/^[.]{2}\//)) {
            return p1.replace(/[\\/][^\\/]*\/$/, SEPARATOR) + p2.substring(3);
        }
        if (p2.match(/^\//)) {
            return p1 + p2.substring(1);
        }
        return p1 + p2;
    };

    var dirname = function (file) {
        return ((file instanceof File) ? file : new File(file)).getParent(); //TODO: rewrite this with a regex
    };

    var extension = function (path) {
        var parts = path.match(/[.][^.\\/]*$/ig);
        return parts ? parts[0].substring(1) : null;
    };

    var readFile = function (file) {
        var reader = new BufferedReader(new FileReader(file));
        var line,
            content = '';
        while (( line = reader.readLine()) !== null) {
            content += line + '\n';
        }
        reader.close();
        return content;
    };

    var loadFile = function (mod) {
        console.log('loadFile : ' + mod);
        var file = new File(mod);
        if (file.exists() && !file.isDirectory()) {
            return file;
        }
        file = new File(mod + '.js');
        if (file.exists() && !file.isDirectory()) {
            return file;
        }
        return null;
    };

    var loadDirectory = function (mod) {
        console.log('loadDirectory : ' + mod);
        var file = new File(mod);
        if (!file.isDirectory()) {
            return null;
        }
        file = new File(mod + SEPARATOR + 'package.json');
        if (file.exists()) {
            var pkg = JSON.parse(readFile(file));
            return loadFile(mod + SEPARATOR + denormalize(pkg.main));
        }
        file = new File(mod + SEPARATOR + 'index.js');
        if (file.exists()) {
            return file;
        }
        return null;
    };

    var loadModules = function (mod, start) {
        var path, file,
            dirs = start.split(SEPARATOR),
            i = dirs.length - 1;
        while (i > 0) {
            path = dirs.slice(0, i + 1).join(SEPARATOR);
            console.log('dir ' + dirs[i]);
            console.log('path : ' + path);
            if (dirs[i] !== modulesDir) {
                path += SEPARATOR + modulesDir;
            }
            path += SEPARATOR + mod;
            file = loadFile(path);
            if (file) {
                return file;
            }
            file = loadDirectory(path);
            if (file) {
                return file;
            }
            i--;
        }
        return null;
    };

    var cache = function (current, mod, path) {
        var o,
            obj = files[current] || (files[current] = {});
        if (!path) {
            o = obj[mod];
            return o ? o.path : null;
        }
        obj[mod] = {
            path: path
        };
        return path;
    };

    var requirer = function (current, resolver, global) {
        var require = function (mod) {
            console.log('required module : ' + mod);
            //TODO: implement core module caching
            var path = require.resolve(mod);
            console.log('resolved path for module : ' + path);
            var module = modules[path];
            if (module) {
                console.log('cached module : ' + mod + ' loaded from : ' + path);
                return module.exports;
            }
            var file = new File(path);
            var ext = extension(path);
            var old = engine.get(FILENAME);
            //TODO: properly handle json and js not found issues, invalid content issues etc.
            if (ext === 'json') {
                engine.put(FILENAME, path);
                try {
                    module = {
                        id: path,
                        type: 'json',
                        exports: JSON.parse(readFile(file))
                    };
                    modules[path] = module;
                    return module.exports;
                } finally {
                    engine.put(FILENAME, old);
                }
            }
            if (ext === 'js') {
                engine.put(FILENAME, path);
                try {
                    var fn = engine.eval(prefix + readFile(file) + suffix);
                    module = {
                        id: path,
                        type: 'js',
                        exports: {},
                        require: requirer(path, resolver)
                    };
                    console.log('evaluating module : ' + mod + ' with ' + path);
                    fn.apply(module.exports, [module.exports, module, module.require, path, dirname(file), global]);
                    modules[path] = module;
                    return module.exports;
                } finally {
                    engine.put(FILENAME, old);
                }
            }
            throw new Error('Invalid module required ' + mod);
        };
        require.resolve = resolver(current);
        return require;
    };

    var resolver = function (current) {
        return function (mod) {
            console.log('resolving module : ' + mod);
            var file, module, path, parent, dmod;
            console.log('parent dir : ' + current);
            path = cache(current, mod);
            if (path) {
                return path;
            }
            parent = dirname(current);
            dmod = denormalize(mod);
            if (mod.match(/^[.]{0,2}[\/]/)) {
                path = joinPaths(parent, dmod);
                console.log('joined path : ' + path);
                file = loadFile(path);
                if (file) {
                    return cache(current, mod, file.getAbsolutePath());
                }
                file = loadDirectory(path);
                if (file) {
                    return cache(current, mod, file.getAbsolutePath());
                }
            } else {
                file = loadModules(dmod, parent);
                if (file) {
                    return cache(current, mod, file.getAbsolutePath());
                }
            }
            throw new Error('A module with the name ' + mod + ' cannot be found at ' + parent);
        };
    };

    return requirer(engine.get(FILENAME), resolver, {
        requirer: requirer,
        resolver: resolver,
        jaggery: jaggery
    });

}(jaggery));

var exec = require('./' + jaggery.get('name'));



