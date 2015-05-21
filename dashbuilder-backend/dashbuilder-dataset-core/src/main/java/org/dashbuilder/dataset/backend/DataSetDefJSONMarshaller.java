/**
 * Copyright (C) 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.dataset.backend;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.dashbuilder.dataprovider.DataSetProviderRegistry;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.def.*;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * DataSetDef from/to JSON utilities
 */
public class DataSetDefJSONMarshaller {

    // General settings
    public static final String UUID = "uuid";
    public static final String NAME = "name";
    public static final String PROVIDER = "provider";
    public static final String ISPUBLIC = "isPublic";
    public static final String PUSH_ENABLED = "pushEnabled";
    public static final String PUSH_MAXSIZE = "pushMaxSize";
    public static final String COLUMNS = "columns";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_PATTERN = "pattern";
    public static final String FILTERS = "filters";
    public static final String ALL_COLUMNS = "allColumns";

    // CSV related
    public static final String FILEURL = "fileURL";
    public static final String FILEPATH = "filePath";
    public static final String SEPARATORCHAR = "separatorChar";
    public static final String QUOTECHAR = "quoteChar";
    public static final String ESCAPECHAR = "escapeChar";
    public static final String DATEPATTERN = "datePattern";
    public static final String NUMBERPATTERN = "numberPattern";

    // SQL related
    public static final String DATA_SOURCE = "dataSource";
    public static final String DB_SCHEMA = "dbSchema";
    public static final String DB_TABLE = "dbTable";
    public static final String DB_SQL = "dbSQL";
    public static final String CACHE_ENABLED = "cacheEnabled";
    public static final String CACHE_MAXROWS = "cacheMaxRows";
    public static final String REFRESH_TIME = "refreshTime";
    public static final String REFRESH_ALWAYS = "refreshAlways";

    // ElasticSearch related
    public static final String SERVER_URL = "serverURL";
    public static final String CLUSTER_NAME = "clusterName";
    public static final String INDEX = "index";
    public static final String TYPE = "type";
    public static final String QUERY = "query";
    public static final String RELEVANCE = "relevance";
    public static final String CACHE_SYNCED = "cacheSynced";
    
    // Bean related
    public static final String GENERATOR_CLASS = "generatorClass";
    public static final String GENERATOR_PARAMS = "generatorParams";
    public static final String PARAM = "param";
    public static final String VALUE = "value";

    // Other
    public static final String COMMA = ",";
    
    @Inject
    DataSetProviderRegistry dataSetProviderRegistry;

    @Inject
    DataSetLookupJSONMarshaller dataSetLookupJSONMarshaller;

    public DataSetDef fromJson(String jsonString) throws Exception {
        JSONObject json = new JSONObject(jsonString);
        DataSetProviderType providerType = readProviderType(json);
        DataSetDef dataSetDef = DataSetProviderType.createDataSetDef(providerType);

        readGeneralSettings(dataSetDef, json);

        switch (providerType) {
            case CSV:
                readCSVSettings((CSVDataSetDef) dataSetDef, json);
                break;
            case SQL:
                readSQLSettings((SQLDataSetDef) dataSetDef, json);
                break;
            case ELASTICSEARCH:
                readElasticSearchSettings((ElasticSearchDataSetDef) dataSetDef, json);
                break;
            case BEAN:
                readBeanSettings((BeanDataSetDef) dataSetDef, json);
                break;
        }
        return dataSetDef;
    }

    public ElasticSearchDataSetDef readElasticSearchSettings(ElasticSearchDataSetDef dataSetDef, JSONObject json) throws Exception {
        String serverURL = json.has(SERVER_URL) ? json.getString(SERVER_URL) : null;
        String clusterName = json.has(CLUSTER_NAME) ? json.getString(CLUSTER_NAME) : null;
        String index = json.has(INDEX) ? json.getString(INDEX) : null;
        String type = json.has(TYPE) ? json.getString(TYPE) : null;
        String query = json.has(QUERY) ? json.getString(QUERY) : null;
        String relevance = json.has(RELEVANCE) ? json.getString(RELEVANCE) : null;
        String cacheEnabled = json.has(CACHE_ENABLED) ? json.getString(CACHE_ENABLED) : null;
        String cacheMaxRows = json.has(CACHE_MAXROWS) ? json.getString(CACHE_MAXROWS) : null;
        String cacheSynced = json.has(CACHE_SYNCED) ? json.getString(CACHE_SYNCED) : null;

        // ServerURL parameter.
        if (StringUtils.isBlank(serverURL)) {
            throw new IllegalArgumentException("The serverURL property is missing.");
        } else {
            dataSetDef.setServerURL(serverURL);
        }

        // Cluster name parameter.
        if (StringUtils.isBlank(clusterName)) {
            throw new IllegalArgumentException("The clusterName property is missing.");
        } else {
            dataSetDef.setClusterName(clusterName);
        }

        // Index parameter
        if (StringUtils.isBlank(index)) {
            throw new IllegalArgumentException("The index property is missing.");
        } else {
            String[] indexList = index.split(COMMA);
            if (indexList != null && indexList.length > 0) {
                for (String _index : indexList) {
                    dataSetDef.addIndex(_index);
                }
            }
        }

        // Type parameter.
        if (!StringUtils.isBlank(type)) {
            String[] typeList = type.split(COMMA);
            if (typeList != null && typeList.length > 0) {
                for (String _type : typeList) {
                    dataSetDef.addType(_type);
                }
            }
        }

        // Query parameter.
        if (!StringUtils.isBlank(query)) dataSetDef.setQuery(query);

        // Relevance parameter.
        if (!StringUtils.isBlank(relevance)) dataSetDef.setRelevance(relevance);

        // Cache enabled parameter.
        if (!StringUtils.isBlank(cacheEnabled)) dataSetDef.setCacheEnabled(Boolean.parseBoolean(cacheEnabled));

        // Cache max rows parameter.
        if (!StringUtils.isBlank(cacheMaxRows)) dataSetDef.setCacheMaxRows(Integer.parseInt(cacheMaxRows));

        // Cache synced parameter.
        if (!StringUtils.isBlank(cacheSynced)) dataSetDef.setCacheSynced(Boolean.parseBoolean(cacheSynced));

        return dataSetDef;
    }

    public DataSetProviderType readProviderType(JSONObject json) throws Exception {
        String provider = json.getString(PROVIDER);
        if (StringUtils.isBlank(provider)) {
            throw new IllegalArgumentException("Missing 'provider' property");
        }
        DataSetProviderType type = DataSetProviderType.getByName(provider);
        if (type == null || dataSetProviderRegistry.getDataSetProvider(type) == null) {
            throw new IllegalArgumentException("Provider not supported: " + provider);
        }
        return type;
    }

    public DataSetDef readGeneralSettings(DataSetDef def, JSONObject json) throws Exception {
        String uuid = json.has(UUID) ? json.getString(UUID) : null;
        String name = json.has(NAME) ? json.getString(NAME) : null;
        String isPublic = json.has(ISPUBLIC) ? json.getString(ISPUBLIC) : null;
        String pushEnabled = json.has(PUSH_ENABLED) ? json.getString(PUSH_ENABLED) : null;
        String pushMaxSize = json.has(PUSH_MAXSIZE) ? json.getString(PUSH_MAXSIZE) : null;
        String cacheEnabled = json.has(CACHE_ENABLED) ? json.getString(CACHE_ENABLED) : null;
        String cacheMaxRows = json.has(CACHE_MAXROWS) ? json.getString(CACHE_MAXROWS) : null;
        String refreshTime  = json.has(REFRESH_TIME) ? json.getString(REFRESH_TIME) : null;
        String refreshAlways = json.has(REFRESH_ALWAYS) ? json.getString(REFRESH_ALWAYS) : null;
        String allColumns = json.has(ALL_COLUMNS) ? json.getString(ALL_COLUMNS) : null;

        if (!StringUtils.isBlank(uuid)) def.setUUID(uuid);
        if (!StringUtils.isBlank(name)) def.setName(name);
        if (!StringUtils.isBlank(isPublic)) def.setPublic(Boolean.parseBoolean(isPublic));
        if (!StringUtils.isBlank(pushEnabled)) def.setPushEnabled(Boolean.parseBoolean(pushEnabled));
        if (!StringUtils.isBlank(pushMaxSize)) def.setPushMaxSize(Integer.parseInt(pushMaxSize));
        if (!StringUtils.isBlank(cacheEnabled)) def.setCacheEnabled(Boolean.parseBoolean(cacheEnabled));
        if (!StringUtils.isBlank(cacheMaxRows)) def.setCacheMaxRows(Integer.parseInt(cacheMaxRows));
        if (!StringUtils.isBlank(refreshTime)) def.setRefreshTime(refreshTime);
        if (!StringUtils.isBlank(refreshAlways)) def.setRefreshAlways(Boolean.parseBoolean(refreshAlways));
        if (!StringUtils.isBlank(allColumns)) def.setAllColumnsEnabled(Boolean.parseBoolean(allColumns));

        if (json.has(COLUMNS)) {
            JSONArray array = json.getJSONArray(COLUMNS);
            for (int i=0; i<array.length(); i++) {
                JSONObject column = array.getJSONObject(i);
                String columnId = column.has(COLUMN_ID) ? column.getString(COLUMN_ID) : null;
                String columnType = column.has(COLUMN_TYPE) ? column.getString(COLUMN_TYPE) : null;
                String columnPattern = column.has(COLUMN_PATTERN) ? column.getString(COLUMN_PATTERN) : null;

                if (StringUtils.isBlank(columnId)) {
                    throw new IllegalArgumentException("Column id. attribute is mandatory.");
                }
                if (StringUtils.isBlank(columnType)) {
                    throw new IllegalArgumentException("Missing column 'type' attribute: " + columnId);
                }

                ColumnType type = ColumnType.TEXT;
                if (columnType.equals("label")) type = ColumnType.LABEL;
                else if (columnType.equals("date")) type = ColumnType.DATE;
                else if (columnType.equals("number")) type = ColumnType.NUMBER;

                def.addColumn(columnId, type);

                if (!StringUtils.isBlank(columnPattern)) {
                    def.setPattern(columnId, columnPattern);
                }
            }
        }
        if (json.has(FILTERS)) {
            JSONArray array = json.getJSONArray(FILTERS);
            DataSetFilter dataSetFilter = dataSetLookupJSONMarshaller.fromJsonFilterOps(array);
            def.setDataSetFilter(dataSetFilter);
        }
        return def;
    }

    public DataSetDef readBeanSettings(BeanDataSetDef def, JSONObject json) throws Exception {
        String generator = json.has(GENERATOR_CLASS) ? json.getString(GENERATOR_CLASS) : null;

        if (!StringUtils.isBlank(generator)) def.setGeneratorClass(generator);

        if (json.has(GENERATOR_PARAMS)) {
            JSONArray array = json.getJSONArray(GENERATOR_PARAMS);
            for (int i=0; i<array.length(); i++) {
                JSONObject param = array.getJSONObject(i);
                String paramId = param.has(PARAM) ? param.getString(PARAM) : null;
                String value = param.has(VALUE) ? param.getString(VALUE) : null;

                if (!StringUtils.isBlank(paramId)) {
                    def.getParamaterMap().put(paramId, value);
                }
            }
        }
        return def;
    }

    public CSVDataSetDef readCSVSettings(CSVDataSetDef def, JSONObject json) throws Exception {
        String fileURL = json.has(FILEURL) ? json.getString(FILEURL) : null;
        String filePath = json.has(FILEPATH) ? json.getString(FILEPATH) : null;
        String separatorChar = json.has(SEPARATORCHAR) ? json.getString(SEPARATORCHAR) : null;
        String quoteChar = json.has(QUOTECHAR) ? json.getString(QUOTECHAR) : null;
        String escapeChar = json.has(ESCAPECHAR) ? json.getString(ESCAPECHAR) : null;
        String datePattern = json.has(DATEPATTERN) ? json.getString(DATEPATTERN) : null;
        String numberPattern = json.has(NUMBERPATTERN) ? json.getString(NUMBERPATTERN) : null;

        if (!StringUtils.isBlank(fileURL)) def.setFileURL(fileURL);
        if (!StringUtils.isBlank(filePath)) def.setFilePath(filePath);
        if (!StringUtils.isBlank(separatorChar)) def.setSeparatorChar(separatorChar.charAt(0));
        if (!StringUtils.isBlank(quoteChar)) def.setQuoteChar(quoteChar.charAt(0));
        if (!StringUtils.isBlank(escapeChar)) def.setEscapeChar(escapeChar.charAt(0));
        if (!StringUtils.isBlank(numberPattern)) def.setNumberPattern(numberPattern);
        if (!StringUtils.isBlank(datePattern)) def.setDatePattern(datePattern);

        return def;
    }

    public SQLDataSetDef readSQLSettings(SQLDataSetDef def, JSONObject json) throws Exception {
        String dataSource = json.has(DATA_SOURCE) ? json.getString(DATA_SOURCE) : null;
        String dbTable = json.has(DB_TABLE) ? json.getString(DB_TABLE) : null;
        String dbSchema = json.has(DB_SCHEMA) ? json.getString(DB_SCHEMA) : null;
        String dbSQL = json.has(DB_SQL) ? json.getString(DB_SQL) : null;

        if (!StringUtils.isBlank(dataSource)) def.setDataSource(dataSource);
        if (!StringUtils.isBlank(dbSchema)) def.setDbSchema(dbSchema);
        if (!StringUtils.isBlank(dbTable)) def.setDbTable(dbTable);
        if (!StringUtils.isBlank(dbSQL)) def.setDbSQL(dbSQL);

        return def;
    }

    public String toJsonString(final DataSetDef dataSetDef) throws JSONException {
        return toJsonObject( dataSetDef ).toString(1);
    }

    public JSONObject toJsonObject(final DataSetDef dataSetDef ) throws JSONException {
        JSONObject json = new JSONObject(  );

        // UUID.
        json.put( UUID, dataSetDef.getUUID());

        // Name.
        json.put( NAME, dataSetDef.getName());

        // Provider.
        json.put( PROVIDER, dataSetDef.getProvider().name());

        // Public.
        json.put( ISPUBLIC, dataSetDef.isPublic());
        
        // Backend cache.
        json.put( CACHE_ENABLED, dataSetDef.isCacheEnabled());
        json.put( CACHE_MAXROWS, dataSetDef.getCacheMaxRows());

        // Client cache.
        json.put( PUSH_ENABLED, dataSetDef.isPushEnabled());
        json.put( PUSH_MAXSIZE, dataSetDef.getPushMaxSize());

        // Refresh.
        json.put( REFRESH_ALWAYS, dataSetDef.isRefreshAlways());
        json.put( REFRESH_TIME, dataSetDef.getRefreshTime());

        // Specific provider.
        if (dataSetDef instanceof BeanDataSetDef) {
            toJsonObject(((BeanDataSetDef)dataSetDef), json);
        } else if (dataSetDef instanceof CSVDataSetDef) {
            toJsonObject(((CSVDataSetDef)dataSetDef), json);
        } else if (dataSetDef instanceof SQLDataSetDef) {
            toJsonObject(((SQLDataSetDef)dataSetDef), json);
        } else if (dataSetDef instanceof ElasticSearchDataSetDef) {
            toJsonObject(((ElasticSearchDataSetDef)dataSetDef), json);
        }
        
        // Data columns.
        final Collection<DataColumnDef> columns = dataSetDef.getColumns();
        if (columns != null)
        {
            final JSONArray columnsArray = toJsonObject(columns);
            if (columnsArray != null)
            {
                json.put(COLUMNS, columnsArray);
            }
        }

        final DataSetFilter filter = dataSetDef.getDataSetFilter();
        if (filter != null) {
            try {
                final JSONArray filters = dataSetLookupJSONMarshaller.toJson(filter);
                if (filters != null) {
                    json.put(FILTERS, filters);
                }
            } catch (Exception e) {
                throw new JSONException(e);
            }
        }
        
        return json;
    }
    
    private JSONArray toJsonObject(final Collection<DataColumnDef> columnList) throws JSONException {
        JSONArray result = null;
        if (columnList != null && !columnList.isEmpty()) {
            result = new JSONArray();
            for (final DataColumnDef column : columnList) {
                final String id = column.getId();
                final ColumnType type = column.getColumnType();
                final JSONObject columnObject = new JSONObject();
                columnObject.put(COLUMN_ID, id);
                columnObject.put(COLUMN_TYPE, type.name().toLowerCase());
                result.put(columnObject);
            }
        }
        
        return result;
    }

    private void toJsonObject(final BeanDataSetDef dataSetDef, final JSONObject json ) throws JSONException {

        // Generator class.
        json.put( GENERATOR_CLASS, dataSetDef.getGeneratorClass());

        // Generator parameters.
        Map<String, String> parameters = dataSetDef.getParamaterMap();
        if (parameters != null && !parameters.isEmpty()) {
            final JSONArray array = new JSONArray();
            for (Map.Entry<String, String> param : parameters.entrySet()) {
                final JSONObject paramObject = toJSONParameter(param.getKey(), param.getValue());
                array.put(paramObject);
            }
            json.put( GENERATOR_PARAMS, array);
        }
    }

    private void toJsonObject(final CSVDataSetDef dataSetDef, final JSONObject json ) throws JSONException {

        // File.
        if (dataSetDef.getFilePath() != null) json.put( FILEPATH, dataSetDef.getFilePath());
        if (dataSetDef.getFileURL() != null) json.put( FILEURL, dataSetDef.getFileURL());

        // Separator.
        json.put( SEPARATORCHAR, dataSetDef.getSeparatorChar());

        // Quote.
        json.put( QUOTECHAR, dataSetDef.getQuoteChar());

        // Escape.
        json.put( ESCAPECHAR, dataSetDef.getEscapeChar());

        // Date pattern.
        json.put( DATEPATTERN, dataSetDef.getDatePattern());

        // Number pattern.
        json.put( NUMBERPATTERN, dataSetDef.getNumberPattern());

        // All columns flag.
        json.put( ALL_COLUMNS, dataSetDef.isAllColumnsEnabled());

    }

    private void toJsonObject(final SQLDataSetDef dataSetDef, final JSONObject json ) throws JSONException {

        // Data source.
        json.put( DATA_SOURCE, dataSetDef.getDataSource());

        // Schema.
        json.put( DB_SCHEMA, dataSetDef.getDbSchema());

        // Table.
        if (dataSetDef.getDbTable() != null) json.put( DB_TABLE, dataSetDef.getDbTable());

        // Query.
        if (dataSetDef.getDbSQL() != null) json.put( DB_SQL, dataSetDef.getDbSQL());

        // All columns flag.
        json.put( ALL_COLUMNS, dataSetDef.isAllColumnsEnabled());

    }

    private void toJsonObject(final ElasticSearchDataSetDef dataSetDef, final JSONObject json ) throws JSONException {

        // Server URL.
        json.put( SERVER_URL, dataSetDef.getServerURL());

        // Cluster name.
        json.put( CLUSTER_NAME, dataSetDef.getClusterName());

        // Index.
        json.put( INDEX, ArrayUtils.toString(dataSetDef.getIndex()));

        // Type.
        json.put( TYPE, ArrayUtils.toString(dataSetDef.getType()));

        // All columns flag.
        json.put( ALL_COLUMNS, dataSetDef.isAllColumnsEnabled());

    }
    
    private JSONObject toJSONParameter(final String key, final String value) throws JSONException {
        JSONObject json = new JSONObject(  );

        // Param.
        json.put( PARAM, key);

        // Value.
        json.put( VALUE, value);
        
        return json;
    }

}
