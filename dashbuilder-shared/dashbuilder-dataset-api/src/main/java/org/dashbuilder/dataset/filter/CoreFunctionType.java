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

import java.util.List;
import java.util.ArrayList;

import org.dashbuilder.dataset.ColumnType;
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

    private static CoreFunctionType[] _typeArray = values();

    private static int[] _parametersSize = new int[] {0,0,1,1,1,1,1,1,2,1};

    public int getIndex() {
        for (int i = 0; i < _typeArray.length; i++) {
            CoreFunctionType type = _typeArray[i];
            if (this.equals(type)) return i;
        }
        return -1;
    }

    public int getNumberOfParameters() {
        return _parametersSize[getIndex()];
    }

    public boolean supportType(ColumnType type) {
        if (TIME_FRAME.equals(this)) {
            return ColumnType.DATE.equals(type);
        }
        return true;
    }
    public static CoreFunctionType getByIndex(int index) {
        return _typeArray[index];
    }

    public static CoreFunctionType getByName(String type) {
        try {
            return valueOf(type.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    public static int getNumberOfParameters(String type) {
        CoreFunctionType ft = getByName(type);
        return ft.getNumberOfParameters();
    }

    public static List<CoreFunctionType> getSupportedTypes(ColumnType columnType) {
        List<CoreFunctionType> result = new ArrayList<CoreFunctionType>();
        for (int i = 0; i < _typeArray.length; i++) {
            CoreFunctionType type = _typeArray[i];
            if (type.supportType(columnType)) result.add(type);
        }
        return result;
    }
}
