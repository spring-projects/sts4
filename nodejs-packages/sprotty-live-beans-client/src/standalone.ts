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
    TYPES, IActionDispatcher, SModelElementSchema, SEdgeSchema, SNodeSchema, SGraphSchema,
    ModelSource, LocalModelSource, WebSocketDiagramServer, RequestModelAction
} from "sprotty";
import createContainer, {TransportMedium} from "./di.config";
import * as SockJS from "sockjs-client";
import {VSCodeWebViewDiagramServer} from "./model";

export default function runStandalone(clientId: string) {
    const container = createContainer(TransportMedium.LSP, clientId);
    const dispatcher = container.get<IActionDispatcher>(TYPES.IActionDispatcher);

    // Initialize gmodel
    const node0 = {
        id: 'node0', type: 'node:bean', position: {x: 100, y: 100}, size: {width: 80, height: 80},
        layout: 'vbox',

        children: [
            {
                id: 'node0_header',
                type: 'compartment',
                layout: 'hbox',
                children: [
                    {
                        id: 'bean-name',
                        type: 'node:label',
                        text: 'SpringBootApplication'
                    }
                ]
            }
        ]
    };

    // const children_for_node = [];


    const graph: SGraphSchema = {id: 'graph', type: 'graph', children: [node0]};

    let count = 2;

    function addNode(): SModelElementSchema[] {
        const newNode: SNodeSchema = {
            id: 'node' + count,
            type: 'node:bean',
            position: {
                x: Math.random() * 1024,
                y: Math.random() * 768
            },
            size: {
                width: 80,
                height: 80
            }
        };
        const newEdge: SEdgeSchema = {
            id: 'edge' + count,
            type: 'edge:straight',
            sourceId: 'node0',
            targetId: 'node' + count++
        };
        return [newNode, newEdge];
    }

    for (let i = 0; i < 10; ++i) {
        const newElements = addNode();
        for (const e of newElements) {
            graph.children.splice(0, 0, e);
        }
    }

    // Run
    const modelSource = container.get<ModelSource>(TYPES.ModelSource);
    console.log('Model source: ' + modelSource);
    if (modelSource instanceof LocalModelSource) {
        (<LocalModelSource>modelSource).setModel(graph);
    }

    if (modelSource instanceof WebSocketDiagramServer) {
        const ws = new SockJS('http://localhost:8080/websocket');
        modelSource.clientId = 'spring-boot';
        modelSource.listen(ws);
        ws.addEventListener('open', () => {
            dispatcher.dispatch(new RequestModelAction());
        });
        ws.addEventListener('error', (event) => {
            console.error(`WebSocket Error: ${event}`)
        })
    }

    if (modelSource instanceof VSCodeWebViewDiagramServer) {
        console.log('Listen and acquire VSCode API with client-id = ' + clientId);
        modelSource.listen(acquireVsCodeApi());
    }

    console.log('Before requesting model');

    // Button features
    document.getElementById('refresh')!.addEventListener('click', () => {
        dispatcher.dispatch(new RequestModelAction());
    });

}
