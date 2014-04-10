/**
 * Copyright (C) 2012 JBoss Inc
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
package org.dashbuilder.model.dataset.group;

import org.dashbuilder.model.dataset.ColumnType;

/**
 * The domain types available.
 */
public enum DomainType {

    /**
     * The intervals are fixed of an specific size and they don't depend on the underlying data.
     */
    FIXED,

    /**
     * The intervals depends on the underlying data plus some additional criteria such as
     * the minimun interval size or the maximum number of intervals allowed.
     */
    ADAPTATIVE,

    /**
     * Same as FIXED but additionally each interval data is split into multiple series.
     */
    MULTIPLE,

    /**
     * The intervals are defined in a custom matter and are not bound to any specific generation algorithm.
     */
    CUSTOM;

    /**
     * Check if this DataDomainType instance can be used with the specified column type.
     */
    public boolean isColumnTypeSupported(ColumnType ct) {
        switch (this) {
            case ADAPTATIVE:
            case CUSTOM:
                return true;

            case FIXED:
            case MULTIPLE:
                return ct.equals(ColumnType.DATE) || ct.equals(ColumnType.NUMBER);
        }
        return false;
    }
}
