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
package org.dashbuilder.client.js;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.core.client.JsArrayString;
import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.impl.DataColumnImpl;
import org.dashbuilder.model.dataset.impl.DataSetImpl;
import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.displayer.XAxis;
import org.dashbuilder.model.displayer.YAxis;
import org.dashbuilder.model.displayer.impl.DataDisplayerImpl;
import org.dashbuilder.model.displayer.impl.XAxisImpl;
import org.dashbuilder.model.displayer.impl.YAxisImpl;

public class JsObjectHelper {

    public static DataSet createDataSet(JsDataSet jsDataSet) {
        DataSetImpl dataSet = new DataSetImpl();
        dataSet.setColumns(createDataColumns(jsDataSet.getJsColumns()));
        return dataSet;
    }

    public static DataColumn createDataColumn(JsDataColumn jsDataColumn) {
        DataColumnImpl dataColumn = new DataColumnImpl();
        dataColumn.setId(jsDataColumn.getId());
        dataColumn.setName(jsDataColumn.getName());
        dataColumn.setColumnType(ColumnType.getByName(jsDataColumn.getType()));
        if (dataColumn.getColumnType().equals(ColumnType.NUMBER)) {
            dataColumn.setValues(createListNumber(jsDataColumn.getJsNumbers()));
        } else if (dataColumn.getColumnType().equals(ColumnType.DATE)) {
            dataColumn.setValues(createListDate(jsDataColumn.getJsStrings()));
        } else {
            dataColumn.setValues(createListString(jsDataColumn.getJsStrings()));
        }
        return dataColumn;
    }

    public static List<DataColumn> createDataColumns(JsArray<JsDataColumn> array) {
        List<DataColumn> results = new ArrayList<DataColumn>();
        for (int i = 0; i < array.length(); i++) {
            DataColumn dataColumn = createDataColumn(array.get(i));
            results.add(dataColumn);
        }
        return results;
    }

    public static DataDisplayer createDataDisplayer(JsDataDisplayer jsDataDisplayer) {
        DataDisplayerImpl displayer = new DataDisplayerImpl();
        displayer.setTitle(jsDataDisplayer.getTitle());
        displayer.setRenderer(jsDataDisplayer.getRenderer());
        displayer.setType(jsDataDisplayer.getType());
        displayer.setXAxis(createXAxis(jsDataDisplayer.getJsXAxis()));
        displayer.setYAxes(createYAxes(jsDataDisplayer.getJsYAxes()));
        return displayer;
    }

    public static XAxis createXAxis(JsXAxis obj) {
        XAxisImpl result = new XAxisImpl();
        result.setColumnId(obj.getColumnId());
        result.setDisplayName(obj.getDisplayName());
        return result;
    }

    public static YAxis createYAxis(JsYAxis obj) {
        YAxisImpl result = new YAxisImpl();
        result.setColumnId(obj.getColumnId());
        result.setDisplayName(obj.getDisplayName());
        return result;
    }

    public static List<YAxis> createYAxes(JsArray<JsYAxis> array) {
        List<YAxis> results = new ArrayList<YAxis>();
        for (int i = 0; i < array.length(); i++) {
            results.add(createYAxis(array.get(i)));
        }
        return results;
    }

    public static List createListNumber(JsArrayNumber array) {
        List  results = new ArrayList();
        for (int i = 0; i < array.length(); i++) {
            results.add(array.get(i));
        }
        return results;
    }

    public static List createListString(JsArrayString array) {
        List  results = new ArrayList();
        for (int i = 0; i < array.length(); i++) {
            results.add(array.get(i));
        }
        return results;
    }

    public static List createListDate(JsArrayString array) {
        List  results = new ArrayList();
        for (int i = 0; i < array.length(); i++) {
            // TODO: parse dates
            results.add(array.get(i));
        }
        return results;
    }
}
