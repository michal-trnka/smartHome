var awsIot = require('aws-iot-device-sdk');
var config = require('./configuration.json');

var device = awsIot.device({
    keyPath: config.privKey,
    certPath: config.certificate,
    clientId: config.clientId,
    host: config.endpoint,
    caPath: config.rootCa
});

device.publish("temperature","25");

console.log(config.certificate);