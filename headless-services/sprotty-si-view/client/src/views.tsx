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

/** @jsx svg */
import { svg } from 'snabbdom-jsx';

import { VNode } from "snabbdom/vnode";
import {
    RenderingContext,
    SNode,
    IView,
    SGraphView,
    SGraph,
    PolylineEdgeView,
    SEdge,
    Point,
    RectangularNodeView,
    Selectable,
    SPort,
    Hoverable,
    SShapeElement
} from "sprotty";
import { injectable } from 'inversify';

/**
 * A very simple example node consisting of a plain circle.
 */
@injectable()
export class CircleNodeView implements IView {
    render(node: SNode, context: RenderingContext): VNode {
        const radius = this.getRadius(node);
        return <g>
            <circle class-sprotty-node={true} class-selected={node.selected} r={radius} cx={radius} cy={radius}></circle>
            <text x={radius} y={radius + 7} class-sprotty-text={true}>{node.id.substr(4)}</text>
        </g>;
    }

    protected getRadius(node: SNode) {
        return 40;
    }
}

@injectable()
export class ExampleGraphView extends SGraphView {

    render(model: Readonly<SGraph>, context: RenderingContext): VNode {
        const transform = `scale(${model.zoom}) translate(${-model.scroll.x},${-model.scroll.y})`;
        return <svg class-sprotty-graph={true}>
            <defs>
                <marker id="arrow" orient="auto" overflow="visible" markerUnits="userSpaceOnUse">
                    <path transform="rotate(180)" d="M 10 -5 0 0 10 5 z" />
                </marker>

                <marker id="dot" viewBox="0 0 10 10" refX="5" refY="5"
                        markerWidth="5" markerHeight="5">
                    <circle cx="5" cy="5" r="5" fill="red" />
                </marker>
            </defs>
            <g transform={transform}>
                {context.renderChildren(model)}
            </g>
        </svg>;
    }

}

export class EdgeView extends PolylineEdgeView {

    protected renderLine(edge: SEdge, segments: Point[], context: RenderingContext): VNode {
        const firstPoint = segments[0];
        let path = `M ${firstPoint.x},${firstPoint.y}`;
        for (let i = 1; i < segments.length; i++) {
            const p = segments[i];
            path += ` L ${p.x},${p.y}`;
        }
        const dom: any = <path d={path}/>;
        dom.data.attrs['marker-end'] = 'url(#arrow)';
        return dom;
    }

}

@injectable()
export class IntegrationNodeView extends RectangularNodeView {
    render(node: Readonly<SShapeElement & Hoverable & Selectable>, context: RenderingContext): VNode {
        return <g>
            <rect class-sprotty-node={node instanceof SNode} class-sprotty-port={node instanceof SPort}
                  class-mouseover={node.hoverFeedback} class-selected={node.selected}
                  x="0" y="0" width={Math.max(node.size.width, 0)} height={Math.max(node.size.height, 0)} rx="10" ry="10"></rect>
            {context.renderChildren(node)}
        </g>;
    }
}

@injectable()
export class ChannelNodeView extends RectangularNodeView {
    render(node: Readonly<SShapeElement & Hoverable & Selectable>, context: RenderingContext): VNode {
        const w = Math.max(node.size.width, 0);
        const h = Math.max(node.size.height, 0);
        const path = `M 0 ${h} H ${w-8} A 8 ${h/2} 0 0 0 ${w-8} 0 H 0`;
        return <g>
            <g class-sprotty-node={node instanceof SNode}
               class-mouseover={node.hoverFeedback}
               class-selected={node.selected}
            >
                <path d={path}/>
                <ellipse cx="0" cy={h/2} rx="8" ry={h/2}/>
            </g>
            {context.renderChildren(node)}
        </g>;
    }
}
/*
<g class="shape">'+
            '<rect class="border"/>' +
            '<path class="the_shape" d="M 0 10 H 100 A 8 10 0 0 1 100 30 H 0"/>'+
            '<ellipse class="the_shape" cx="0" cy="20" rx="8" ry="10"/>'+
            '<text class="label"/>'+
            '<text class="label2"/>'+
        '</g>*/
