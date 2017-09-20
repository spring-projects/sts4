const fs = require('fs');
const path = require('path');
const download = require('download');
const PROPERTIES = require('./properties.json');

let fileExists = function(path) {
    return new Promise((resolve, reject) => {
            fs.access(path, fs.R_OK, error => {
            resolve(!error || error.code !== 'ENOENT');
        })
    });
};

const serverDownloadUrl = PROPERTIES.jarUrl;

const serverHome = path.join(__dirname, 'server');

const localFileName = path.join(serverHome, 'boot-java-language-server.jar');

fileExists(localFileName).then(exists => {
   if (!exists) {
       console.log(`Downloading ${serverDownloadUrl} to ${localFileName}`);
       fileExists(serverHome)
           .then(doesExist => { if (!doesExist) fs.mkdir(serverHome) })
           .then(() => download(serverDownloadUrl))
           .then(data => fs.writeFileSync(localFileName, data))
           .then(() => fileExists(localFileName))
           .then(doesExist => { if (!doesExist) throw Error(`Failed to install the ${this.getServerName()} language server`) })
           .then(() => console.log(`Successfully downloaded ${serverDownloadUrl}`));
   }
});



