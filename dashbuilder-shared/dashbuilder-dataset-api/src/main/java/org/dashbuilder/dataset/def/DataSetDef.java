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

import java.util.HashMap;
import java.util.Map;

import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.group.DateIntervalType;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * This class is used to define the origin, structure and runtime behaviour of a data set instance.
 */
@Portable
public class DataSetDef {

    protected String UUID;
    protected String defFilePath;
    protected DataSetProviderType provider;
    protected DataSet dataSet = DataSetFactory.newEmptyDataSet();
    protected DataSetFilter dataSetFilter = null;
    protected boolean isPublic = true;
    protected boolean pushEnabled = false;
    protected int pushMaxSize = 1024;
    protected boolean cacheEnabled = false;
    protected int cacheMaxRows = 1000;
    protected String refreshTime = null;
    protected boolean refreshAlways = false;

    protected Map<String,String> patternMap = new HashMap<String,String>();

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
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

    public int getPushMaxSize() {
        return pushMaxSize;
    }

    public void setPushMaxSize(int pushMaxSize) {
        this.pushMaxSize = pushMaxSize;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public int getCacheMaxRows() {
        return cacheMaxRows;
    }

    public void setCacheMaxRows(int cacheMaxRows) {
        this.cacheMaxRows = cacheMaxRows;
    }

    public String getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(String refreshTime) {
        if (DateIntervalType.getDurationInMillis(refreshTime) == -1) {
            throw new IllegalArgumentException("Invalid time frame: " + refreshTime);
        }
        this.refreshTime = refreshTime;
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
}
