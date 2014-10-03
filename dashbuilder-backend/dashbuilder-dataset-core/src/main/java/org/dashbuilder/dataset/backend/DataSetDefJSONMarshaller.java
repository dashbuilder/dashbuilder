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
import org.dashbuilder.dataset.def.CSVDataSetDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * DataSetDef from/to JSON utilities
 */
public class DataSetDefJSONMarshaller {

    public static final String UUID = "uuid";
    public static final String PROVIDER = "provider";
    public static final String SHARED = "shared";
    public static final String PUSHENABLED = "pushEnabled";
    public static final String MAXPUSHSIZE = "maxPushSize";
    public static final String FILEURL = "fileURL";
    public static final String FILEPATH = "filePath";
    public static final String SEPARATORCHAR = "separatorChar";
    public static final String QUOTECHAR = "quoteChar";
    public static final String ESCAPECHAR = "escapeChar";
    public static final String DATEPATTERN = "datePattern";
    public static final String NUMBERPATTERN = "numberPattern";
    public static final String COLUMNS = "columns";
    public static final String COLUMN_ID = "columnId";
    public static final String ASLABEL = "asLabel";

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
        String shared = json.has(SHARED) ? json.getString(SHARED) : null;
        String pushEnabled = json.has(PUSHENABLED) ? json.getString(PUSHENABLED) : null;
        String maxPushSize = json.has(MAXPUSHSIZE) ? json.getString(MAXPUSHSIZE) : null;

        if (!StringUtils.isBlank(uuid)) def.setUUID(uuid);
        if (!StringUtils.isBlank(shared)) def.setShared(Boolean.parseBoolean(shared));
        if (!StringUtils.isBlank(pushEnabled)) def.setPushEnabled(Boolean.parseBoolean(pushEnabled));
        if (!StringUtils.isBlank(maxPushSize)) def.setMaxPushSize(Integer.parseInt(maxPushSize));
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

        if (json.has(COLUMNS)) {
            JSONArray array = json.getJSONArray(COLUMNS);
            for (int i=0; i<array.length(); i++) {
                JSONObject column = array.getJSONObject(i);
                String columnId = column.has(COLUMN_ID) ? column.getString(COLUMN_ID) : null;
                String asLabel = column.has(ASLABEL) ? column.getString(ASLABEL) : null;
                String cNumberPattern = column.has(NUMBERPATTERN) ? column.getString(NUMBERPATTERN) : null;
                String cDatePattern = column.has(DATEPATTERN) ? column.getString(DATEPATTERN) : null;

                if (!StringUtils.isBlank(columnId)) {
                    if (!StringUtils.isBlank(asLabel)) def.asLabel(columnId);
                    if (!StringUtils.isBlank(cNumberPattern)) def.setNumberPattern(columnId, cNumberPattern);
                    if (!StringUtils.isBlank(cDatePattern)) def.setDatePattern(columnId, cDatePattern);
                }
            }
        }
        return def;
    }
}
