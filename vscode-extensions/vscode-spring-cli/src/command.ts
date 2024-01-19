import { Uri, window } from "vscode";
import { enterText } from "./utils";
import { CLI } from "./extension";

export async function handleCommandAdd() {
    let url;
    try {
        url = await enterText({
            title: "URL",
            prompt: "Enter Repository URL",
            placeholder: "https://github.com/my-org/my-command",
            validate: async v => {
                try {
                    Uri.parse(v, true);
                } catch (error) {
                    return "Invalid URL value";
                }
            }
        });
    } catch (error) {
        // Cancelled - ignore the error
    }

    if (url) {
        CLI.commandAdd({url});
    }
}

export async function handleCommandRemove() {
    window.showErrorMessage("Not Implemented!");
}

export async function handleCommandNew() {
    window.showErrorMessage("Not Implemented!");
}