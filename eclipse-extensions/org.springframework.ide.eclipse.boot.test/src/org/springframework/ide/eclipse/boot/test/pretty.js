/*global require console*/
var fs = require('fs');
var file = fs.readFileSync("initializr-v2.1.json", "utf8");
var obj = JSON.parse(file);
console.log(JSON.stringify(obj, null, "   "));