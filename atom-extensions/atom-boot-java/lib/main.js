const {AutoLanguageClient} = require('atom-languageclient');

class BootJavaLanguageClient extends AutoLanguageClient {

    constructor() {
        super();
    }

    getGrammarScopes() {
        return [];
    }

    getLanguageName() {
        return 'boot-java';
    }

    getServerName() {
        return 'Spring Boot';
    }

    activate() {
        const notification = atom.notifications.addInfo('`boot-java` Extension __NOT__ Functional', {
            dismissable: true,
            detail: '`boot-java` extension starting from 0.1.4 is obsolete',
            description: 'The `boot-java` extension is obsolete and no longer functional. Please uninstall it and install the `spring-boot` extension instead.',
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


module.exports = new BootJavaLanguageClient();
