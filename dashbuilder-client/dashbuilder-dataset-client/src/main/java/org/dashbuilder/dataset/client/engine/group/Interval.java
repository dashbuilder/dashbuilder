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
package org.dashbuilder.dataset.client.engine.group;

import java.util.ArrayList;
import java.util.List;

/**
 * An interval represent a grouped subset of a data values.
 */
public class Interval {

    /**
     * A name that identifies the interval and it's different of other intervals belonging to the same group.
     */
    protected String name = null;

    /**
     * The row indexes of the values that belong to this interval.
     */
    protected List<Integer> rows = new ArrayList<Integer>();

    public Interval() {
    }

    public Interval(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getRows() {
        return rows;
    }

    public void setRows(List<Integer> rows) {
        this.rows = rows;
    }

    public boolean equals(Object other) {
        if (name == null) return other == null;
        return name == other || name.equals(other);
    }

    public int hashCode() {
        if (name == null) return 0;
        return name.hashCode();
    }
}
