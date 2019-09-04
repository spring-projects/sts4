/********************************************************************************
 * Copyright (c) 2017-2018 TypeFox and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 ********************************************************************************/


declare const acquireVsCodeApi: any;

import {
    TYPES, IActionDispatcher,
    ModelSource, WebSocketDiagramServer, RequestModelAction
} from "sprotty";
import createContainer, {TransportMedium} from "./di.config";
import * as SockJS from "sockjs-client";
import {VSCodeWebViewDiagramServer} from "./model";

export default function runStandalone() {
    const clientId = getOptionFromDom('client-id') || 'sprotty-client';
    const transport = getOptionFromDom('transport') || 'websocket';
    const container = createContainer(transport=='websocket' ? TransportMedium.Websocket : TransportMedium.LSP, clientId);
    const dispatcher = container.get<IActionDispatcher>(TYPES.IActionDispatcher);

    // Run
    const modelSource = container.get<ModelSource>(TYPES.ModelSource);

    if (modelSource instanceof WebSocketDiagramServer) {
        const ws = new SockJS('/websocket');
        modelSource.listen(ws);
        ws.addEventListener('open', () => {
            dispatcher.dispatch(requestModelAction());
        });
        ws.addEventListener('error', (event) => {
            console.error(`WebSocket Error: ${event}`)
        })
    }

    if (modelSource instanceof VSCodeWebViewDiagramServer) {
        modelSource.listen(acquireVsCodeApi());
        dispatcher.dispatch(requestModelAction());
    }

    // Button features
    const refreshButton = document.getElementById('refresh');
    if (refreshButton) {
        refreshButton.addEventListener('click', () => {
            dispatcher.dispatch(requestModelAction());
        });
    }

    function getOptionFromDom(att: string) : string | null {
        const appDiv = document.getElementById('sprotty-app');
        if (appDiv) {
            return appDiv.getAttribute(att);
        }
        return null;
    }

    function getTarget() : string {
        const input = <HTMLInputElement>document.getElementById('target-url');
        const target = input && input.value;
        return target || /*'target-missing'*/'http://localhost:8080/integration';
    }

    function requestModelAction() : RequestModelAction {
        return new RequestModelAction({ 'target' : getTarget()});
    }
}
