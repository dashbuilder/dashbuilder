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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerLocator;

public class DisplayerSettingsJSONMarshaller {

    private static final String JSON_DATASET_PREFIX = "dataSet";
    private static final String JSON_DATASET_LOOKUP_PREFIX = "dataSetLookup";

    private static final String SETTINGS_UUID = "uuid";

    private DataSetJSONMarshaller dataSetJSONMarshaller;
    private DataSetLookupJSONMarshaller dataSetLookupJSONMarshaller;

    @Inject
    public DisplayerSettingsJSONMarshaller(DataSetJSONMarshaller dataSetJSONMarshaller,
            DataSetLookupJSONMarshaller dataSetLookupJSONMarshaller) {
        this.dataSetJSONMarshaller = dataSetJSONMarshaller;
        this.dataSetLookupJSONMarshaller = dataSetLookupJSONMarshaller;
    }

    public DisplayerSettingsJSONMarshaller() {
        dataSetJSONMarshaller = new DataSetJSONMarshaller();
        dataSetLookupJSONMarshaller = new DataSetLookupJSONMarshaller();
    }

    public DisplayerSettings fromJsonString( String jsonString ) {
        DisplayerSettings ds = new DisplayerSettings();

        if ( !StringUtils.isBlank( jsonString ) ) {

            JSONObject parseResult = JSONParser.parseStrict( jsonString ).isObject();

            if ( parseResult != null ) {

                // UUID
                JSONValue uuidValue = parseResult.get( SETTINGS_UUID );
                ds.setUUID( uuidValue != null && uuidValue.isString() != null ? uuidValue.isString().stringValue() : null );

                // First look if a dataset 'on-the-fly' has been specified
                JSONValue data = parseResult.get( JSON_DATASET_PREFIX );
                if ( data != null ) {
                    DataSet dataSet = dataSetJSONMarshaller.fromJson( data.isObject() );
                    ds.setDataSet( dataSet );

                    // Remove from the json input so that it doesn't end up in the settings map.
                    parseResult.put( JSON_DATASET_PREFIX, null );

                // If none was found, look for a dataset lookup definition
                } else if ( (data = parseResult.get( JSON_DATASET_LOOKUP_PREFIX )) != null ) {
                    DataSetLookup dataSetLookup = dataSetLookupJSONMarshaller.fromJson( data.isObject() );
                    ds.setDataSetLookup( dataSetLookup );

                    // Remove from the json input so that it doesn't end up in the settings map.
                    parseResult.put( JSON_DATASET_LOOKUP_PREFIX, null );

                } else {
                    throw new RuntimeException( "Either a DataSet or a DataSetLookup should be specified" );
                }

                // Now parse all other settings
                ds.setSettingsFlatMap( parseSettingsFromJson( parseResult ) );
            }
        }
        return ds;
    }

    public String toJsonString(DisplayerSettings displayerSettings) {
        return toJsonObject( displayerSettings ).toString();
    }

    public JSONObject toJsonObject( DisplayerSettings displayerSettings ) {
        JSONObject json = new JSONObject(  );

        // UUID
        json.put( SETTINGS_UUID, displayerSettings.getUUID() != null ? new JSONString( displayerSettings.getUUID() ) : null );

        for ( Map.Entry<String, String> entry : displayerSettings.getSettingsFlatMap().entrySet() ) {
                setNodeValue( json, entry.getKey(), entry.getValue() );
        }

        // DataSet or DataSetLookup
        DataSetLookup dataSetLookup = null;
        DataSet dataSet = displayerSettings.getDataSet();
        if ( dataSet != null ) {
            json.put( JSON_DATASET_PREFIX, dataSetJSONMarshaller.toJson( dataSet ) );
        } else if ( ( dataSetLookup = displayerSettings.getDataSetLookup() ) != null ) {
            json.put( JSON_DATASET_LOOKUP_PREFIX, dataSetLookupJSONMarshaller.toJson( dataSetLookup ) );
        } else throw new RuntimeException( "Either a DataSet or a DataSetLookup should be specified" );

        return json;
    }

    private void setNodeValue( JSONObject node, String path, String value ) {
        if ( node == null || StringUtils.isBlank( path ) || value == null ) return;

        int separatorIndex = path.lastIndexOf(".");
        String nodesPath = separatorIndex > 0 ? path.substring( 0, separatorIndex ) : "";
        String leaf = separatorIndex > 0 ? path.substring( separatorIndex + 1 ) : path;

        JSONObject _node = findNode( node, nodesPath, true );
        _node.put( leaf, new JSONString( value ) );
    }

    private String getNodeValue( JSONObject node, String path ) {
        if ( node == null || StringUtils.isBlank( path ) ) return null;

        int separatorIndex = path.lastIndexOf(".");
        String subNodesPath = separatorIndex > 0 ? path.substring( 0, separatorIndex ) : "";
        String leaf = separatorIndex > 0 ? path.substring( separatorIndex + 1 ) : path;

        JSONObject childNode = findNode( node, subNodesPath, false );
        String value = null;
        if ( childNode != null) {
            JSONValue jsonValue = childNode.get( leaf );
            if (jsonValue != null && jsonValue.isString() != null) value = jsonValue.isString().stringValue();
        }
        return value;
    }

    private JSONObject findNode(JSONObject parent, String path, boolean createPath) {
        if ( parent == null ) return null;
        if ( StringUtils.isBlank( path ) ) return parent;

        int separatorIndex = path.indexOf(".");
        String strChildNode = separatorIndex > 0 ? path.substring( 0, separatorIndex ) : path;
        String remainingNodes = separatorIndex > 0 ? path.substring( separatorIndex + 1 ) : "";

        JSONObject childNode = (JSONObject) parent.get( strChildNode );
        if ( childNode == null && createPath ) {
            childNode = new JSONObject();
            parent.put( strChildNode, childNode );
        }
        return findNode( childNode, remainingNodes, createPath );
    }

    private Map<String, String> parseSettingsFromJson( JSONObject settingsJson ) {
        Map<String, String> flatSettingsMap = new HashMap<String, String>( 30 );

        if ( settingsJson != null && settingsJson.size() > 0 ) {
            fillRecursive( "", settingsJson, flatSettingsMap );
        }
        return flatSettingsMap;
    }

    private void fillRecursive( String parentPath, JSONObject json, Map<String, String> settings ) {
        String sb = new String( StringUtils.isBlank( parentPath ) ? "" : parentPath + ".");
        for ( String key : json.keySet() ) {
            String path = sb + key;
            JSONValue value = json.get( key );
            if ( value.isObject() != null ) fillRecursive( path, value.isObject(), settings );
            else if ( value.isString() != null ) settings.put( path, value.isString().stringValue() );
        }
    }
}
