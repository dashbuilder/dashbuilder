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
package org.dashbuilder.model.dataset.filter;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * A custom provided function filter definition
 */
public class FilterCustomFunction extends FilterColumn {

    protected FilterFunction function = null;

    public FilterCustomFunction() {
    }

    public FilterCustomFunction(String columnId, FilterFunction function) {
        super(columnId);
        this.function = function;
    }

    public FilterFunction getFunction() {
        return function;
    }

    public void setFunction(FilterFunction function) {
        this.function = function;
    }

    public boolean equals(Object obj) {
        try {
            FilterCustomFunction other = (FilterCustomFunction) obj;
            if (!super.equals(other)) return false;
            if (function != null && !function.getClass().equals(other.getClass())) return false;
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }
}
