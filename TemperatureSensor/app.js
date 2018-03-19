const awsIot = require('aws-iot-device-sdk');
const fs = require("fs");
const path = require("path");

const CONFIG = require('./configuration.json');
const DATA_DIR_PREFIX = "28-";
const SENSOR_PRECISION = 1000;

let extractTemperatureFromFile = function (fileName) {
    let content = fs.readFileSync(fileName, "utf8");
    content = content.split(/\r?\n/);
    content = content.filter(line => {
        //filter empty lines, empty lines are converted to false by JS
        return line;
    });
    if (content.length < 2) {
        return false;
    }
    if (content[0].slice(-3) !== "YES") {
        return false;
    }
    let temperature = content[1].slice(content[1].indexOf("t=") + 2);
    return temperature / SENSOR_PRECISION;
};

//set up file with temperature
let dirContent = fs.readdirSync(CONFIG.pathLocation);
//temperature measurments are located in a specific subdir with dir starting with "28"
let sensorDirName = dirContent.filter(elem => {
    return elem.startsWith(DATA_DIR_PREFIX);
});
let directoryPath = CONFIG.pathLocation + "\\" + sensorDirName;
let filePath = path.format({
    dir: directoryPath,
    base: CONFIG.dataFileName
});

//set up AWS device
let device = awsIot.device({
    keyPath: CONFIG.privKey,
    certPath: CONFIG.certificate,
    clientId: CONFIG.clientId,
    host: CONFIG.endpoint,
    caPath: CONFIG.rootCa
});

setInterval(async function () {
    let temperature = extractTemperatureFromFile(filePath);
    if (temperature === false) {
        return;
    }
    device.publish("temperature",temperature.toString())
}, 60000);
