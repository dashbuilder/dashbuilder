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

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.dashbuilder.dataprovider.DataSetProviderRegistry;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.def.BeanDataSetDef;
import org.dashbuilder.dataset.def.CSVDataSetDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.SQLDataSetDef;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * DataSetDef from/to JSON utilities
 */
public class DataSetDefJSONMarshaller {

    // General settings
    public static final String UUID = "uuid";
    public static final String PROVIDER = "provider";
    public static final String ISPUBLIC = "isPublic";
    public static final String PUSH_ENABLED = "pushEnabled";
    public static final String PUSH_MAXSIZE = "pushMaxSize";
    public static final String COLUMNS = "columns";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_PATTERN = "pattern";

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
    public static final String CACHE_ENABLED = "cacheEnabled";
    public static final String CACHE_MAXROWS = "cacheMaxRows";

    // Bean related
    public static final String GENERATOR_CLASS = "generatorClass";
    public static final String GENERATOR_PARAMS = "generatorParams";
    public static final String PARAM = "param";
    public static final String VALUE = "value";

    @Inject
    DataSetProviderRegistry dataSetProviderRegistry;

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
            case BEAN:
                readBeanSettings((BeanDataSetDef) dataSetDef, json);
                break;
        }
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
        String isPublic = json.has(ISPUBLIC) ? json.getString(ISPUBLIC) : null;
        String pushEnabled = json.has(PUSH_ENABLED) ? json.getString(PUSH_ENABLED) : null;
        String pushMaxSize = json.has(PUSH_MAXSIZE) ? json.getString(PUSH_MAXSIZE) : null;

        if (!StringUtils.isBlank(uuid)) def.setUUID(uuid);
        if (!StringUtils.isBlank(isPublic)) def.setPublic(Boolean.parseBoolean(isPublic));
        if (!StringUtils.isBlank(pushEnabled)) def.setPushEnabled(Boolean.parseBoolean(pushEnabled));
        if (!StringUtils.isBlank(pushMaxSize)) def.setPushMaxSize(Integer.parseInt(pushMaxSize));

        if (json.has(COLUMNS)) {
            JSONArray array = json.getJSONArray(COLUMNS);
            for (int i=0; i<array.length(); i++) {
                JSONObject column = array.getJSONObject(i);
                String columnId = column.has(COLUMN_ID) ? column.getString(COLUMN_ID) : null;
                String columnName = column.has(COLUMN_NAME) ? column.getString(COLUMN_NAME) : null;
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

                if (StringUtils.isBlank(columnName)) {
                    def.getDataSet().addColumn(columnId, type);
                } else {
                    def.getDataSet().addColumn(columnId, columnName, type);
                }

                if (!StringUtils.isBlank(columnPattern)) {
                    def.setPattern(columnId, columnPattern);
                }
            }
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
        String cacheEnabled = json.has(CACHE_ENABLED) ? json.getString(CACHE_ENABLED) : null;
        String cacheMaxRows = json.has(CACHE_MAXROWS) ? json.getString(CACHE_MAXROWS) : null;

        if (!StringUtils.isBlank(dataSource)) def.setDataSource(dataSource);
        if (!StringUtils.isBlank(dbSchema)) def.setDbSchema(dbSchema);
        if (!StringUtils.isBlank(dbTable)) def.setDbTable(dbTable);
        if (!StringUtils.isBlank(cacheEnabled)) def.setCacheEnabled(Boolean.parseBoolean(cacheEnabled));
        if (!StringUtils.isBlank(cacheMaxRows)) def.setCacheMaxRows(Integer.parseInt(cacheMaxRows));

        return def;
    }
}
