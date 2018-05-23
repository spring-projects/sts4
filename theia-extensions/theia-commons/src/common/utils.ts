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