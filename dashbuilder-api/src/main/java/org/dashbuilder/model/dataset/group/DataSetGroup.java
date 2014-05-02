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

import java.util.List;
import java.util.ArrayList;

import org.dashbuilder.model.dataset.DataSetOp;
import org.dashbuilder.model.dataset.DataSetOpType;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * A data set group operation.
 */
@Portable
public class DataSetGroup implements DataSetOp {

    protected List<GroupColumn> groupColumnList = new ArrayList<GroupColumn>();
    protected List<GroupFunction> groupFunctionList = new ArrayList<GroupFunction>();

    public DataSetOpType getType() {
        return DataSetOpType.GROUP;
    }

    public void addGroupColumn(GroupColumn... groupColumns) {
        for (GroupColumn groupColumn : groupColumns) {
            groupColumnList.add(groupColumn);
        }
    }

    public void addGroupFunction(GroupFunction... groupFunctions) {
        for (GroupFunction groupFunction : groupFunctions) {
            groupFunctionList.add(groupFunction);
        }
    }

    public List<GroupColumn> getGroupColumns() {
        return groupColumnList;
    }

    public List<GroupFunction> getGroupFunctions() {
        return groupFunctionList;
    }

    public boolean equals(Object obj) {
        try {
            DataSetGroup other = (DataSetGroup) obj;
            if (groupColumnList.size() != other.groupColumnList.size()) return false;
            if (groupFunctionList.size() != other.groupFunctionList.size()) return false;
            for (int i = 0; i < groupColumnList.size(); i++) {
                GroupColumn el = groupColumnList.get(i);
                GroupColumn otherEl = other.groupColumnList.get(i);
                if (!el.equals(otherEl)) return false;
            }
            for (int i = 0; i < groupFunctionList.size(); i++) {
                GroupFunction el = groupFunctionList.get(i);
                GroupFunction otherEl = other.groupFunctionList.get(i);
                if (!el.equals(otherEl)) return false;
            }
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }
}
