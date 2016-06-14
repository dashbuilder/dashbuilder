/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.dataprovider.backend.elasticsearch.rest.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>An ElasticSearch query representation.</p>
 * 
 * <p>Generates a group of ElasticSearch queries and filters to run from a the previous pasing of a DataSetFilter.</p>
 * 
 * <p>Parameters for each query type:</p>
 * <ul>
 *     <li>
 *         <p>Query - Boolean</p>
 *         <ul>
 *             <li>must - Contains a List of other Query objects.</li>
 *             <li>must_not - Contains a List of other Query objects.</li>
 *             <li>should - Contains a List of other Query objects.</li>
 *         </ul>
 *     </li>
 *     <li>
 *         <p>Query - Match</p>
 *         <ul>
 *             <li>value - The value to match.</li>
 *         </ul>
 *     </li>
 *     <li>
 *         <p>Query - Match All</p>
 *     </li>
 *     <li>
 *         <p>Query - Filtered</p>
 *         <ul>
 *             <li>query - The query.</li>
 *             <li>filter - The filter for this query.</li>
 *         </ul>
 *     </li>
 *     <li>
 *         <p>Filter - And</p>
 *         <ul>
 *             <li>filters - Contains a List of other filters.</li>
 *         </ul>
 *     </li>
 *     <li>
 *         <p>Filter - Or</p>
 *         <ul>
 *             <li>filters - Contains a List of other filters.</li>
 *         </ul>
 *     </li>
 *     <li>
 *         <p>Filter - Not</p>
 *         <ul>
 *             <li>filter - Contains another query instance that represents a filter.</li>
 *         </ul>
 *     </li>
 *     <li>
 *         <p>Filter - Exist</p>
 *         <p>Does not contain any parameter. Only check if the value for <code>field</code> exists.</p>
 *     </li>
 *     <li>
 *         <p>Filter - Term</p>
 *         <ul>
 *             <li>value - The value for the term to search.</li>
 *         </ul>
 *     </li>
 *     <li>
 *         <p>Filter - Range</p>
 *         <ul>
 *             <li>gt - The value for greater than.</li>
 *             <li>gte - The value for greater than or equals.</li>
 *             <li>lt - The value for lower than.</li>
 *             <li>lte - The value for lower than or equals.</li>
 *         </ul>
 *     </li>

 * </ul>
 */
public class Query {
    
    public static enum Parameter {
        MUST, MUST_NOT, SHOULD, VALUE, QUERY, FILTER, FILTERS, GT, GTE, LT, LTE,
        DEFAULT_FIELD, DEFAULT_OPERATOR, LOWERCASE_EXPANDED_TERMS, OPERATOR;
    }
    
    public static enum Type {
        QUERY(null), 
            BOOL(QUERY),
            MATCH(QUERY),
            MATCH_ALL(QUERY),
            WILDCARD(QUERY),
            QUERY_STRING(QUERY),
            FILTERED(QUERY),
        FILTER(null),
            AND(FILTER),
            OR(FILTER),
            NOT(FILTER),
            EXISTS(FILTER),
            TERM(FILTER),
            TERMS(FILTER),
            RANGE(FILTER);
        
        private Type type;
        
        private Type(Type type) {
            this.type = type;
        }

        public Type getType() {
            return type;
        }
    }

    private final  Type type;
    private final  String field;
    private final  Map<String, Object> params;

    public Query(Type type) {
        this.type = type;
        this.field = null;
        params = new HashMap<String, Object>();
    }

    public Query(String field, Type type) {
        this.type = type;
        this.field = field;
        params = new HashMap<String, Object>();
    }

    public Type getType() {
        return type;
    }

    public String getField() {
        return field;
    }

    public Query setParam(String key, Object value) {
        params.put(key, value);
        return this;
    }
    
    public Object getParam(String key) {
        if (params.isEmpty()) return null;
        
        return params.get(key);
    }

    @Override
    public String toString() {
        return toString(-1);
    }

    public String toString(int level) {
        level++;
        StringBuilder result = new StringBuilder();
        result.append(indent(level)).append("|").append(getType().name()).append("\n");
        if (!params.isEmpty()) {
            for (Map.Entry<String, Object> paramEntry : params.entrySet()) {
                String key = paramEntry.getKey();
                Object value = paramEntry.getValue();
                if (value instanceof Query) {
                    result.append(indent(level)).append("|").append(key).append(":\n");
                    result.append(((Query)value).toString(level));
                }
                else if (value instanceof List) {
                    result.append(indent(level)).append("|").append(key).append(":\n");
                    result.append(collectionToString(((List<Query>)value), level));
                }
                else {
                    result.append(indent(level)).append("|").append(key).append(" -> ").append(value).append("\n");
                }
            }
        }
        return result.toString();
    }
    
    protected String indent(int level) {
        StringBuilder result = new StringBuilder();
        for (int x = 0; x < level; x++) {
            result.append("--");
        }
        return result.toString();
    }

    protected String collectionToString(Collection<Query> queries, int level) {
        StringBuilder result = new StringBuilder();
        for (Query query : queries) {
            result.append(query.toString(level));
        }
        return result.toString();
    }
}
