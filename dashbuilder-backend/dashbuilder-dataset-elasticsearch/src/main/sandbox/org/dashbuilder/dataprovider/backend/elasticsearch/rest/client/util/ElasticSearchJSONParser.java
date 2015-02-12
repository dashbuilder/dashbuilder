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
package org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.util;

import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model.FieldMappingResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * <p>Utility class for parsing some ElasticSearch JSON response body.</p>
 * <p>Currently it parses these response queries:</p>
 * <ul>
 *     <li>Index Mappings</li>
 * </ul>
 *  
 * @since 0.3.0
 */
public class ElasticSearchJSONParser {

    private static final String KEYWORD_MAPPINGS = "mappings";
    private static final String KEYWORD_PROPERTIES = "properties";
    private static final String KEYWORD_INDEX = "index";
    private static final String KEYWORD_TYPE = "type";


    /**
     * <p>Parses field mappings.</p>
     * <p>This method expect a JSON input as:</p>
     * <code>
     *     {"act": {
     *          "properties": {
     *              "play_name": {
     *              "type": "string",
     *               "index": "not_analyzed"
     *              },
     *              "speech_number": {
     *              "type": "integer"
     *              }
     *          }
     *      }}
     * </code>
     * 
     * @param json The JSON input data.
     * @return The collection of field mappings model.
     */
    public FieldMappingResponse[] parseFieldMappings(String json) throws ParseException {
        if (json == null) return null;
        
        // Parse the JSON data.
        JSONParser parser=new JSONParser();
        JSONObject mappings = (JSONObject) parser.parse(json);
        if (mappings == null || mappings.size() == 0) return null;
        
        Collection<FieldMappingResponse> result = new LinkedList<FieldMappingResponse>();


        Set<Map.Entry<String,JSONObject>> mappingEntries =  mappings.entrySet();
        if (mappingEntries.size() == 0) return null;
        for (Map.Entry<String,JSONObject> mappingEntry : mappingEntries) {
            JSONObject typeMappingObj = mappingEntry.getValue();
            JSONObject propertiesObj = (JSONObject) typeMappingObj.get(KEYWORD_PROPERTIES);

            Set<Map.Entry<String,JSONObject>> propertiesEntries =  propertiesObj.entrySet();
            if (propertiesEntries.size() == 0) return null;

            for (Map.Entry<String,JSONObject> propertyEntry : propertiesEntries) {
                String fieldName = propertyEntry.getKey();
                JSONObject fieldMappings = propertyEntry.getValue();
                FieldMappingResponse fieldMapping = parseField(fieldName, fieldMappings);
                result.add(fieldMapping);
            }
        }
        
        
        return result.toArray(new FieldMappingResponse[result.size()]);
    }

    public FieldMappingResponse parseField(String fieldName, JSONObject fieldMappings) {
        if (fieldName == null || fieldName.trim().length() == 0 || fieldMappings == null) return null;

        Set<Map.Entry<String,String>> propertiesEntries =  fieldMappings.entrySet();
        if (propertiesEntries.size() == 0) return null;

        FieldMappingResponse.IndexType index = null;
        FieldMappingResponse.FieldType type = null;
        for (Map.Entry<String,String> propertyEntry : propertiesEntries) {
            String propertyName = propertyEntry.getKey();
            if (KEYWORD_INDEX.equalsIgnoreCase(propertyName)) {
                index = FieldMappingResponse.IndexType.valueOf(propertyEntry.getValue().toUpperCase());
            } else if (KEYWORD_TYPE.equalsIgnoreCase(propertyName)) {
                type = FieldMappingResponse.FieldType.valueOf(propertyEntry.getValue().toUpperCase());

            }
        }

        return new FieldMappingResponse(fieldName, type, index);
    }
}
