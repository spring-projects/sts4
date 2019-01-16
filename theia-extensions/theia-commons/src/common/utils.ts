/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
export class Utils {

    private static setNestedValue(properties: string[], value: any, obj: any){
        if (properties.length > 1) {
            // The property doesn't exists OR is not an object (and so we overwritte it) so we create it
            if (!obj.hasOwnProperty(properties[0]) || typeof obj[properties[0]] !== 'object') {
                obj[properties[0]] = {};
            }
            Utils.setNestedValue(properties.slice(1), value, obj[properties[0]]);
        } else {
            obj[properties[0]] = value;
        }
    }

    public static convertDotToNested(source: any): any {
        const result = {};
        Object.keys(source).forEach(property => {
           const properties = property.split('.');
           if (source[property] && typeof source[property] === 'object') {
               Utils.setNestedValue(properties, Utils.convertDotToNested(source[property]), result);
           } else {
               Utils.setNestedValue(properties, source[property], result);
           }
        });
        return result;
    }

}