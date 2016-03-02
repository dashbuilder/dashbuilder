/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.dataset.json;

import com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.def.*;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.json.Json;
import org.dashbuilder.json.JsonArray;
import org.dashbuilder.json.JsonException;
import org.dashbuilder.json.JsonObject;

import java.util.Collection;
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

    private static DataSetDefJSONMarshaller SINGLETON = new DataSetDefJSONMarshaller();

    public static DataSetDefJSONMarshaller get() {
        return SINGLETON;
    }

    protected DataSetLookupJSONMarshaller dataSetLookupJSONMarshaller;
    
    public DataSetDefJSONMarshaller() {
        this(DataSetLookupJSONMarshaller.get());
    }

    public DataSetDefJSONMarshaller(DataSetLookupJSONMarshaller dataSetLookupJSONMarshaller) {
        this.dataSetLookupJSONMarshaller = dataSetLookupJSONMarshaller;
    }

    public DataSetDef fromJson(String jsonString) throws Exception {
        JsonObject json = Json.parse(jsonString);
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

    public ElasticSearchDataSetDef readElasticSearchSettings(ElasticSearchDataSetDef dataSetDef, JsonObject json) throws Exception {
        String serverURL = json.getString(SERVER_URL);
        String clusterName = json.getString(CLUSTER_NAME);
        String index = json.getString(INDEX);
        String type = json.getString(TYPE);
        String query = json.getString(QUERY);
        String relevance = json.getString(RELEVANCE);
        String cacheEnabled = json.getString(CACHE_ENABLED);
        String cacheMaxRows = json.getString(CACHE_MAXROWS);
        String cacheSynced = json.getString(CACHE_SYNCED);

        // ServerURL parameter.
        if (isBlank(serverURL)) {
            throw new IllegalArgumentException("The serverURL property is missing.");
        } else {
            dataSetDef.setServerURL(serverURL);
        }

        // Cluster name parameter.
        if (isBlank(clusterName)) {
            throw new IllegalArgumentException("The clusterName property is missing.");
        } else {
            dataSetDef.setClusterName(clusterName);
        }

        // Index parameter
        if (isBlank(index)) {
            throw new IllegalArgumentException("The index property is missing.");
        } else {
            dataSetDef.setIndex(index);
        }

        // Type parameter.
        if (!isBlank(type)) {
            dataSetDef.setType(type);
        }

        // Query parameter.
        if (!isBlank(query)) {
            dataSetDef.setQuery(query);
        }

        // Relevance parameter.
        if (!isBlank(relevance)) {
            dataSetDef.setRelevance(relevance);
        }

        // Cache enabled parameter.
        if (!isBlank(cacheEnabled)) {
            dataSetDef.setCacheEnabled(Boolean.parseBoolean(cacheEnabled));
        }

        // Cache max rows parameter.
        if (!isBlank(cacheMaxRows)) {
            dataSetDef.setCacheMaxRows(Integer.parseInt(cacheMaxRows));
        }

        return dataSetDef;
    }

    public DataSetProviderType readProviderType(JsonObject json) throws Exception {
        String provider = json.getString(PROVIDER);
        if (isBlank(provider)) {
            throw new IllegalArgumentException("Missing 'provider' property");
        }
        DataSetProviderType type = DataSetProviderType.getByName(provider);
        if (type == null) {
            throw new IllegalArgumentException("Provider not supported: " + provider);
        }
        return type;
    }

    public DataSetDef readGeneralSettings(DataSetDef def, JsonObject json) throws Exception {
        String uuid = json.getString(UUID);
        String name = json.getString(NAME);
        String isPublic = json.getString(ISPUBLIC);
        String pushEnabled = json.getString(PUSH_ENABLED);
        String pushMaxSize = json.getString(PUSH_MAXSIZE);
        String cacheEnabled = json.getString(CACHE_ENABLED);
        String cacheMaxRows = json.getString(CACHE_MAXROWS);
        String refreshTime  = json.getString(REFRESH_TIME);
        String refreshAlways = json.getString(REFRESH_ALWAYS);
        String allColumns = json.getString(ALL_COLUMNS);

        if (!isBlank(uuid)) {
            def.setUUID(uuid);
        }
        if (!isBlank(name)) {
            def.setName(name);
        }
        if (!isBlank(isPublic)) {
            def.setPublic(Boolean.parseBoolean(isPublic));
        }
        if (!isBlank(pushEnabled)) {
            def.setPushEnabled(Boolean.parseBoolean(pushEnabled));
        }
        if (!isBlank(pushMaxSize)) {
            def.setPushMaxSize(Integer.parseInt(pushMaxSize));
        }
        if (!isBlank(cacheEnabled)) {
            def.setCacheEnabled(Boolean.parseBoolean(cacheEnabled));
        }
        if (!isBlank(cacheMaxRows)) {
            def.setCacheMaxRows(Integer.parseInt(cacheMaxRows));
        }
        if (!isBlank(refreshTime)) {
            def.setRefreshTime(refreshTime);
        }
        if (!isBlank(refreshAlways)) {
            def.setRefreshAlways(Boolean.parseBoolean(refreshAlways));
        }
        if (!isBlank(allColumns)) {
            def.setAllColumnsEnabled(Boolean.parseBoolean(allColumns));
        }

        if (json.has(COLUMNS)) {
            JsonArray array = json.getArray(COLUMNS);
            for (int i=0; i<array.length(); i++) {
                JsonObject column = array.getObject(i);
                String columnId = column.getString(COLUMN_ID);
                String columnType = column.getString(COLUMN_TYPE);
                String columnPattern = column.getString(COLUMN_PATTERN);

                if (isBlank(columnId)) {
                    throw new IllegalArgumentException("Column id. attribute is mandatory.");
                }
                if (isBlank(columnType)) {
                    throw new IllegalArgumentException("Missing column 'type' attribute: " + columnId);
                }

                ColumnType type = ColumnType.TEXT;
                if (columnType.equalsIgnoreCase("label")) {
                    type = ColumnType.LABEL;
                }
                else if (columnType.equalsIgnoreCase("date")) {
                    type = ColumnType.DATE;
                }
                else if (columnType.equalsIgnoreCase("number")) {
                    type = ColumnType.NUMBER;
                }

                def.addColumn(columnId, type);

                if (!isBlank(columnPattern)) {
                    def.setPattern(columnId, columnPattern);
                }
            }
        }
        if (json.has(FILTERS)) {
            JsonArray array = json.getArray(FILTERS);
            DataSetFilter dataSetFilter = dataSetLookupJSONMarshaller.parseFilterOperation(array);
            def.setDataSetFilter(dataSetFilter);
        }
        return def;
    }

    public DataSetDef readBeanSettings(BeanDataSetDef def, JsonObject json) throws Exception {
        String generator = json.getString(GENERATOR_CLASS);

        if (!isBlank(generator)) {
            def.setGeneratorClass(generator);
        }
        if (json.has(GENERATOR_PARAMS)) {
            JsonArray array = json.getArray(GENERATOR_PARAMS);
            for (int i=0; i<array.length(); i++) {
                JsonObject param = array.getObject(i);
                String paramId = param.getString(PARAM);
                String value = param.getString(VALUE);

                if (!isBlank(paramId)) {
                    def.getParamaterMap().put(paramId, value);
                }
            }
        }
        return def;
    }

    public CSVDataSetDef readCSVSettings(CSVDataSetDef def, JsonObject json) throws Exception {
        String fileURL = json.getString(FILEURL);
        String filePath = json.getString(FILEPATH);
        String separatorChar = parseCodePoint(json.getString(SEPARATORCHAR));
        String quoteChar = parseCodePoint(json.getString(QUOTECHAR));
        String escapeChar = parseCodePoint(json.getString(ESCAPECHAR));
        String datePattern = json.getString(DATEPATTERN);
        String numberPattern = json.getString(NUMBERPATTERN);

        if (!isBlank(fileURL)) {
            def.setFileURL(fileURL);
        }
        if (!isBlank(filePath)) {
            def.setFilePath(filePath);
        }
        if (!isBlank(separatorChar)) {
            def.setSeparatorChar(separatorChar.charAt(0));
        }
        if (!isBlank(quoteChar)) {
            def.setQuoteChar(quoteChar.charAt(0));
        }
        if (!isBlank(escapeChar)) {
            def.setEscapeChar(escapeChar.charAt(0));
        }
        if (!isBlank(numberPattern)) {
            def.setNumberPattern(numberPattern);
        }
        if (!isBlank(datePattern)) {
            def.setDatePattern(datePattern);
        }

        return def;
    }

    public String parseCodePoint(String codePoint) {
        try {
            if (!isBlank(codePoint)) {
                return String.valueOf(Character.toChars(Integer.parseInt(codePoint)));
            }
        } catch (Exception e) {
            // If is not a code point then return the string "as is"
        }
        return codePoint;
    }

    public SQLDataSetDef readSQLSettings(SQLDataSetDef def, JsonObject json) throws Exception {
        String dataSource = json.getString(DATA_SOURCE);
        String dbTable = json.getString(DB_TABLE);
        String dbSchema = json.getString(DB_SCHEMA);
        String dbSQL = json.getString(DB_SQL);

        if (!isBlank(dataSource)) {
            def.setDataSource(dataSource);
        }
        if (!isBlank(dbSchema)) {
            def.setDbSchema(dbSchema);
        }
        if (!isBlank(dbTable)) {
            def.setDbTable(dbTable);
        }
        if (!isBlank(dbSQL)) {
            def.setDbSQL(dbSQL);
        }

        return def;
    }

    public String toJsonString(final DataSetDef dataSetDef) throws JsonException {
        return toJsonObject( dataSetDef ).toString();
    }

    public JsonObject toJsonObject(final DataSetDef dataSetDef) throws JsonException {
        JsonObject json = Json.createObject();

        // UUID.
        json.put(UUID, dataSetDef.getUUID());

        // Name.
        json.put(NAME, dataSetDef.getName());

        // Provider.
        json.put(PROVIDER, dataSetDef.getProvider().name());

        // Public.
        json.put(ISPUBLIC, dataSetDef.isPublic());
        
        // Backend cache.
        json.put(CACHE_ENABLED, dataSetDef.isCacheEnabled());
        json.put(CACHE_MAXROWS, dataSetDef.getCacheMaxRows());

        // Client cache.
        json.put(PUSH_ENABLED, dataSetDef.isPushEnabled());
        json.put(PUSH_MAXSIZE, dataSetDef.getPushMaxSize());

        // Refresh.
        json.put(REFRESH_ALWAYS, dataSetDef.isRefreshAlways());
        json.put(REFRESH_TIME, dataSetDef.getRefreshTime());

        // Specific provider.
        if (dataSetDef instanceof BeanDataSetDef) {
            toJsonObject((BeanDataSetDef)dataSetDef, json);
        } 
        else if (dataSetDef instanceof CSVDataSetDef) {
            toJsonObject((CSVDataSetDef)dataSetDef, json);
        } 
        else if (dataSetDef instanceof SQLDataSetDef) {
            toJsonObject((SQLDataSetDef)dataSetDef, json);
        } 
        else if (dataSetDef instanceof ElasticSearchDataSetDef) {
            toJsonObject((ElasticSearchDataSetDef)dataSetDef, json);
        }
        
        // Data columns.
        final Collection<DataColumnDef> columns = dataSetDef.getColumns();
        if (columns != null)
        {
            final JsonArray columnsArray = toJsonObject(columns, dataSetDef);
            if (columnsArray != null)
            {
                json.put(COLUMNS, columnsArray);
            }
        }

        final DataSetFilter filter = dataSetDef.getDataSetFilter();
        if (filter != null) {
            try {
                final JsonArray filters = dataSetLookupJSONMarshaller.formatColumnFilters(filter.getColumnFilterList());
                if (filters != null) {
                    json.put(FILTERS, filters);
                }
            } catch (Exception e) {
                throw new JsonException(e);
            }
        }
        
        return json;
    }

    protected JsonArray toJsonObject(final Collection<DataColumnDef> columnList,
                                   final DataSetDef dataSetDef) throws JsonException {
        JsonArray result = null;
        if (columnList != null && !columnList.isEmpty()) {
            result = Json.createArray();
            int idx = 0;
            for (final DataColumnDef column : columnList) {
                final String id = column.getId();
                final ColumnType type = column.getColumnType();
                final JsonObject columnObject = Json.createObject();
                columnObject.put(COLUMN_ID, id);
                columnObject.put(COLUMN_TYPE, type.name().toLowerCase());
                String pattern = dataSetDef.getPattern(id);
                if (pattern != null && pattern.trim().length() > 0) {
                    columnObject.put(COLUMN_PATTERN, pattern);
                }
                result.set(idx++, columnObject);
            }
        }
        
        return result;
    }

    protected void toJsonObject(final BeanDataSetDef dataSetDef, final JsonObject json) throws JsonException {

        // Generator class.
        json.put(GENERATOR_CLASS, dataSetDef.getGeneratorClass());

        // Generator parameters.
        Map<String, String> parameters = dataSetDef.getParamaterMap();
        if (parameters != null && !parameters.isEmpty()) {
            final JsonArray array = Json.createArray();
            int idx = 0;
            for (Map.Entry<String, String> param : parameters.entrySet()) {
                final JsonObject paramObject = toJsonParameter(param.getKey(), param.getValue());
                array.set(idx++, paramObject);
            }
            json.put(GENERATOR_PARAMS, array);
        }
    }

    protected void toJsonObject(final CSVDataSetDef dataSetDef, final JsonObject json) throws JsonException {

        // File.
        if (dataSetDef.getFilePath() != null) {
            json.put(FILEPATH, dataSetDef.getFilePath());
        }
        if (dataSetDef.getFileURL() != null) {
            json.put(FILEURL, dataSetDef.getFileURL());
        }

        // Separator.
        json.put(SEPARATORCHAR, String.valueOf(dataSetDef.getSeparatorChar()));

        // Quote.
        json.put(QUOTECHAR, String.valueOf(dataSetDef.getQuoteChar()));

        // Escape.
        json.put(ESCAPECHAR, String.valueOf(dataSetDef.getEscapeChar()));

        // Date pattern.
        json.put(DATEPATTERN, dataSetDef.getDatePattern());

        // Number pattern.
        json.put(NUMBERPATTERN, dataSetDef.getNumberPattern());

        // All columns flag.
        json.put(ALL_COLUMNS, dataSetDef.isAllColumnsEnabled());

    }

    protected void toJsonObject(final SQLDataSetDef dataSetDef, final JsonObject json) throws JsonException {

        // Data source.
        json.put(DATA_SOURCE, dataSetDef.getDataSource());

        // Schema.
        json.put(DB_SCHEMA, dataSetDef.getDbSchema());

        // Table.
        if (dataSetDef.getDbTable() != null) {
            json.put(DB_TABLE, dataSetDef.getDbTable());
        }

        // Query.
        if (dataSetDef.getDbSQL() != null) {
            json.put(DB_SQL, dataSetDef.getDbSQL());
        }

        // All columns flag.
        json.put(ALL_COLUMNS, dataSetDef.isAllColumnsEnabled());

    }

    protected void toJsonObject(final ElasticSearchDataSetDef dataSetDef, final JsonObject json) throws JsonException {

        // Server URL.
        json.put(SERVER_URL, dataSetDef.getServerURL());

        // Cluster name.
        json.put(CLUSTER_NAME, dataSetDef.getClusterName());

        // Index.
        json.put(INDEX, dataSetDef.getIndex());

        // Type.
        json.put(TYPE, dataSetDef.getType());

        // All columns flag.
        json.put(ALL_COLUMNS, dataSetDef.isAllColumnsEnabled());

    }

    protected JsonObject toJsonParameter(final String key, final String value) throws JsonException {
        JsonObject json = Json.createObject();

        // Param.
        json.put(PARAM, key);

        // Value.
        json.put(VALUE, value);
        
        return json;
    }

    protected boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isSpace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
