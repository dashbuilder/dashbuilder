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
package org.dashbuilder.client.widgets.resources.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.ConstantsWithLookup;

public interface DataSetEditorConstants extends ConstantsWithLookup {

    public static final DataSetEditorConstants INSTANCE = GWT.create( DataSetEditorConstants.class );

    String selectType();
    String bean();
    String csv();
    String sql();
    String elasticSearch();
    String next();
    String test();
    String save();
    String cancel();
    String back();
    String providerType();
    String commonAttributes();
    String backendCacheAttributes();
    String clientCacheAttributes();
    String refreshPolicyAttributes();
    String attributeId();
    String attributeUUID();
    String attributeName();
    String attributeMaxBytes();
    String attributeMaxRows();
    String attributeRefreshInterval();
    String attributeRefreshOnStaleData();
    String attributeDataSource();
    String query();
    String table();
    String on();
    String off();
    String createNewDataSetText();
    String defaultDataSetName();
    String sqlAttributes();
    String sql_datasource();
    String sql_datasource_placeHolder();
    String sql_schema();
    String sql_schema_placeHolder();
    String sql_table();
    String sql_table_placeHolder();
    String staticAttributes();
    String beanAttributes();
    String csvAttributes();
    String csv_filePath();
    String csv_filePath_placeholder();
    String csv_URL();
    String csv_URL_placeholder();
    String csv_useFilePath();
    String csv_useFileURL();
    String csv_sepChar();
    String csv_sepChar_placeholder();
    String csv_quoteChar();
    String csv_quoteChar_placeholder();
    String csv_escapeChar();
    String csv_escapeChar_placeholder();
    String csv_datePattern();
    String csv_datePattern_placeholder();
    String csv_numberPattern();
    String csv_numberPattern_placeholder();
    String elAttributes();
    String tab_dataConfiguration();
    String tab_dataPreview();
    String tab_advancedConfiguration();
    String filter();
    String dataColumns();
}
