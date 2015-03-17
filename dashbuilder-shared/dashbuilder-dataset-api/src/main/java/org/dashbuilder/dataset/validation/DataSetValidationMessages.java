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

public interface DataSetValidationMessages extends
    com.google.gwt.i18n.client.ConstantsWithLookup {

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
            
}
