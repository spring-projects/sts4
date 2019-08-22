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
import {BeanNode, VSCodeWebViewDiagramServer} from "./model";

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
    SLabel,
    SLabelView,
    SCompartment,
    SCompartmentView, SEdge
} from "sprotty";
import {BeanNodeView, CircleNodeView, EdgeView, ExampleGraphView} from "./views";

export default (useWebsocket: boolean) => {
    require("sprotty/css/sprotty.css");
    require("../css/diagram.css");
    const circlegraphModule = new ContainerModule((bind, unbind, isBound, rebind) => {
        if (useWebsocket)
            bind(TYPES.ModelSource).to(VSCodeWebViewDiagramServer).inSingletonScope();
        else
            bind(TYPES.ModelSource).to(LocalModelSource).inSingletonScope();
        rebind(TYPES.ILogger).to(ConsoleLogger).inSingletonScope();
        rebind(TYPES.LogLevel).toConstantValue(LogLevel.log);
        const context = { bind, unbind, isBound, rebind };
        configureModelElement(context, 'graph', SGraph, ExampleGraphView);
        configureModelElement(context, 'node:circle', CircularNode, CircleNodeView);
        configureModelElement(context, 'node:bean', BeanNode, BeanNodeView);
        configureModelElement(context, 'node:label', SLabel, SLabelView);
        configureModelElement(context, 'compartment', SCompartment, SCompartmentView);
        configureModelElement(context, 'edge:straight', /*OrthogonalEgde*/ SEdge, EdgeView);
        configureViewerOptions(context, {
            needsClientLayout: true,
            needsServerLayout: true,
            baseDiv: 'spring-boot'
        });
    });

    const container = new Container();
    container.load(defaultModule, selectModule, moveModule, boundsModule, undoRedoModule, viewportModule,
        exportModule, updateModule, graphModule, routingModule, modelSourceModule, circlegraphModule, hoverModule);
    return container;
};
