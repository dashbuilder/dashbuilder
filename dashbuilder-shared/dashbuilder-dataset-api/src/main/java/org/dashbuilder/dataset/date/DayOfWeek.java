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
package org.dashbuilder.dataset.date;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public enum DayOfWeek {
    SUNDAY,
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY;

    private static DayOfWeek[] _array = values();

    public int getIndex() {
        for (int i = 0; i < _array.length; i++) {
            DayOfWeek el = _array[i];
            if (this.equals(el)) return i+1;
        }
        return -1;
    }

    public static int nextIndex(int index) {
        index++;
        if (index <= _array.length) return index;
        return 1;
    }

    public static DayOfWeek[] getAll() {
        return _array;
    }

    public static DayOfWeek getByName(String name) {
        return valueOf(name.toUpperCase());
    }

    public static DayOfWeek getByIndex(int index) {
        return _array[index-1];
    }
}
