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
package org.dashbuilder.dataset.group;

import org.dashbuilder.dataset.ColumnType;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * The strategy defines how to split a collection of values in a set of intervals.
 */
@Portable
public enum GroupStrategy {

    /**
     * The intervals are fixed of an specific size and they don't depend on the underlying data.
     */
    FIXED,

    /**
     * The intervals depends on the underlying data plus some additional criteria such as
     * the minimum interval size or the maximum number of intervals allowed.
     */
    DYNAMIC,

    /**
     * Same as FIXED but each interval data is split into multiple series.
     */
    MULTIPLE,

    /**
     * The intervals are defined in a custom manner and are not bound to any specific generation algorithm.
     */
    CUSTOM;

    /**
     * Check if this strategy can be used with the specified column type.
     */
    public boolean isColumnTypeSupported(ColumnType ct) {
        switch (this) {
            case DYNAMIC:
            case CUSTOM:
                return true;

            case FIXED:
            case MULTIPLE:
                return ct.equals(ColumnType.DATE) || ct.equals(ColumnType.NUMBER);
        }
        return false;
    }

    public static GroupStrategy getByName(String strategy) {
        return valueOf(strategy.toUpperCase());
    }
}
