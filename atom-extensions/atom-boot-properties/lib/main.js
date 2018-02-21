const {AutoLanguageClient} = require('atom-languageclient');

class BootPropertiesClient extends AutoLanguageClient {

    constructor() {
        super();
    }

    getGrammarScopes() {
        return [];
    }

    getLanguageName() {
        return 'Boot-Properties';
    }

    getServerName() {
        return 'Boot-Properties';
    }

    activate() {
        const notification = atom.notifications.addInfo('`boot-properties` Extension __NOT__ Functional', {
            dismissable: true,
            detail: '`boot-properties` extension starting from 0.1.4 is obsolete',
            description: 'The `boot-properties` extension is obsolete and no longer functional. Please uninstall it and install the `spring-boot` extension instead.',
            buttons: [{
                text: 'OK',
                onDidClick: () => {
                    notification.dismiss();
                }
            }]
        });
        super.activate();
    }

}

module.exports = new BootPropertiesClient();
