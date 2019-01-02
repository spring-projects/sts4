import {JavaExtensionContribution} from "@theia/java/lib/node";
import * as path from 'path';
import { injectable } from 'inversify';

@injectable()
export class BootJavaExtension implements JavaExtensionContribution {

    getExtensionBundles() {
        const jarFolderPath = path.resolve(__dirname, '../../jars');
        return [
            path.resolve(jarFolderPath, 'jdt-ls-commons.jar'),
            path.resolve(jarFolderPath, 'jdt-ls-extension.jar')
        ];
    }

}