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
package org.dashbuilder.kpi;

import org.dashbuilder.dataset.DataSetRef;
import org.dashbuilder.displayer.DisplayerSettings;

/**
 * The interface representing a Key Performance indicator. A KPI is the junction between a source of data (a DataSet)
 * and a specific way of visualizing those data (a DisplayerSettings).
 *
 * @see org.dashbuilder.dataset.DataSet
 * @see DisplayerSettings
 */
public interface KPI {

    /**
     * Returns the UUID for this KPI.
     *
     * @return The UUID for this KPI.
     */
    String getUUID();

    /**
     * Returns the DisplayerSettings associated to this KPI.
     *
     * @return The DisplayerSettings associated to this KPI.
     * @see DisplayerSettings
     */
    DisplayerSettings getDisplayerSettings();

    /**
     * Returns the DataSetRef associated to this KPI.
     *
     * @return The DataSetRef associated to this KPI.
     * @see org.dashbuilder.dataset.DataSetRef
     * @see org.dashbuilder.dataset.DataSet
     */
    DataSetRef getDataSetRef();
}
