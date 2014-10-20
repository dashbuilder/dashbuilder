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

import org.dashbuilder.dataset.ColumnType;

/**
 * Interface for the assembly of a DataSetDef instance in a friendly manner.
 *
 * <pre>
 *   DataSetDef providerSettings = DataSetDefFactory.newSQLDataSetDef()
 *     .uuid("all_employees")
 *     .datasource("jndi/mydatasource")
 *     .query("SELECT * FROM employee")
 *     .label("id")
 *     .build();
 * </pre>
 *
 * @see DataSetDef
 */
public interface DataSetDefBuilder<T> {

    /**
     * Set the DataSetDef UUID.
     *
     * @param uuid The UUID of the DataSetDef that is being assembled.
     * @return The DataSetDefBuilder instance that is being used to configure a DataSetDef.
     */
    T uuid(String uuid);

    /**
     * @return The DataSetDef instance that has been configured.
     * @see DataSetDef
     */
    DataSetDef buildDef();

    // Push settings

    /**
     * Enable the ability to push remote data sets from server.
     *
     * @param maxPushSize The maximum size (in kbytes) a data set may have in order to be pushed to clients.
     * @return The DataSetDefBuilder instance that is being used to configure a DataSetDef.
     */
    T pushOn(int maxPushSize);

    /**
     * Disable the ability to push remote data sets from server.
     *
     * @return The DataSetDefBuilder instance that is being used to configure a DataSetDef.
     */
    T pushOff();

    // Data set structure settings

    /**
     * Add an empty column of type label.
     */
    T label(String columnId);

    /**
     * Add an empty column of type text.
     */
    T text(String columnId);

    /**
     * Add an empty column of numeric type.
     *
     * @return The DataSetDefBuilder instance that is being used to configure a DataSetDef.
     */
    T number(String columnId);

    /**
     * Add an empty column of type date.
     *
     * @return The DataSetDefBuilder instance that is being used to configure a DataSetDef.
     */
    T date(String columnId);

    /**
     * Add an empty column of the specified type.
     *
     * @return The DataSetDefBuilder instance that is being used to configure a DataSetDef.
     */
    T column(String columnId, ColumnType type);

    /**
     * Add a row with the given values at the end of the data set.
     *
     * @return The DataSetDefBuilder instance that is being used to configure a DataSetDef.
     */
    T row(Object... values);
}
