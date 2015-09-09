package org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.jest.gson;

/**
 * @since 0.3.0
 */
public class FieldMapping {
    
    private String type;
    private String index;
    private String format;

    public FieldMapping() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
