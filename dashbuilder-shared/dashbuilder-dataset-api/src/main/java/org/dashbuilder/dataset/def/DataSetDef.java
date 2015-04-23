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
package org.dashbuilder.dataset.def;

import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.date.TimeAmount;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.validation.IsTimeInterval;
import org.dashbuilder.dataset.validation.groups.DataSetDefCacheRowsValidation;
import org.dashbuilder.dataset.validation.groups.DataSetDefPushSizeValidation;
import org.dashbuilder.dataset.validation.groups.DataSetDefRefreshIntervalValidation;
import org.jboss.errai.common.client.api.annotations.Portable;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to define the origin, structure and runtime behaviour of a data set instance.
 */
@Portable
public class DataSetDef {

    @NotNull(message = "{dataSetApi_dataSetDef_uuid_notNull}")
    protected String UUID;
    @NotNull(message = "{dataSetApi_dataSetDef_name_notNull}")
    protected String name;
    protected String defFilePath;
    @NotNull(message = "{dataSetApi_dataSetDef_provider_notNull}")
    protected DataSetProviderType provider;
    protected DataSet dataSet = DataSetFactory.newEmptyDataSet();
    protected DataSetFilter dataSetFilter = null;
    protected boolean isPublic = true;
    protected boolean pushEnabled = false;
    @NotNull(message = "{dataSetApi_dataSetDef_pushMaxSize_notNull}", groups = DataSetDefPushSizeValidation.class)
    @Max(value = 4096)
    protected Integer pushMaxSize = 1024;
    protected boolean cacheEnabled = false;
    @NotNull(message = "{dataSetApi_dataSetDef_cacheMaxRows_notNull}", groups = DataSetDefCacheRowsValidation.class)
    @Max(value = 10000)
    protected Integer cacheMaxRows = 1000;
    @NotNull(message = "{dataSetApi_dataSetDef_refreshTime_notNull}", groups = DataSetDefRefreshIntervalValidation.class)
    @IsTimeInterval(message = "{dataSetApi_dataSetDef_refreshTime_intervalInvalid}", groups = DataSetDefRefreshIntervalValidation.class)
    protected String refreshTime = null;
    protected boolean refreshAlways = false;

    protected Map<String,String> patternMap = new HashMap<String,String>();

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefFilePath() {
        return defFilePath;
    }

    public void setDefFilePath(String defFilePath) {
        this.defFilePath = defFilePath;
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public DataSetFilter getDataSetFilter() {
        return dataSetFilter;
    }

    public void setDataSetFilter(DataSetFilter dataSetFilter) {
        this.dataSetFilter = dataSetFilter;
        if (dataSetFilter != null) this.dataSetFilter.setDataSetUUID(UUID);
    }

    public DataSetProviderType getProvider() {
        return provider;
    }

    public void setProvider(DataSetProviderType provider) {
        this.provider = provider;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean isPushEnabled() {
        return pushEnabled;
    }

    public void setPushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }

    public Integer getPushMaxSize() {
        return pushMaxSize;
    }

    public void setPushMaxSize(Integer pushMaxSize) {
        this.pushMaxSize = pushMaxSize;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public Integer getCacheMaxRows() {
        return cacheMaxRows;
    }

    public void setCacheMaxRows(Integer cacheMaxRows) {
        this.cacheMaxRows = cacheMaxRows;
    }

    public String getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(String refreshTime) {
        this.refreshTime = refreshTime;
    }

    public TimeAmount getRefreshTimeAmount() {
        if (refreshTime != null && refreshTime.trim().length() > 0) {
            return TimeAmount.parse(refreshTime);
        }
        return null;
    }

    public boolean isRefreshAlways() {
        return refreshAlways;
    }

    public void setRefreshAlways(boolean refreshAlways) {
        this.refreshAlways = refreshAlways;
    }

    public String getPattern(String columnId) {
        return patternMap.get(columnId);
    }

    public void setPattern(String columnId, String pattern) {
        patternMap.put(columnId, pattern);
    }

    public DataSetDef clone() {
        DataSetDef def = new DataSetDef();
        clone(def);
        return def;
    }
    
    protected void clone(final DataSetDef def) {
        def.setUUID(getUUID());
        def.setName(getName());
        def.setProvider(getProvider());
        def.setDefFilePath(getDefFilePath());
        def.setPublic(isPublic());
        final DataSetFilter currentFilter = getDataSetFilter();
        if (currentFilter != null) {
            final DataSetFilter nFilter = currentFilter.cloneInstance();
            nFilter.setDataSetUUID(getUUID());
            def.setDataSetFilter(nFilter);
        }
        def.setDataSetFilter(getDataSetFilter());
        def.setCacheEnabled(isCacheEnabled());
        def.setCacheMaxRows(getCacheMaxRows());
        def.setPushEnabled(isPushEnabled());
        def.setPushMaxSize(getPushMaxSize());
        def.setRefreshAlways(isRefreshAlways());
        def.setRefreshTime(getRefreshTime());
        final DataSet dataSet = getDataSet();
        if (dataSet != null && dataSet.getColumns() != null) {
            def.getDataSet().setColumns(dataSet.getColumns());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getUUID() == null) return false;
        
        try {
            DataSetDef d = (DataSetDef) obj;
            return getUUID().equals(d.getUUID());
        } catch (ClassCastException e) {
            return false;
        }
    }
}
