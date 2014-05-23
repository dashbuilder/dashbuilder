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
package org.dashbuilder.model.dataset.filter;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Type of core filter functions available
 */
@Portable
public enum FilterCoreFunctionType {

    IS_NULL,
    IS_NOT_NULL,
    IS_EQUALS_TO,
    IS_NOT_EQUALS_TO,
    IS_GREATER_THAN,
    IS_GREATER_OR_EQUALS_TO,
    IS_LOWER_THAN,
    IS_LOWER_OR_EQUALS_TO,
    IS_BETWEEN;

    public static FilterCoreFunctionType getByName(String interval) {
        if (interval == null || interval.length() == 0) return null;
        return valueOf(interval.toUpperCase());
    }
}
