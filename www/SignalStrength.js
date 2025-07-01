
var exec = require('cordova/exec');

exports.getCellDbm = function(success, error) {
    exec(success, error, 'SignalStrength', 'getCellDbm', []);
};

exports.getWifiDbm = function(success, error) {
    exec(success, error, 'SignalStrength', 'getWifiDbm', []);
};
