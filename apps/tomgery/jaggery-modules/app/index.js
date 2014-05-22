var serve;

module.exports.serve = function (fn) {
    return fn ? (serve = fn) : serve;
};
