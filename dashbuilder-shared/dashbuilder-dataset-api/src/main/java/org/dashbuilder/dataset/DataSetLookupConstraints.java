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
package org.dashbuilder.dataset;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dashbuilder.dataset.group.AggregateFunctionType;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.impl.DataSetLookupBuilderImpl;

/**
 * A set of constraints over the structure of a DataSetLookup instance.
 */
public class DataSetLookupConstraints extends DataSetConstraints<DataSetLookupConstraints> {

    public static final int ERROR_GROUP_NUMBER = 200;
    public static final int ERROR_GROUP_NOT_ALLOWED = 201;
    public static final int ERROR_GROUP_REQUIRED = 203;

    protected boolean groupAllowed = true;
    protected boolean groupRequired = false;
    protected int maxGroups = -1;

    public boolean isGroupAllowed() {
        return groupAllowed;
    }

    public DataSetLookupConstraints setGroupAllowed(boolean groupAllowed) {
        this.groupAllowed = groupAllowed;
        return this;
    }

    public boolean isGroupRequired() {
        return groupRequired;
    }

    public DataSetLookupConstraints setGroupRequired(boolean groupRequired) {
        this.groupRequired = groupRequired;
        return this;
    }

    public int getMaxGroups() {
        return maxGroups;
    }

    public DataSetLookupConstraints setMaxGroups(int maxGroups) {
        this.maxGroups = maxGroups;
        return this;
    }

    public ValidationError check(DataSetLookup lookup) {

        List<DataSetGroup> grOps = lookup.getOperationList(DataSetGroup.class);
        if (!groupAllowed && grOps.size() > 0) {
            return new ValidationError(ERROR_GROUP_NOT_ALLOWED);
        }
        if (groupRequired && grOps.size() == 0) {
            return new ValidationError(ERROR_GROUP_REQUIRED);
        }
        if (maxGroups != -1 && grOps.size() > maxGroups) {
            return new ValidationError(ERROR_GROUP_NUMBER);
        }
        return null;
    }

    public DataSetLookup newDataSetLookup(DataSetMetadata metatada) {
        DataSetLookupBuilder<DataSetLookupBuilderImpl> builder = DataSetFactory.newDataSetLookupBuilder();
        builder.dataset(metatada.getUUID());

        // Data set group lookup
        if (groupRequired) {
            int groupIdx = getGroupColumn(metatada);
            if (groupIdx == -1) {
                // No group-able column available
                return null;
            }
            // Add the group column
            Set<Integer> exclude = new HashSet<Integer>();
            exclude.add(groupIdx);
            builder.group(metatada.getColumnId(groupIdx));
            builder.column(metatada.getColumnId(groupIdx));

            // Add the rest of the columns
            for (int i=1; columnTypes != null && i<columnTypes.length; i++) {
                ColumnType targetType = columnTypes[i];

                // Do the best to get a new (not already added) column for the targetType.
                int idx = getTargetColumn(metatada, targetType, exclude);

                // Otherwise, get the first column available.
                if (idx == -1) idx = getTargetColumn(metatada, exclude);

                exclude.add(idx);
                String columnId = metatada.getColumnId(idx);
                ColumnType columnType = metatada.getColumnType(idx);

                if (ColumnType.LABEL.equals(targetType)) {
                    builder.column(columnId);
                }
                else if (ColumnType.DATE.equals(targetType)) {
                    builder.column(columnId);
                }
                else if (ColumnType.NUMBER.equals(targetType)) {
                    if (ColumnType.LABEL.equals(columnType)) {
                        builder.column(AggregateFunctionType.COUNT, "#items");
                    }
                    else if (ColumnType.LABEL.equals(columnType)) {
                        builder.column(AggregateFunctionType.COUNT, "#items");
                    }
                    else if (ColumnType.NUMBER.equals(columnType)) {
                        builder.column(columnId, AggregateFunctionType.SUM);
                    }
                }
            }
        }
        return builder.buildLookup();
    }

    private int getGroupColumn(DataSetMetadata metatada) {
        for (int i=0; i<metatada.getNumberOfColumns(); i++) {
            ColumnType type = metatada.getColumnType(i);
            if (type.equals(ColumnType.LABEL)) return i;
        }
        for (int i=0; i<metatada.getNumberOfColumns(); i++) {
            ColumnType type = metatada.getColumnType(i);
            if (type.equals(ColumnType.DATE)) return i;
        }
        return -1;
    }

    private int getTargetColumn(DataSetMetadata metatada, ColumnType type, Set<Integer> exclude) {
        int target = -1;
        for (int i=0; i<metatada.getNumberOfColumns(); i++) {
            if (type.equals(metatada.getColumnType(i))) {
                if (target == -1) {
                    target = i;
                }
                if (!exclude.contains(i)) {
                    return i;
                }
            }
        }
        return target;
    }

    private int getTargetColumn(DataSetMetadata metatada, Set<Integer> exclude) {
        for (int i=0; i<metatada.getNumberOfColumns(); i++) {
            if (!exclude.contains(i)) {
                return i;
            }
        }
        return 0;
    }
}