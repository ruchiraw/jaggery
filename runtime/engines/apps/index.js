var application = (function () {
    var fn;
    return {
        serve: function (f) {
            return f ? (fn = f) : fn;
        }
    }
}());

var console = {
    log: function (s) {
        java.lang.System.out.println(s || '');
    }
};

var print = function () {
    console.log('========custom print========');
};


var require = (function (jaggery) {

    var FILENAME = Packages.javax.script.ScriptEngine.FILENAME,
        FileReader = Packages.java.io.FileReader,
        File = Packages.java.io.File,
        SEPARATOR = File.separator,
        BufferedReader = Packages.java.io.BufferedReader,
        format = java.lang.String.format,
        engine = jaggery.get('engine'),
        home = jaggery.get('home'),
        modulesDir = 'jaggery_modules',
        modules = {},
        prefix = '(function(exports,module,require,__filename,__dirname){',
        suffix = '})',
        core = {},
        local = {};

    home = '/Users/ruchira/sources/github/forks/jaggery/runtime';

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
        if (p2.match(/^[.]\//)) {
            return p1 + p2.substring(2);
        }
        if (p2.match(/^[.]{2}\//)) {
            return p1.replace(/\/[^\/]*\/$/, '/') + p2.substring(3);
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
        console.log('loadDir : ' + mod);
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
        var dirs = modulePaths(start);
        var i, path, file,
            length = dirs.length;
        for (i = 0; i < length; i++) {
            path = dirs[i] + SEPARATOR + mod;
            file = loadFile(path);
            if (file) {
                return file;
            }
            file = loadDirectory(path);
            if (file) {
                return file;
            }
        }
        return null;
    };

    var modulePaths = function (start) {
        var parts = start.split(SEPARATOR);
        var root = (root = parts.indexOf(modulesDir)) == -1 ? 0 : root;
        var i = parts.length - 1;
        var dirs = [];
        var dir;
        while (i >= root) {
            if (parts[i] == modulesDir) {
                continue;
            }
            dir = parts.slice(0, i + 1).join(SEPARATOR) + SEPARATOR + modulesDir;
            dirs.push(dir);
            i--;
        }
        console.log(JSON.stringify(dirs));
        return dirs;
    };

    var resolveFile = function (mod) {
        var file,
            parent = engine.get(FILENAME),
            dmod = denormalize(mod);
        parent = '/Users/ruchira/sources/github/forks/jaggery/apps/tomgery/';
        if (mod.match(/^[.]{0,2}[\/]/)) {
            var path = joinPaths(parent, dmod);
            console.log('resolveFile : ' + dmod);
            file = loadFile(path);
            if (file) {
                return file;
            }
            file = loadDirectory(path);
            if (file) {
                return file;
            }
        } else {
            file = loadModules(dmod, dirname(parent));
            if (file) {
                return file;
            }
        }
        throw new Error(format("A module with the '%s' cannot be found at '%s'", mod, parent));
    };

    var require = function (mod) {
        var module = core[mod];
        if (module) {
            return module.exports;
        }
        module = local[mod];
        if (module) {
            return module.exports;
        }
        var file = resolveFile(mod);
        var path = file.getAbsolutePath();
        var ext = extension(path);
        var old = engine.get(FILENAME);

        if (ext === 'json') {
            engine.put(FILENAME, path);
            try {
                module = {
                    id: path,
                    type: 'json',
                    exports: JSON.parse(readFile(file))
                };
                local[path] = module;
                return module;
            } catch (e) {
                throw new Error(format("Error evaluating json '%s'", mod))
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
                    exports: {}
                };
                fn.apply(module.exports, [module.exports, module, require, path, dirname(file)]);
                modules[path] = module;
                return module.exports;
            } catch (e) {
                throw new Error(format("Error evaluating js module '%s'", mod));
            } finally {
                engine.put(FILENAME, old);
            }
        }
        throw new Error(format("Invalid module required '%s'", mod));
    };

    require.resolve = function (mod) {
        return resolveFile(mod).getAbsolutePath();
    };

    return require;

}(jaggery));


//require('./index.js');


var exec = function (options) {
    var index = require('/index.js');
    //var index = require('./index.js');
    console.log(index.a);
    print(); // TODO: why this doesn't see the overridden print
    //index.b();
    //var fn = application.serve();
    //fn(options.request, options.response);
};







