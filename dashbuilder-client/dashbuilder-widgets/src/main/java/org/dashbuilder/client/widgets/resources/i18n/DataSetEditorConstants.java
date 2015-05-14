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

/**
 * <p>Data set editor constants.</p>
 *
 * @since 0.3.0 
 */
public interface DataSetEditorConstants extends ConstantsWithLookup {

    public static final DataSetEditorConstants INSTANCE = GWT.create( DataSetEditorConstants.class );

    String error();
    String type();
    String message();
    String cause();
    String ok();
    String loading();
    String selectType();
    String bean();
    String csv();
    String sql();
    String elasticSearch();
    String bean_description();
    String csv_description();
    String sql_description();
    String elasticSearch_description();
    String next();
    String next_description();
    String test();
    String test_description();
    String save();
    String save_description();
    String cancel();
    String cancel_description();
    String providerType();
    String commonAttributes();
    String maxCacheSize();
    String backendCache();
    String clientCache();
    String dataRefresh();
    String none();
    String scheduledInterval();
    String refreshOnStaleData();
    String refreshEvery();
    String bytes();
    String rows();
    String attributeId();
    String attributeId_description();
    String attributeColumnType();
    String attributeColumnType_description();
    String attributeUUID();
    String attributeUUID_description();
    String attributeName();
    String attributeName_description();
    String attributeMaxBytes();
    String attributeMaxBytes_description();
    String attributeMaxRows();
    String attributeMaxRows_description();
    String attributeRefreshInterval();
    String attributeRefreshInterval_description();
    String attributeRefreshOnStaleData();
    String attributeRefreshOnStaleData_description();
    String on();
    String off();
    String nextSteps();
    String defineA();
    String newDataSet();
    String toFetchYourDataFromExtSystem();
    String provider();
    String colsAndFilter();
    String cacheAndRefresh();
    String configureProvider();
    String configureColumns();
    String configureInitialFilter();
    String configureCacheAndRefreshSettings();
    String saveAndDeployDataSet();
    String createDataDisplayers();
    String createDashboards();
    String defaultDataSetName();
    String sqlAttributes();
    String sql_datasource();
    String sql_datasource_description();
    String sql_datasource_placeHolder();
    String sql_schema();
    String sql_schema_description();
    String sql_schema_placeHolder();
    String sql_table();
    String sql_table_description();
    String sql_table_placeHolder();
    String sql_source();
    String sql_query();
    String sql_query_description();
    String sql_query_placeHolder();
    String staticAttributes();
    String beanAttributes();
    String csvAttributes();
    String csv_filePath();
    String csv_filePath_description();
    String csv_filePath_placeholder();
    String csv_URL();
    String csv_URL_description();
    String csv_URL_placeholder();
    String csv_useFilePath();
    String csv_useFileURL();
    String csv_sepChar();
    String csv_sepChar_description();
    String csv_sepChar_placeholder();
    String csv_quoteChar();
    String csv_quoteChar_description();
    String csv_quoteChar_placeholder();
    String csv_escapeChar();
    String csv_escapeChar_description();
    String csv_escapeChar_placeholder();
    String csv_datePattern();
    String csv_datePattern_description();
    String csv_datePattern_placeholder();
    String csv_numberPattern();
    String csv_numberPattern_description();
    String csv_numberPattern_placeholder();
    String elAttributes();
    String el_server_url();
    String el_server_url_description();
    String el_server_url_placeholder();
    String el_cluster_name();
    String el_cluster_name_description();
    String el_cluster_name_placeholder();
    String el_index();
    String el_index_description();
    String el_index_placeholder();
    String el_type();
    String el_type_description();
    String el_type_placeholder();
    String bean_generator_class();
    String bean_generator_class_description();
    String bean_generator_class_placeholder();
    String bean_generator_params();
    String bean_generator_params_description();
    String bean_generator_params_placeholder();
    String bean_generator_param_key();
    String bean_generator_param_key_description();
    String bean_generator_param_key_placeholder();
    String bean_generator_param_value();
    String bean_generator_param_value_description();
    String bean_generator_param_value_placeholder();
    String tab_dataConfiguration();
    String tab_advancedConfiguration();
    String providerSetup();
    String providerSetup_description();
    String filter();
    String dataColumns();
    String label();
    String text();
    String number();
    String date();
    String showColumnsAndFilter();
    String hideColumnsAndFilter();
    String export();
    String exportToExcel();
    String exportToCSV();
    String defNotFound();
}
