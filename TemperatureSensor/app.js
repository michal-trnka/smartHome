const awsIot = require('aws-iot-device-sdk');
const fs = require("fs");
const path = require("path");
require('console-stamp')(console, '[HH:MM:ss.l]');

const CONFIG = require('./configuration.json');
const DATA_DIR_PREFIX = "28-";
const SENSOR_PRECISION = 1000;

let extractTemperatureFromFile = fileName => {
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


let getDataFilePath = (directoryPath, fileName) => {
    let dirContent = fs.readdirSync(directoryPath);

    //temperature measurments are located in a specific subdir with dir starting with "28"
    let sensorDirName = dirContent.filter(elem => {
        return elem.startsWith(DATA_DIR_PREFIX);
    });
    directoryPath = directoryPath + path.sep + sensorDirName;
    //set up file with temperature
    return path.format({
        dir: directoryPath,
        base: fileName
    });
};

console.log("Getting data file path");
let filePath = getDataFilePath(CONFIG.pathLocation, CONFIG.dataFileName);
console.log(`Data file path is: ${filePath}`);

console.log("Setting up AWS connection");
//set up AWS device
let device = awsIot.device({
    keyPath: CONFIG.privKey,
    certPath: CONFIG.certificate,
    clientId: CONFIG.clientId,
    host: CONFIG.endpoint,
    caPath: CONFIG.rootCa
});

console.log("Starting periodical function to poll the temperature");
setInterval(() => {
    let temperature = extractTemperatureFromFile(filePath);
    if (temperature === false) {
        console.warn("No temperature found");
        return;
    }
    console.log(`Temperature is: ${temperature}`);
    device.publish("temperature",JSON.stringify({
        temperature: temperature,
        time: new Date().getTime(),
    }));
}, 60000);
