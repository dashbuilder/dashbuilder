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
package org.dashbuilder.displayer.client.json;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.impl.DataColumnImpl;
import org.dashbuilder.dataset.impl.DataSetImpl;

public class DataSetJSONMarshaller {

    private static final String DATASET_COLUMN = "column";
    private static final String DATASET_COLUMN_ID = "id";
    private static final String DATASET_COLUMN_TYPE = "type";
    private static final String DATASET_COLUMN_VALUES = "values";

    public JSONObject toJson( DataSet dataSet ) {
        JSONObject json = new JSONObject();
        if ( dataSet != null ) {
            int i = 0;
            // TODO change to JSONArray ?
            for ( DataColumn dataColumn: dataSet.getColumns() ) {
                json.put( DATASET_COLUMN + "." + i++, formatDataColumn( dataColumn ) );
            }
        }
        return json;
    }

    private JSONObject formatDataColumn( DataColumn dataColumn ) {
        JSONObject columnJson = new JSONObject();
        if (dataColumn != null) {
            columnJson.put( DATASET_COLUMN_ID, new JSONString( dataColumn.getId() ) );
            columnJson.put( DATASET_COLUMN_TYPE, new JSONString( dataColumn.getColumnType().toString() ) );
            columnJson.put( DATASET_COLUMN_VALUES, formatColumnValues( dataColumn, dataColumn.getValues() ) );
        }
        return columnJson;
    }

    private JSONArray formatColumnValues( DataColumn dataColumn, List values ) {
        JSONArray valuesJson = new JSONArray();
        if ( values != null ) {
            int i = 0;
            for ( Object value : values ) {
                switch ( dataColumn.getColumnType() ) {
                    case DATE: {
                        String l = Long.toString( ((Date)value).getTime() );
                        valuesJson.set( i++, new JSONString(l) ); break;
                    }
                    default: valuesJson.set( i++, new JSONString(value.toString()) );
                }
            }
        }
        return valuesJson;
    }

    public DataSet fromJson( String jsonString ) {
        if ( StringUtils.isBlank( jsonString ) ) return null;
        JSONObject json = JSONParser.parseStrict( jsonString ).isObject();
        return fromJson( json );
    }

    public DataSet fromJson( JSONObject dataSetJson ) {
        if ( dataSetJson == null ) return null;

        DataSetImpl dataSet = new DataSetImpl();
        for (int i = 0; i < dataSetJson.size(); i++) {
            JSONObject columnJson = dataSetJson.get( DATASET_COLUMN + "." + Integer.toString( i ) ).isObject();
            dataSet.getColumns().add( parseDataColumn( dataSet, columnJson ) );
        }
        return dataSet;
    }

    private DataColumn parseDataColumn( DataSetImpl dataSet, JSONObject columnJson ) {
        DataColumnImpl dataColumn = null;
        if ( columnJson != null) {
            dataColumn = new DataColumnImpl();
            dataColumn.setDataSet( dataSet );

            JSONString columnId = columnJson.get( DATASET_COLUMN_ID ).isString();
            JSONString columnType = columnJson.get( DATASET_COLUMN_TYPE ).isString();

            if ( columnId == null || columnType == null ) throw new RuntimeException( "Column id / type need to be specified" );

            dataColumn.setId( columnId.stringValue() );
            dataColumn.setColumnType( ColumnType.valueOf( columnType.stringValue() ) );

            parseColumnValues( dataColumn, columnJson );
        }
        return dataColumn;
    }

    private void parseColumnValues( DataColumnImpl dataColumn, JSONObject columnJson ) {
        JSONArray valueArray = columnJson.get( DATASET_COLUMN_VALUES ).isArray();
        if ( valueArray != null ) {
            List values = new ArrayList( valueArray.size() );
            for ( int i = 0; i < valueArray.size(); i++ ) {
                JSONString stringJson = valueArray.get( i ).isString();
                switch ( dataColumn.getColumnType() ) {
                    case DATE: values.add( parseDateValue( stringJson.stringValue() ) ); break;
                    case NUMBER: values.add( parseNumberValue( stringJson.stringValue() ) ); break;
                    case LABEL: values.add( stringJson.stringValue() ); break;
                    case TEXT: values.add( stringJson.stringValue() ); break;
                }
            }
            dataColumn.setValues( values );
        }
    }

    private Date parseDateValue( String stringValue ) {
        Long dateLong = Long.parseLong( stringValue, 10 );
        return new Date( dateLong );
    }

    private Double parseNumberValue( String stringValue ) {
        return Double.parseDouble( stringValue );
    }
}
