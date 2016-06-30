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
package org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl;

import org.dashbuilder.dataprovider.backend.elasticsearch.ElasticSearchValueTypeMapper;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.ElasticSearchQueryBuilder;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.FieldMappingResponse;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.Query;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.util.ElasticSearchUtils;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.date.TimeFrame;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.dataset.filter.*;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.Interval;

import java.util.*;

/**
 * <p>Default query builder implementation.</p>
 * <p>It tries to use Filters as much as possible, as they're faster than queries.</p>
 * 
 * <p>If the resulting query only contains filters, wrap them into a MATCH_ALL filtered query, as aggregations do not work with post-filters (just filters, no queries). </p>
 */
public class ElasticSearchQueryBuilderImpl implements ElasticSearchQueryBuilder<ElasticSearchQueryBuilderImpl> {

    protected ElasticSearchValueTypeMapper valueTypeMapper;
    protected ElasticSearchUtils utils;
    
    private DataSetMetadata metadata;
    private List<DataSetGroup> groups= new LinkedList<DataSetGroup>();
    private List<DataSetFilter> filters = new LinkedList<DataSetFilter>();

    private enum Operator {
        AND, OR, NOT;
    }

    public ElasticSearchQueryBuilderImpl(ElasticSearchValueTypeMapper valueTypeMapper, ElasticSearchUtils utils) {
        this.valueTypeMapper = valueTypeMapper;
        this.utils = utils;
    }

    @Override
    public ElasticSearchQueryBuilderImpl metadata(DataSetMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public ElasticSearchQueryBuilderImpl groupInterval(List<DataSetGroup> groups) {
        this.groups = groups;
        return this;
    }

    @Override
    public ElasticSearchQueryBuilderImpl filter(List<DataSetFilter> filters) {
        if (filters != null) {
            this.filters.addAll(filters);
        }

        return this;
    }

    @Override
    public Query build() {
        if (filters == null || filters.isEmpty()) return null;
        
        List<Query> queries = new LinkedList<Query>();
        
        // Build query definition for filter operations.
        for (DataSetFilter filter : filters) {
            Query subQuery = build(filter.getColumnFilterList(), Operator.AND);
            if (subQuery == null) continue;
            queries.add(subQuery);
        }

        // Build query definition for interval group selections.
        if (groups != null) {
            for (DataSetGroup group: groups) {
                if (group.isSelect()) {
                    List<Query> subQueries = buildGroupIntervalQuery(group);
                    if (subQueries != null && !subQueries.isEmpty()) {
                        for (Query subQuery : subQueries) {
                            queries.add(subQuery);
                        }
                    }
                }
            }
        }

        Query result = joinQueriesAndFilters(queries, Operator.AND);
        
        // If result is a filter, wrap into a MATCH_ALL filtered query, as EL aggregations requires working with queries.
        if (result != null && isFilter(result)) {
            Query filtered = new Query(Query.Type.FILTERED);
            Query matchAll = new Query(Query.Type.MATCH_ALL);
            filtered.setParam(Query.Parameter.QUERY.name(), matchAll);
            filtered.setParam(Query.Parameter.FILTER.name(), result);
            return filtered;
        }
        return result;
    }
    
    private List<Query> buildGroupIntervalQuery(DataSetGroup group) {
        if (group == null || !group.isSelect()) return null;

        List<Query> result = new LinkedList<Query>();
        String sourceId = group.getColumnGroup().getSourceId();
        ColumnType columnType = metadata.getColumnType(sourceId);
        ElasticSearchDataSetDef def = (ElasticSearchDataSetDef) metadata.getDefinition();
        
        List<Interval> intervals = group.getSelectedIntervalList();
        for (Interval interval : intervals) {
            Query _result = null;
            boolean isLabelCol = ColumnType.LABEL.equals(columnType);
            boolean isNumericCol = ColumnType.NUMBER.equals(columnType);
            boolean isDateCol = ColumnType.DATE.equals(columnType);
            boolean isTextCol = ColumnType.TEXT.equals(columnType);

            if (isTextCol) {
                throw new IllegalArgumentException("Not supported type [" + columnType.name() + "] for column with id [" + sourceId + "] using grouping.");
            }
            
            if (isLabelCol) {
                String filterValue = valueTypeMapper.formatLabel(def, sourceId, interval.getName());
                _result = new Query(sourceId, Query.Type.TERM);
                _result.setParam(Query.Parameter.VALUE.name(), filterValue);
            } else if (isNumericCol || isDateCol) {
                Object maxValue = interval.getMaxValue();
                Object minValue = interval.getMinValue();
                Object value0 = isNumericCol ? 
                        valueTypeMapper.formatNumeric(def, sourceId, (Number) minValue) :
                        valueTypeMapper.formatDate(def, sourceId, (Date) minValue);
                Object value1 = isNumericCol ?
                        valueTypeMapper.formatNumeric(def, sourceId, (Number) maxValue) :
                        valueTypeMapper.formatDate(def, sourceId, (Date) maxValue);
                _result = new Query(sourceId, Query.Type.RANGE);
                _result.setParam(Query.Parameter.GT.name(), value0);
                _result.setParam(Query.Parameter.LT.name(), value1);
            } 

            result.add(_result);
        }
        
        return result;
    }
    
    private String formatValue(ElasticSearchDataSetDef definition, String columnId, Object value) {
        ColumnType columnType = metadata.getColumnType(columnId);
        boolean isLabelCol = ColumnType.LABEL.equals(columnType);
        boolean isNumericCol = ColumnType.NUMBER.equals(columnType);
        boolean isDateCol = ColumnType.DATE.equals(columnType);
        boolean isTextCol = ColumnType.TEXT.equals(columnType);
        if (isTextCol) {
            return valueTypeMapper.formatText(definition, columnId, value != null ? value.toString() : null);
        } else if (isLabelCol) {
            return valueTypeMapper.formatLabel(definition, columnId, value != null ? value.toString() : null);
        } else if (isDateCol) {
            return valueTypeMapper.formatDate(definition, columnId, (Date) value);
        } else if (isNumericCol) {
            return valueTypeMapper.formatNumeric(definition, columnId, (Number) value);
        }

        throw new IllegalArgumentException("Not supported type [" + columnType.name() + "] for column id [" + columnId + "].");
    }

    private Query joinQueriesAndFilters(List<Query> queries, Operator operator) {
        if (queries == null || queries.isEmpty()) return null;

        Query result;

        List<Query> subQueries = getQueries(queries);
        List<Query> subFilters = getFilters(queries);
        boolean existFilters = !subFilters.isEmpty();
        boolean existQueries = !subQueries.isEmpty();
        boolean onlyOneQuery = queries.size() == 1;
        
        String boolType = getBooleanQueryType(operator);
        Query.Type filterOperator = getType(operator);
        
        // Only exist queries (not filters) in the query.
        if (!existFilters) {
            if (onlyOneQuery && !operator.equals(Operator.NOT)) {
                // Single query.
                return queries.get(0);
            } else {
                // Multiple queries.
                result = new Query(Query.Type.BOOL);
                result.setParam(boolType, queries);
            }
        
        // Only exist filters in the query.
        } else if (!existQueries) {

            if (onlyOneQuery && !operator.equals(Operator.NOT)) {
                // Single filter.
                return queries.get(0);
            } else if (onlyOneQuery) {
                // Single NOT filter.
                result = new Query(Query.Type.NOT);
                result.setParam(Query.Parameter.FILTER.name(), queries.get(0));
                        
            } else {
                // Multiple filters.
                result = new Query(filterOperator);
                result.setParam(Query.Parameter.FILTERS.name(), queries);
            }

        // Exists both queries and filters in the query. Let's mix them depending on the logical operator.
        } else {

            // Join all the filters.
            Query filter;
            if (subFilters.size() == 1) {
                filter = subFilters.get(0);
            } else {
                filter = new Query(filterOperator);
                filter.setParam(Query.Parameter.FILTERS.name(), subFilters);
            }

            // Join all the queries.
            Query booleanQuery;
            if (subQueries.size() == 1) {
                booleanQuery = subQueries.get(0);
            } else {
                booleanQuery = new Query(Query.Type.BOOL);
                booleanQuery.setParam(boolType, subQueries);
            }
            
            final boolean isAndOp = operator.equals(Operator.AND);
            if (isAndOp) {
                // For AND operator, join queries and filters using a FILTERED query.
                result = new Query(Query.Type.FILTERED);
                result.setParam(Query.Parameter.QUERY.name(), booleanQuery);
                result.setParam(Query.Parameter.FILTER.name(), filter);
            } else {
                result = new Query(Query.Type.BOOL);
                Query filtered = new Query(Query.Type.FILTERED);
                filtered.setParam(Query.Parameter.FILTER.name(), filter);
                result.setParam(boolType, Arrays.asList(booleanQuery, filtered));
            }

        }

        return result;
    }
    
    protected String getBooleanQueryType(Operator operator) {
        String boolType = null;
        switch (operator) {
            case AND:
                boolType = Query.Parameter.MUST.name();
                break;
            case OR:
                boolType = Query.Parameter.SHOULD.name();
                break;
            case NOT:
                boolType = Query.Parameter.MUST_NOT.name();
                break;
        }
        return boolType;
    }
    
    protected Query.Type getType(Operator operator) {
        Query.Type boolType = null;
        switch (operator) {
            case AND:
                boolType = Query.Type.AND;
                break;
            case OR:
                boolType = Query.Type.OR;
                break;
            case NOT:
                boolType = Query.Type.NOT;
                break;
        }
        return boolType;
    }
    
    private boolean isQuery(Query query) {
        return query != null && query.getType().getType().equals(Query.Type.QUERY);
    }
    
    private boolean isFilter(Query query) {
        return  query != null && query.getType().getType().equals(Query.Type.FILTER);
    }

    private List<Query> getFilters(List<Query> queries) {
        if (queries == null || queries.isEmpty()) return null;
        
        List<Query> result = new LinkedList<Query>();
        for (Query query : queries) {
            if (isFilter(query)) result.add(query);
        }
        return result;
    }

    private List<Query> getQueries(List<Query> queries) {
        if (queries == null || queries.isEmpty()) return null;

        List<Query> result = new LinkedList<Query>();
        for (Query query : queries) {
            if (isQuery(query)) result.add(query);
        }
        return result;
    }

    /**
     * @see <a>http://www.elasticsearch.org/guide/en/elasticsearch/client/java-api/current/query-dsl-filters.html</a>
     *
     */
    private Query build(List<ColumnFilter> filterList, Operator operator) {
        if (filterList == null) return null;

        List<Query> results = new LinkedList<Query>();
        
        for (ColumnFilter filter : filterList) {
            Query result = null;
            
            // Core functions.
            if (filter instanceof CoreFunctionFilter) {
                result = buildColumnCoreFunctionFilter((CoreFunctionFilter) filter, metadata);
            }
            // Logical expressions.
            else if (filter instanceof LogicalExprFilter) {
                LogicalExprFilter f = (LogicalExprFilter) filter;
                LogicalExprType type = f.getLogicalOperator();
                if (LogicalExprType.AND.equals(type)) {
                    result = buildLogicalExpressionFilter(f, Operator.AND);
                }
                else if (LogicalExprType.OR.equals(type)) {
                    result = buildLogicalExpressionFilter(f, Operator.OR);
                }
                else if (LogicalExprType.NOT.equals(type)) {
                    result = buildLogicalExpressionFilter(f, Operator.NOT);
                }
            }
            
            if (result != null) results.add(result);
            
        }
        
        return joinQueriesAndFilters(results, operator);
    }
    
    protected Query buildLogicalExpressionFilter(LogicalExprFilter filter, Operator operator) {
        if (filter == null) return null;
        
        List<ColumnFilter> columnFilters = filter.getLogicalTerms();
        if (columnFilters != null && !columnFilters.isEmpty()) {
            return build(columnFilters, operator) ;
        }
        
        return null;
    }

    protected Query buildColumnCoreFunctionFilter(CoreFunctionFilter filter, DataSetMetadata metadata) {
        String columnId = filter.getColumnId();
        ColumnType columnType = metadata.getColumnType(columnId);
        ElasticSearchDataSetDef def = (ElasticSearchDataSetDef) metadata.getDefinition();
        
        Query result = null;

        CoreFunctionType type = filter.getType();
        List params = filter.getParameters();

        if (CoreFunctionType.IS_NULL.equals(type)) {
            
            result = new Query(Query.Type.NOT);
            Query existResult = new Query(columnId, Query.Type.EXISTS);
            result.setParam(Query.Parameter.FILTER.name(), existResult);
            
        } else if (CoreFunctionType.NOT_NULL.equals(type)) {
            
            result = new Query(columnId, Query.Type.EXISTS);
            
        } else if (CoreFunctionType.EQUALS_TO.equals(type) || CoreFunctionType.IN.equals(type)) {
            
            if (ColumnType.LABEL.equals(columnType)) {
                result = buildTermOrTermsFilter(def, columnId, params);
            } else {
                result = buildBooleanMatchQuery(def, columnId, params);
            }
            
        } else if (CoreFunctionType.NOT_EQUALS_TO.equals(type) || CoreFunctionType.NOT_IN.equals(type)) {
            
            if (ColumnType.LABEL.equals(columnType)) {
                Query resultMatch = buildTermOrTermsFilter(def, columnId, params);
                result = new Query(columnId, Query.Type.NOT);
                result.setParam(Query.Parameter.FILTER.name(), resultMatch);
            } else {
                Query resultMatch = buildBooleanMatchQuery(def, columnId, params);
                result = new Query(columnId, Query.Type.BOOL);
                result.setParam(Query.Parameter.MUST_NOT.name(), asList(resultMatch));
            }
            
        } else if (CoreFunctionType.LIKE_TO.equals(type)) {

            Object value = formatValue(def, columnId, params.get(0));

            if (value != null) {

                if (ColumnType.NUMBER.equals(columnType) || ColumnType.DATE.equals(columnType)) {
                    throw new RuntimeException("The operator LIKE can be applied only for LABEL or TEXT column types. The column [" + columnId + "] is type [" + columnType.name() + "}.");
                }

                // TEXT or LABEL columns.
                String indexType = def.getPattern(columnId);
                if (indexType == null || indexType.trim().length() == 0) {
                    // Default ELS index type for String fields is ANALYZED.
                    indexType = FieldMappingResponse.IndexType.ANALYZED.name();
                }


                // Replace Dashbuilder wildcard characters by the ones used in ELS wildcard query.
                boolean caseSensitive = params.size() < 2 || Boolean.parseBoolean(params.get(1).toString());
                boolean isFieldAnalyzed = FieldMappingResponse.IndexType.ANALYZED.name().equalsIgnoreCase(indexType);

                // Case un-sensitive is not supported for not_analyzed string fields.
                if (!isFieldAnalyzed && !caseSensitive) {
                    throw new RuntimeException("Case unsensitive is not supported for not_analyzed string fields. Field: [" + columnId + "].");
                }

                String pattern = utils.transformPattern(value.toString());
                boolean isLowerCaseExpandedTerms = isFieldAnalyzed && (!caseSensitive);
                result = new Query(columnId, Query.Type.QUERY_STRING);
                result.setParam(Query.Parameter.DEFAULT_FIELD.name(), columnId);
                result.setParam(Query.Parameter.DEFAULT_OPERATOR.name(), "AND");
                result.setParam(Query.Parameter.QUERY.name(), pattern);
                result.setParam(Query.Parameter.LOWERCASE_EXPANDED_TERMS.name(), isLowerCaseExpandedTerms);
            }

        } else if (CoreFunctionType.LOWER_THAN.equals(type)) {

            Object value = formatValue(def, columnId, params.get(0));
            result = new Query(columnId, Query.Type.RANGE);
            result.setParam(Query.Parameter.LT.name(), value);
            
        } else if (CoreFunctionType.LOWER_OR_EQUALS_TO.equals(type)) {

            Object value = formatValue(def, columnId, params.get(0));
            result = new Query(columnId, Query.Type.RANGE);
            result.setParam(Query.Parameter.LTE.name(), value);
            
        } else if (CoreFunctionType.GREATER_THAN.equals(type)) {

            Object value = formatValue(def, columnId, params.get(0));
            result = new Query(columnId, Query.Type.RANGE);
            result.setParam(Query.Parameter.GT.name(), value);
            
        } else if (CoreFunctionType.GREATER_OR_EQUALS_TO.equals(type)) {

            Object value = formatValue(def, columnId, params.get(0));
            result = new Query(columnId, Query.Type.RANGE);
            result.setParam(Query.Parameter.GTE.name(), value);
            
        } else if (CoreFunctionType.BETWEEN.equals(type)) {

            Object value0 = formatValue(def, columnId, params.get(0));
            Object value1 = formatValue(def, columnId, params.get(1));
            result = new Query(columnId, Query.Type.RANGE);
            result.setParam(Query.Parameter.GTE.name(), value0);
            result.setParam(Query.Parameter.LTE.name(), value1);
            
        } else if (CoreFunctionType.TIME_FRAME.equals(type)) {

            TimeFrame timeFrame = TimeFrame.parse(params.get(0).toString());
            if (timeFrame != null) {
                Date past = new Date( timeFrame.getFrom().getTimeInstant().getTime() );
                Date future = new Date( timeFrame.getTo().getTimeInstant().getTime() );
                String pastRaw = valueTypeMapper.formatDate( def, columnId, past );
                String futureRaw = valueTypeMapper.formatDate( def, columnId, future );
                result = new Query(columnId, Query.Type.RANGE);
                result.setParam(Query.Parameter.GTE.name(), pastRaw );
                result.setParam(Query.Parameter.LTE.name(), futureRaw );
            }

        } else {
            throw new IllegalArgumentException("Core function type not supported: " + type);
        }

        return result;
    }
    
    protected Query buildBooleanMatchQuery(ElasticSearchDataSetDef def, String columnId, List params) {
        Query result = new Query(columnId, Query.Type.MATCH);
        StringBuilder terms = new StringBuilder();
        String _pre = params.size() == 1 ? "" : " ";
        for (Object param : params) {
            String paramStr = formatValue(def, columnId, param);
            terms.append(_pre).append(paramStr);
        }
        if(params.size() > 1) {
            result.setParam(Query.Parameter.OPERATOR.name(), "or");
        }
        result.setParam(Query.Parameter.VALUE.name(), terms.toString());
        return result;
    }

    protected Query buildTermOrTermsFilter(ElasticSearchDataSetDef def, String columnId, List params) {
        Query result;
        Object value;
        if (params.size() == 1) {
            value = formatValue(def, columnId, params.get(0));
            result = new Query(columnId, Query.Type.TERM);
        } else {
            result = new Query(columnId, Query.Type.TERMS);
            Collection<String> terms = new ArrayList<String>(params.size());
            for (Object param : params) {
                String paramStr = formatValue(def, columnId, param);
                terms.add(paramStr);
            }
            value = terms;
        }
        result.setParam(Query.Parameter.VALUE.name(), value);
        return result;
    }
    
    protected List<Query> asList(Query... queries) {
        if (queries == null) return null;
        
        List<Query> result = new LinkedList<Query>();
        Collections.addAll(result, queries);
        
        return result;
    }
}
