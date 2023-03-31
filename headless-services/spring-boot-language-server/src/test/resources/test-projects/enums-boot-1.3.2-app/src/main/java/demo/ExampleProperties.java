package demo;

import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = ExampleProperties.PREFIX)
@Validated
public class ExampleProperties {
    
    public static final String PREFIX  = "nested-enum";
    
    public C.E enumValue;
    
    private Set<C.E> listOfEnums;
    
    private Set<SomeProperties> listProperties;
    
    private Map<String, SomeProperties> mapProperties;
        
    public C.E getEnumValue() {
        return enumValue;
    }

    public void setEnumValue(C.E enumValue) {
        this.enumValue = enumValue;
    }

    public Set<C.E> getListOfEnums() {
        return listOfEnums;
    }

    public void setListOfEnums(Set<C.E> listOfEnums) {
        this.listOfEnums = listOfEnums;
    }
    
    public Set<SomeProperties> getListProperties() {
        return listProperties;
    }

    public void setListProperties(Set<SomeProperties> listProperties) {
        this.listProperties = listProperties;
    }

    public Map<String, SomeProperties> getMapProperties() {
        return mapProperties;
    }

    public void setMapProperties(Map<String, SomeProperties> mapProperties) {
        this.mapProperties = mapProperties;
    }

    public static class SomeProperties {
        private C.E enumValue;
        private Set<C.E> listOfEnums;
        public C.E getEnumValue() {
            return enumValue;
        }
        public void setEnumValue(C.E enumValue) {
            this.enumValue = enumValue;
        }
        public Set<C.E> getListOfEnums() {
            return listOfEnums;
        }
        public void setListOfEnums(Set<C.E> listOfEnums) {
            this.listOfEnums = listOfEnums;
        }
    }

}
