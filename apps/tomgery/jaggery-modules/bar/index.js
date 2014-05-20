var LogFactory = Packages.org.apache.commons.logging.LogFactory;

var Log = function (logger) {
    this.log = LogFactory.getLog(logger);
};

Log.prototype.info = function (s) {
    this.log.info(s);
};

Log.prototype.warn = function (s) {
    this.log.warn(s);
};

Log.prototype.error = function (s) {
    this.log.error(s);
};

Log.prototype.fatal = function (s) {
    this.log.fatal(s);
};

Log.prototype.debug = function (s) {
    this.log.debug(s);
};

module.exports = Log;

print('------------bar');