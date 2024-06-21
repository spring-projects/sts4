import { LanguageModelChatRequestOptions, LanguageModelChatSelector, CancellationToken, Disposable, LanguageModelChatMessage, window, ProgressLocation, lm, LogOutputChannel, LanguageModelChatMessageRole } from "vscode";

export const logger: LogOutputChannel = window.createOutputChannel("Spring tools agent", { log: true });

export default class CopilotRequest {

    public static readonly DEFAULT_MODEL_SELECTOR: LanguageModelChatSelector = { vendor: 'copilot', family: 'gpt-3.5-turbo' };
    public static readonly DEFAULT_MODEL_OPTIONS: LanguageModelChatRequestOptions = { modelOptions: {} };

    public static readonly NOT_CANCELLABLE: CancellationToken = { isCancellationRequested: false, onCancellationRequested: () => Disposable.from() };

    public constructor(
        private readonly systemMessagesOrPrompts: LanguageModelChatMessage[] = [],
        private readonly modelSelector: LanguageModelChatSelector = CopilotRequest.DEFAULT_MODEL_SELECTOR,
        private readonly modelOptions: LanguageModelChatRequestOptions = CopilotRequest.DEFAULT_MODEL_OPTIONS
    ) {
    }

    public async chatRequest(userMessage: LanguageModelChatMessage[], modelOptions: LanguageModelChatRequestOptions = CopilotRequest.DEFAULT_MODEL_OPTIONS, cancellationToken: CancellationToken = CopilotRequest.NOT_CANCELLABLE): Promise<string> {
        const messages = [...this.systemMessagesOrPrompts];
        messages.push(...userMessage);
        logger.info(`Prompt: \n`, messages);
        // console.log(messages[0].content)
        messages.forEach(m => console.log(m.content));

        return window.withProgress({
            location: ProgressLocation.Window,
            title: "Copilot request",
            cancellable: true
        }, async (progress, cancellation) => {
            progress.report({ message: "processing..." });

            if (cancellation.isCancellationRequested) {
                console.log("Chat request cancelled");
                return 'Chat request cancelled';
            }
            try {
                const response = await this.sendRequest(messages, modelOptions, cancellationToken);
                logger.info(`Copilot: \n`, response);
                return response;
            } catch (e) {
                const cause = e.cause || e;
                logger.error(`Failed to chat with copilot`, cause);
                throw cause;
            }
        });
    }

    private async selectModel() {
        const model = (await lm.selectChatModels(this.modelSelector))?.[0];
        if (!model) {
            const models = await lm.selectChatModels();
            throw new Error(`No suitable model, available models: [${models.map(m => m.name).join(', ')}]. Please make sure you have installed the latest "GitHub Copilot Chat" (v0.16.0 or later).`);
        }
        return model;
    }

    private async sendRequest(messages: LanguageModelChatMessage[], modelOptions: LanguageModelChatRequestOptions, cancellationToken: CancellationToken) {
        const response = [];
        const model = await this.selectModel();
        const chatResponse = await model.sendRequest(messages, modelOptions ?? this.modelOptions, cancellationToken);
        for await (const fragment of chatResponse.text) {
            response.push(fragment);
        }
        return response.join('');
    }
}
