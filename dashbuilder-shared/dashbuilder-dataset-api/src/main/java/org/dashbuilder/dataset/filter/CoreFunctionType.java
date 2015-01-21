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
package org.dashbuilder.dataset.filter;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Type of core filter functions available
 */
@Portable
public enum CoreFunctionType {

    IS_NULL,
    NOT_NULL,
    EQUALS_TO,
    NOT_EQUALS_TO,
    GREATER_THAN,
    GREATER_OR_EQUALS_TO,
    LOWER_THAN,
    LOWER_OR_EQUALS_TO,
    BETWEEN,
    TIME_FRAME;

    public static CoreFunctionType getByName(String type) {
        try {
            return valueOf(type.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
