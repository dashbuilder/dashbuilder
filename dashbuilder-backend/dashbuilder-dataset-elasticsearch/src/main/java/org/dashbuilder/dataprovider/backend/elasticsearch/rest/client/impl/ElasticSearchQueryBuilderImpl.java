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
package org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.impl;

import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.ElasticSearchQueryBuilder;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.impl.jest.ElasticSearchJestClient;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model.Query;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.date.TimeFrame;
import org.dashbuilder.dataset.filter.*;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.Interval;
import org.dashbuilder.dataset.impl.ElasticSearchDataSetMetadata;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>Default query builder implementation.</p>
 * <p>It tries to use Filters as much as possible, as they're faster than queries.</p>
 * 
 * <p>If the resulting query only contains filters, wrap them into a MATCH_ALL filtered query, as aggregations do not work with post-filters (just filters, no queries). </p>
 */
public class ElasticSearchQueryBuilderImpl implements ElasticSearchQueryBuilder<ElasticSearchQueryBuilderImpl> {
    
    private ElasticSearchDataSetMetadata metadata;
    private List<DataSetGroup> groups= new LinkedList<DataSetGroup>();
    private List<DataSetFilter> filters = new LinkedList<DataSetFilter>();

    private enum Operator {
        AND, OR, NOT;
    }
    
    @Override
    public ElasticSearchQueryBuilderImpl metadata(ElasticSearchDataSetMetadata metadata) {
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
        
        List<Interval> intervals = group.getSelectedIntervalList();
        for (Interval interval : intervals) {
            Query _result = null;
            if (ColumnType.LABEL.equals(columnType)) {
                String filterValue = interval.getName();
                _result = new Query(sourceId, Query.Type.TERM);
                _result.setParam(Query.Parameter.VALUE.name(), filterValue);
            } else if (ColumnType.NUMBER.equals(columnType) || (ColumnType.DATE.equals(columnType))) {
                Object maxValue = interval.getMaxValue();
                Object minValue = interval.getMinValue();
                Object value0 = ElasticSearchJestClient.formatValue(sourceId, metadata, minValue);
                Object value1 = ElasticSearchJestClient.formatValue(sourceId, metadata, maxValue);
                _result = new Query(sourceId, Query.Type.RANGE);
                _result.setParam(Query.Parameter.GT.name(), value0);
                _result.setParam(Query.Parameter.LT.name(), value1);
            } else {
                throw new IllegalArgumentException("Not supported type [" + columnType.name() + "] for column id [" + sourceId + "].");
            }
            
            result.add(_result);
        }
        
        return result;
    }

    private Query joinQueriesAndFilters(List<Query> queries, Operator operator) {
        if (queries == null || queries.isEmpty()) return null;

        Query result = null;

        List<Query> subQueries = getQueries(queries);
        List<Query> subFilters = getFilters(queries);
        boolean existFilters = !subFilters.isEmpty();
        boolean existQueries = !subQueries.isEmpty();
        boolean onlyOneQuery = queries.size() == 1;
        
        String boolType = getBooleanQueryType(operator);
        Query.Type filterOperator = getType(operator);
        if (!existFilters) {
            if (onlyOneQuery && !operator.equals(Operator.NOT)) {
                // Single query.
                return queries.get(0);
            } else {
                // Multiple queries.
                result = new Query(Query.Type.BOOL);
                result.setParam(boolType, queries);
            }
        }
        else if (!existQueries) {

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
        }
        else {

            Query booleanQuery = null;
            if (subQueries.size() == 1) {
                booleanQuery = subQueries.get(0);
            } else {
                booleanQuery = new Query(Query.Type.BOOL);
                booleanQuery.setParam(boolType, subQueries);
            }

            Query filter = null;
            if (subFilters.size() == 1) {
                filter = subFilters.get(0);
            }
            else {
                new Query(filterOperator);
                filter.setParam(Query.Parameter.FILTERS.name(), subFilters);
            }

            // Join queries and filters using a FILTERED query.
            result = new Query(Query.Type.FILTERED);
            result.setParam(Query.Parameter.QUERY.name(), booleanQuery);
            result.setParam(Query.Parameter.FILTER.name(), filter);
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

    protected Query buildColumnCoreFunctionFilter(CoreFunctionFilter filter, ElasticSearchDataSetMetadata metadata) {
        String columnId = filter.getColumnId();
        ColumnType columnType = metadata.getColumnType(columnId);

        Query result = null;

        CoreFunctionType type = filter.getType();
        List<Comparable> params = filter.getParameters();

        if (CoreFunctionType.IS_NULL.equals(type)) {
            
            result = new Query(Query.Type.NOT);
            Query existResult = new Query(columnId, Query.Type.EXISTS);
            result.setParam(Query.Parameter.FILTER.name(), existResult);
            
        } else if (CoreFunctionType.NOT_NULL.equals(type)) {
            
            result = new Query(columnId, Query.Type.EXISTS);
            
        } else if (CoreFunctionType.EQUALS_TO.equals(type)) {
            
            Object value = ElasticSearchJestClient.formatValue(columnId, metadata, params.get(0));

            if (ColumnType.LABEL.equals(columnType)) {
                result = new Query(columnId, Query.Type.TERM);
            } else {
                result = new Query(columnId, Query.Type.MATCH);
            }
            result.setParam(Query.Parameter.VALUE.name(), value);
            
        } else if (CoreFunctionType.NOT_EQUALS_TO.equals(type)) {
            
            Object value = ElasticSearchJestClient.formatValue(columnId, metadata, params.get(0));

            if (ColumnType.LABEL.equals(columnType)) {
                Query resultMatch = new Query(columnId, Query.Type.TERM);
                resultMatch.setParam(Query.Parameter.VALUE.name(), value);
                result = new Query(columnId, Query.Type.NOT);
                result.setParam(Query.Parameter.FILTER.name(), resultMatch);
            } else {
                Query resultMatch = new Query(columnId, Query.Type.MATCH);
                resultMatch.setParam(Query.Parameter.VALUE.name(), value);
                result = new Query(columnId, Query.Type.BOOL);
                result.setParam(Query.Parameter.MUST_NOT.name(), asList(resultMatch));
            }
            
        } else if (CoreFunctionType.LOWER_THAN.equals(type)) {

            Object value = ElasticSearchJestClient.formatValue(columnId, metadata, params.get(0));
            result = new Query(columnId, Query.Type.RANGE);
            result.setParam(Query.Parameter.LT.name(), value);
            
        } else if (CoreFunctionType.LOWER_OR_EQUALS_TO.equals(type)) {

            Object value = ElasticSearchJestClient.formatValue(columnId, metadata, params.get(0));
            result = new Query(columnId, Query.Type.RANGE);
            result.setParam(Query.Parameter.LTE.name(), value);
            
        } else if (CoreFunctionType.GREATER_THAN.equals(type)) {

            Object value = ElasticSearchJestClient.formatValue(columnId, metadata, params.get(0));
            result = new Query(columnId, Query.Type.RANGE);
            result.setParam(Query.Parameter.GT.name(), value);
            
        } else if (CoreFunctionType.GREATER_OR_EQUALS_TO.equals(type)) {

            Object value = ElasticSearchJestClient.formatValue(columnId, metadata, params.get(0));
            result = new Query(columnId, Query.Type.RANGE);
            result.setParam(Query.Parameter.GTE.name(), value);
            
        } else if (CoreFunctionType.BETWEEN.equals(type)) {

            Object value0 = ElasticSearchJestClient.formatValue(columnId, metadata, params.get(0));
            Object value1 = ElasticSearchJestClient.formatValue(columnId, metadata, params.get(1));
            result = new Query(columnId, Query.Type.RANGE);
            result.setParam(Query.Parameter.GT.name(), value0);
            result.setParam(Query.Parameter.LT.name(), value1);
            
        } else if (CoreFunctionType.TIME_FRAME.equals(type)) {

            TimeFrame timeFrame = TimeFrame.parse(params.get(0).toString());
            if (timeFrame != null) {
                java.sql.Date past = new java.sql.Date(timeFrame.getFrom().getTimeInstant().getTime());
                java.sql.Date future = new java.sql.Date(timeFrame.getTo().getTimeInstant().getTime());
                result = new Query(columnId, Query.Type.RANGE);
                result.setParam(Query.Parameter.GTE.name(), past);
                result.setParam(Query.Parameter.LTE.name(), future);
            }

        } else {
            throw new IllegalArgumentException("Core function type not supported: " + type);
        }

        return result;
    }
    
    protected List<Query> asList(Query... queries) {
        if (queries == null) return null;
        
        List<Query> result = new LinkedList<Query>();
        Collections.addAll(result, queries);
        
        return result;
    }
}
