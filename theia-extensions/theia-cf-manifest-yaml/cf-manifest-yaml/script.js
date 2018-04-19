const fs = require('fs');
const path = require('path');
const url = require('url');
const glob = require('glob');
const download = require('download');
const PROPERTIES = require('./properties.json');

let fileExists = function(path) {
    return new Promise((resolve, reject) => {
            fs.access(path, fs.R_OK, error => {
                resolve(!error || error.code !== 'ENOENT');
        })
    });
};

const serverHome = path.join(__dirname, 'server');
const filePaths = glob.sync('manifest-yaml-language-server*.jar', { cwd: serverHome });

if (filePaths.length === 0) {
    const serverDownloadUrl = PROPERTIES.jarUrl;
    const fileName = path.basename(url.parse(serverDownloadUrl).pathname);
    const localFileName = path.join(serverHome, fileName.startsWith('manifest-yaml-language-server') ? fileName : 'manifest-yaml-language-server.jar');
    console.log(`Downloading ${serverDownloadUrl} to ${localFileName}`);
    fileExists(serverHome)
        .then(doesExist => { if (!doesExist) fs.mkdir(serverHome) })
        .then(() => download(serverDownloadUrl))
        .then(data => fs.writeFileSync(localFileName, data))
        .then(() => fileExists(localFileName))
        .then(doesExist => { if (!doesExist) throw Error(`Failed to install the ${this.getServerName()} language server`); })
        .then(() => console.log(`Successfully downloaded ${serverDownloadUrl}`));
}



