package org.dashbuilder.dataprovider;

import org.dashbuilder.dataset.def.*;

/**
 * An enumeration of the available data set provider types.
 */
public enum DataSetProviderType {

    /**
     * For accessing statically registered data set which are created by calling directly to the data set API.
     */
    STATIC,

    /**
     * For accessing data sets generated directly from a bean class implementing the DataSetGenerator interface
     */
    BEAN,

    /**
     * For accessing data sets defined as an SQL query over an existing data source.
     */
    SQL,

    /**
     * For accessing data sets that are the result of loading all the rows of a CSV file.
     */
    CSV,

    /**
     * For accessing data sets that are the result of querying an elasticsearch server.
     */
    ELASTICSEARCH;

    public static DataSetProviderType getByName(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    public static DataSetDef createDataSetDef(DataSetProviderType type) {
        switch (type) {
            case STATIC: return new StaticDataSetDef();
            case BEAN: return new BeanDataSetDef();
            case CSV: return new CSVDataSetDef();
            case SQL: return new SQLDataSetDef();
            case ELASTICSEARCH: return new ElasticSearchDataSetDef();
        }
        throw new RuntimeException("Unknown type: " + type);
    }
}
