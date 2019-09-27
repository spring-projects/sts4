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
import {ExampleWebsocketDiagramServer, VSCodeWebViewDiagramServer} from "./model";

console.log("Loading di.config");
import "reflect-metadata"

import { Container, ContainerModule } from "inversify";
import {
    defaultModule,
    TYPES,
    configureViewerOptions,
    ConsoleLogger,
    LogLevel,
    boundsModule,
    hoverModule,
    moveModule,
    selectModule,
    undoRedoModule,
    viewportModule,
    LocalModelSource,
    exportModule,
    CircularNode,
    configureModelElement,
    SGraph,
    updateModule,
    graphModule,
    routingModule,
    modelSourceModule,
    edgeLayoutModule,
    SLabel,
    SCompartment,
    SCompartmentView,
    SEdge,
    RectangularNode,
    HtmlRootView,
    HtmlRoot,
    PreRenderedView,
    PreRenderedElement,
    labelEditModule, 
    edgeEditModule, 
    RectangularPort, 
    LayoutRegistry, 
    VBoxLayouter, 
    HBoxLayouter,
    boundsFeature
} from "sprotty";
import {
    IntegrationNodeView,
    CircleNodeView,
    EdgeView,
    ExampleGraphView,
    ChannelNodeView,
    PortView,
    ErrorPortView,
    LimitedWidthLabelView
} from "./views";
import fadeModule from "sprotty/lib/features/fade/di.config";
import buttonModule from "sprotty/lib/features/button/di.config";
import expandModule from "sprotty/lib/features/expand/di.config";
import {MyHBoxLayouter, MyVBoxLayouter} from "./layout";

export enum TransportMedium {
    None,
    Websocket,
    LSP
}

export default (transport: TransportMedium, clientId: string) => {
    require("sprotty/css/sprotty.css");
    require("../css/diagram.css");
    const integrationGaphModule = new ContainerModule((bind, unbind, isBound, rebind) => {
        switch (transport) {
            case TransportMedium.Websocket:
                bind(TYPES.ModelSource).to(ExampleWebsocketDiagramServer).inSingletonScope();
                break;
            case TransportMedium.LSP:
                bind(TYPES.ModelSource).to(VSCodeWebViewDiagramServer).inSingletonScope();
                break;
            default:
                bind(TYPES.ModelSource).to(LocalModelSource).inSingletonScope();
        }
        rebind(TYPES.ILogger).to(ConsoleLogger).inSingletonScope();
        rebind(TYPES.LogLevel).toConstantValue(LogLevel.log);
        const context = { bind, unbind, isBound, rebind };
        configureModelElement(context, 'graph', SGraph, ExampleGraphView);
        configureModelElement(context, 'node:circle', CircularNode, CircleNodeView);
        configureModelElement(context, 'node:integration', RectangularNode, IntegrationNodeView, {disable: [boundsFeature]});
        configureModelElement(context, 'node:channel', RectangularNode, ChannelNodeView);
        configureModelElement(context, 'node:label', SLabel, LimitedWidthLabelView);
        configureModelElement(context, 'compartment', SCompartment, SCompartmentView);
        configureModelElement(context, 'edge:straight', /*OrthogonalEgde*/ SEdge, EdgeView);
        configureModelElement(context, 'html', HtmlRoot, HtmlRootView);
        configureModelElement(context, 'pre-rendered', PreRenderedElement, PreRenderedView);
        configureModelElement(context, 'input-port', RectangularPort, PortView);
        configureModelElement(context, 'output-port', RectangularPort, PortView);
        configureModelElement(context, 'error-port', RectangularPort, ErrorPortView);
        configureViewerOptions(context, {
            needsClientLayout: true,
            needsServerLayout: true,
            baseDiv: clientId,
        });
    });

    const container = new Container();
    container.load(defaultModule, selectModule, moveModule, boundsModule, undoRedoModule,
        viewportModule, fadeModule, hoverModule, exportModule, expandModule, buttonModule,
        updateModule, graphModule, routingModule, edgeEditModule, edgeLayoutModule, labelEditModule,
        modelSourceModule, integrationGaphModule);

    const layoutRegistry = container.get<LayoutRegistry>(TYPES.LayoutRegistry);
    layoutRegistry.deregister(VBoxLayouter.KIND);
    layoutRegistry.register(VBoxLayouter.KIND, new MyVBoxLayouter());
    layoutRegistry.deregister(HBoxLayouter.KIND);
    layoutRegistry.register(HBoxLayouter.KIND, new MyHBoxLayouter());


    return container;
};
