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
    SShapeElement,
    SLabelView,
    SLabel
} from "sprotty";
import { injectable } from 'inversify';
import { IntegrationNode } from './model';

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

/**
 * A very simple example node consisting of a plain circle.
 */
@injectable()
export class LimitedWidthLabelView extends SLabelView {

    static maxChars = 30; 

    render(label: SLabel, context: RenderingContext): VNode {
        let l = label.text;
        if (l.length > LimitedWidthLabelView.maxChars) {
            const excess = l.length - LimitedWidthLabelView.maxChars + 1;
            const keepChars = l.length - excess;
            const prefixLen = Math.floor(keepChars / 2);
            const postfixLen = keepChars - prefixLen; 
            l = l.substring(0, prefixLen)+"\u2026" + l.substring(l.length-postfixLen);
        }
        return <text class-sprotty-label={true}>{l}</text>;
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
    render(node: Readonly<IntegrationNode & Hoverable & Selectable>, context: RenderingContext): VNode {
        if (node.componentType==='transformer') {
            return <g>
                <rect x="1.5px" stroke="black" height="97px" id="background_rect" stroke-width="3" width="97px" y="1.5px" fill="none"
                    class-sprotty-node={node instanceof SNode}
                    class-mouseover={node.hoverFeedback} class-selected={node.selected}
                />
                <rect stroke="#000000" height="80px" x="10px" id="rect1" stroke-width="3px" width="20px" y="10px" fill="greenyellow" transform=""/>
                <rect stroke="#000000" height="80px" x="70px" id="rect2" stroke-width="3px" width="20px" y="10px" fill="greenyellow" transform=""/>
                <line stroke="#000000" y1="20.000000px" id="line1" stroke-width="3px" x1="25.000000px" y2="80.000000px" x2="75.000000px" transform=""/>
                <line stroke="#000000" y1="80.000000px" id="line2" x1="25.000000px" stroke-width="3px" y2="20.000000px" x2="75.000000px" transform=""/>
                {context.renderChildren(node)}
            </g>;
        } else if (node.componentType==='aggregator') {
            return <g>
                <rect x="1.5px" stroke="black" height="97px" id="background_rect" stroke-width="3" width="97px" y="1.5px" fill="none"
                    class-sprotty-node={node instanceof SNode}
                    class-mouseover={node.hoverFeedback} class-selected={node.selected}
                />
                <rect stroke="#000000" x="10px" height="20px" y="10px" id="rect1" stroke-width="3px" width="20px" fill="greenyellow"
                    transform=""></rect>
                <rect stroke="#000000" x="10px" height="20px" y="40px" id="rect2" stroke-width="3px" width="20px" fill="greenyellow"
                    transform=""></rect>
                <rect stroke="#000000" x="70px" height="20px" y="40px" id="rect4" stroke-width="3px" width="20px" fill="greenyellow"
                    transform=""></rect>
                <rect stroke="#000000" x="10px" height="20px" y="70px" id="rect3" stroke-width="3px" width="20px" fill="greenyellow"
                    transform=""></rect>
                <line stroke="#000000" y1="50.000000px" id="line1" x1="35.000000px" stroke-width="3px" y2="50.000000px"
                    x2="65.000000px" transform=""></line>
                <line stroke="#000000" y1="45.000000px" id="line2" x1="58.000000px" stroke-width="3px" y2="50.000000px"
                    x2="65.000000px" transform=""></line>
                <line stroke="#000000" y1="55.000000px" id="line3" x1="58.000000px" stroke-width="3px" y2="50.000000px"
                    x2="65.000000px" transform=""></line>
                {context.renderChildren(node)}
            </g>
        } else if (node.componentType==='splitter') {
            return <g>
                <rect x="1.5px" stroke="black" height="97px" id="background_rect" stroke-width="3" width="97px" y="1.5px" fill="none"
                    class-sprotty-node={node instanceof SNode}
                    class-mouseover={node.hoverFeedback} class-selected={node.selected}
                ></rect>
                <rect stroke="#000000" x="70px" height="20px" y="10px" id="rect1" stroke-width="3px" width="20px" fill="greenyellow"
                    transform=""></rect>
                <rect stroke="#000000" x="10px" height="20px" y="40px" id="rect2" stroke-width="3px" width="20px" fill="greenyellow"
                    transform=""></rect>
                <rect stroke="#000000" x="70px" height="20px" y="40px" id="rect4" stroke-width="3px" width="20px" fill="greenyellow"
                    transform=""></rect>
                <rect stroke="#000000" x="70px" height="20px" y="70px" id="rect3" stroke-width="3px" width="20px" fill="greenyellow"
                    transform=""></rect>
                <line stroke="#000000" y1="50.000000px" id="line1" x1="35.000000px" stroke-width="3px" y2="50.000000px"
                    x2="65.000000px" transform=""></line>
                <line stroke="#000000" y1="45.000000px" id="line2" x1="58.000000px" stroke-width="3px" y2="50.000000px"
                    x2="65.000000px" transform=""></line>
                <line stroke="#000000" y1="55.000000px" id="line3" x1="58.000000px" stroke-width="3px" y2="50.000000px"
                    x2="65.000000px" transform=""></line>
                {context.renderChildren(node)}
            </g>;
        } else if (node.componentType === 'router') {
            return <g>
                <rect x="1.5px" stroke="black" height="97px" id="background_rect" stroke-width="3" width="97px" y="1.5px" fill="none"
                    class-sprotty-node={node instanceof SNode}
                    class-mouseover={node.hoverFeedback} class-selected={node.selected}
                ></rect>
                <line stroke="#000000" stroke-width="2px" x1="23.25" y1="50" x2="77" y2="20"/>
                <circle cx="20" cy="50" r="7"/>
                <circle cx="80" cy="20" r="7"/>
                <circle cx="80" cy="50" r="7"/>
                <circle cx="80" cy="80" r="7"/>
                <line stroke="#000000" stroke-width="2px" x1="7.5" y1="50.25" x2="15" y2="50"/>
                <line stroke="#000000" stroke-width="2px" x1="85" y1="20" x2="94" y2="20"/>
                <line stroke="#000000" stroke-width="2px" x1="85" y1="50" x2="94" y2="50"/>
                <line stroke="#000000" stroke-width="2px" x1="85" y1="80" x2="94" y2="80"/>
                {context.renderChildren(node)}
            </g>;
        } else if (node.componentType === 'service-activator') {
            return <g>
                <rect x="1.5px" stroke="black" height="97px" id="background_rect" stroke-width="3" width="97px" y="1.5px" fill="none"
                    class-sprotty-node={node instanceof SNode}
                    class-mouseover={node.hoverFeedback} class-selected={node.selected}
                ></rect>
                <rect x="15" y="45" width="10" height="10" fill="black" stroke-width="2px" transform="matrix(0.707107, 0.707107, -0.707107, 0.707107, 59.21994, 0.486262)"/>
                <rect x="15" y="45" width="10" height="10"
                     stroke="black" fill="#ffffff" stroke-width="2px"
                     transform="matrix(0.707107, 0.707107, -0.707107, 0.707107, 97.098412, 0.486262)"/>
                <g>
                    <line stroke="black" x1="10" y1="50" x2="30" y2="50"/>
                    <line stroke="black" x1="24" y1="45" x2="30" y2="50"/>
                    <line stroke="black" x1="24" y1="55" x2="30" y2="50"/>
                </g>
                <g transform="matrix(1, 0, 0, 1, 35.958775, -0.044944)">
                    <line stroke="black" x1="10" y1="50" x2="30" y2="50"/>
                    <line stroke="black" x1="24" y1="45" x2="30" y2="50"/>
                    <line stroke="black" x1="24" y1="55" x2="30" y2="50"/>
                </g>
                {context.renderChildren(node)}
            </g>;
        } else if (node.componentType.indexOf('outbound-channel-adapter')>=0) {
            return <g> 
                <rect class-sprotty-node={node instanceof SNode}
                      class-mouseover={node.hoverFeedback} class-selected={node.selected}
                      x="0" y="0" width={Math.max(node.size.width, 0)} height={Math.max(node.size.height, 0)} rx="10" ry="10"/>
                <g>
                    <line stroke="black" x1="10" y1="50" x2="27" y2="50"/>
                    <line stroke="black" x1="21" y1="45" x2="27" y2="50"/>
                    <line stroke="black" x1="21" y1="55" x2="27" y2="50"/>
                </g>
                <line stroke="black" x1="75" y1="30" x2="75" y2="30"/>
                <polygon fill="greenyellow" stroke="black" stroke-linejoin="round" points="55 10 55 90 30 70 30 30"/>
                <rect x="60" y="10" width="30" height="80" stroke="black" fill="white" stroke-linejoin="round"/>
                {context.renderChildren(node)}
            </g>
        } else {
            return <g> 
                <rect class-sprotty-node={node instanceof SNode}
                      class-mouseover={node.hoverFeedback} class-selected={node.selected}
                      x="0" y="0" width={Math.max(node.size.width, 0)} height={Math.max(node.size.height, 0)} rx="10" ry="10"></rect>
                {context.renderChildren(node)}
            </g>
        }
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

@injectable()
export class PortView implements IView {
    render(node: Readonly<SShapeElement & Hoverable & Selectable>, context: RenderingContext): VNode {
        return <g>
            <rect class-sprotty-node={node instanceof SPort} class-sprotty-port={node instanceof SPort}
                  class-mouseover={node.hoverFeedback} class-selected={node.selected}
                  x="0" y="0" width={Math.max(node.size.width, 0)} height={Math.max(node.size.height, 0)}></rect>
            {context.renderChildren(node)}
        </g>;
    }
}

@injectable()
export class ErrorPortView implements IView {
    render(node: Readonly<SShapeElement & Hoverable & Selectable>, context: RenderingContext): VNode {
        return <g>
            <rect class-sprotty-node={node instanceof SPort} class-sprotty-port={node instanceof SPort}
                  class-sprotty-error-port={node instanceof SPort} class-mouseover={node.hoverFeedback} class-selected={node.selected}
                  x="0" y="0" width={Math.max(node.size.width, 0)} height={Math.max(node.size.height, 0)}></rect>
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
