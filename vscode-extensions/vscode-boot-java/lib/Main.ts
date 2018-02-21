'use strict';

import * as VSCode from 'vscode';

/** Called when extension is activated */
export function activate(context: VSCode.ExtensionContext) {
    VSCode.window.showInformationMessage(
        "The `vscode-boot-java` extension is obsolete and no longer functional. "+
        "Please uninstall it and install the `vscode-spring-boot` extension instead."
    );
}
