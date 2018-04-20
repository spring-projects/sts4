import { injectable, inject } from "inversify";
import { CommandContribution, CommandRegistry, MenuContribution, MenuModelRegistry, MessageService } from "@theia/core/lib/common";
import { CommonMenus } from "@theia/core/lib/browser";

export const CfManifestYamlCommand = {
    id: 'CfManifestYaml.command',
    label: "Shows a message"
};

@injectable()
export class CfManifestYamlCommandContribution implements CommandContribution {

    constructor(
        @inject(MessageService) private readonly messageService: MessageService,
    ) { }

    registerCommands(registry: CommandRegistry): void {
        registry.registerCommand(CfManifestYamlCommand, {
            execute: () => this.messageService.info('Hello World!')
        });
    }
}

@injectable()
export class CfManifestYamlMenuContribution implements MenuContribution {

    registerMenus(menus: MenuModelRegistry): void {
        menus.registerMenuAction(CommonMenus.EDIT_FIND, {
            commandId: CfManifestYamlCommand.id,
            label: 'Say Hello'
        });
    }
}