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
    protected List<FunctionColumn> functionColumnList = new ArrayList<FunctionColumn>();

    public DataSetOpType getType() {
        return DataSetOpType.GROUP;
    }

    public void addGroupColumn(GroupColumn... groupColumns) {
        for (GroupColumn groupColumn : groupColumns) {
            groupColumnList.add(groupColumn);
        }
    }

    public void addFunctionColumn(FunctionColumn... functionColumns) {
        for (FunctionColumn functionColumn : functionColumns) {
            functionColumnList.add(functionColumn);
        }
    }

    public List<GroupColumn> getGroupColumns() {
        return groupColumnList;
    }

    public List<FunctionColumn> getFunctionColumns() {
        return functionColumnList;
    }

    public boolean equals(Object obj) {
        try {
            DataSetGroup other = (DataSetGroup) obj;
            if (groupColumnList.size() != other.groupColumnList.size()) return false;
            if (functionColumnList.size() != other.functionColumnList.size()) return false;
            for (int i = 0; i < groupColumnList.size(); i++) {
                GroupColumn el = groupColumnList.get(i);
                GroupColumn otherEl = other.groupColumnList.get(i);
                if (!el.equals(otherEl)) return false;
            }
            for (int i = 0; i < functionColumnList.size(); i++) {
                FunctionColumn el = functionColumnList.get(i);
                FunctionColumn otherEl = other.functionColumnList.get(i);
                if (!el.equals(otherEl)) return false;
            }
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }
}
