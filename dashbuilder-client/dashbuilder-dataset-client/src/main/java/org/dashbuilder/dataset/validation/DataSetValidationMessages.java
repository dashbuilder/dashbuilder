/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dashbuilder.dataset.validation;

import com.google.gwt.core.client.GWT;

/**
 * <p>The data set validation messages for the client side.</p>
 * <p>The properties file for these messages is located at <code>dashbuilder-dataset-api</code>, 
 * as they've to be shared across server and client side.</p>
 *
 * @see <code>dashbuilder-dataset-api/src/main/resources/org/dashbuilder/dataset/validation/DataSetValidationMessages.properties</code>
 */
public interface DataSetValidationMessages extends
        com.google.gwt.i18n.client.ConstantsWithLookup {

    public static final DataSetValidationMessages INSTANCE = GWT.create(DataSetValidationMessages.class);

    @Key("dataSetApi_dataSetDef_uuid_notNull")
    String dataSetApi_dataSetDef_uuid_notNull();

    @Key("dataSetApi_dataSetDef_name_notNull")
    String dataSetApi_dataSetDef_name_notNull();

    @Key("dataSetApi_dataSetDef_provider_notNull")
    String dataSetApi_dataSetDef_provider_notNull();

    @Key("dataSetApi_dataSetDef_refreshTime_notNull")
    String dataSetApi_dataSetDef_refreshTime_notNull();

    @Key("dataSetApi_dataSetDef_refreshTime_notEmpty")
    String dataSetApi_dataSetDef_refreshTime_notEmpty();

    @Key("dataSetApi_dataSetDef_pushMaxSize_notNull")
    String dataSetApi_dataSetDef_pushMaxSize_notNull();

    @Key("dataSetApi_dataSetDef_cacheMaxRows_notNull")
    String dataSetApi_dataSetDef_cacheMaxRows_notNull();

    @Key("dataSetApi_dataSetDef_refreshTime_intervalInvalid")
    String dataSetApi_dataSetDef_refreshTime_intervalInvalid();

    @Key("dataSetApi_sqlDataSetDef_dataSource_notNull")
    String dataSetApi_sqlDataSetDef_dataSource_notNull();


    @Key("dataSetApi_sqlDataSetDef_dbSchema_notNull")
    String dataSetApi_sqlDataSetDef_dbSchema_notNull();

    @Key("dataSetApi_sqlDataSetDef_dbTable_notNull")
    String dataSetApi_sqlDataSetDef_dbTable_notNull();

    @Key("dataSetApi_sqlDataSetDef_dbSQL_notNull")
    String dataSetApi_sqlDataSetDef_dbSQL_notNull();

    @Key("dataSetApi_csvDataSetDef_fileURL_notNull")
    String dataSetApi_csvDataSetDef_fileURL_notNull();

    @Key("dataSetApi_csvDataSetDef_filePath_notNull")
    String dataSetApi_csvDataSetDef_filePath_notNull();

    @Key("dataSetApi_csvDataSetDef_sepChar_notNull")
    String dataSetApi_csvDataSetDef_sepChar_notNull();

    @Key("dataSetApi_csvDataSetDef_quoteChar_notNull")
    String dataSetApi_csvDataSetDef_quoteChar_notNull();

    @Key("dataSetApi_csvDataSetDef_escapeChar_notNull")
    String dataSetApi_csvDataSetDef_escapeChar_notNull();

    @Key("dataSetApi_csvDataSetDef_datePattern_notNull")
    String dataSetApi_csvDataSetDef_datePattern_notNull();

    @Key("dataSetApi_csvDataSetDef_numberPattern_notNull")
    String dataSetApi_csvDataSetDef_numberPattern_notNull();


    @Key("dataSetApi_beanDataSetDef_generatorClass_notNull")
    String dataSetApi_beanDataSetDef_generatorClass_notNull();


    @Key("dataSetApi_elDataSetDef_serverURL_notNull")
    String dataSetApi_elDataSetDef_serverURL_notNull();

    @Key("dataSetApi_elDataSetDef_clusterName_notNull")
    String dataSetApi_elDataSetDef_clusterName_notNull();

    @Key("dataSetApi_elDataSetDef_index_notNull")
    String dataSetApi_elDataSetDef_index_notNull();

    @Key("dataSetApi_dataColumnDef_id_notNull")
    String dataSetApi_dataColumnDef_id_notNull();

    @Key("dataSetApi_dataColumnDef_columnType_notNull")
    String dataSetApi_dataColumnDef_columnType_notNull();

}