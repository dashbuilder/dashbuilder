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
package org.dashbuilder.model.date;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public enum Quarter {
    Q1,
    Q2,
    Q3,
    Q4;

    private static Quarter[] _array = values();

    public int getIndex() {
        for (int i = 0; i < _array.length; i++) {
            Quarter q = _array[i];
            if (this.equals(q)) return i;
        }
        return -1;
    }

    public static Quarter getByName(String name) {
        return valueOf(name.toUpperCase());
    }

    public static Quarter getByIndex(int index) {
        return _array[index];
    }
}
