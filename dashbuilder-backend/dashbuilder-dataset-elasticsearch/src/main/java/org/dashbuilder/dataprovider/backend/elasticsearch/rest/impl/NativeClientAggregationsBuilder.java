/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl;

import org.dashbuilder.dataprovider.backend.elasticsearch.ElasticSearchClientFactory;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.ElasticSearchClient;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.exception.ElasticSearchClientGenericException;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.SearchRequest;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.util.ElasticSearchUtils;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.IntervalBuilderDynamicDate;
import org.dashbuilder.dataset.date.DayOfWeek;
import org.dashbuilder.dataset.date.Month;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.dataset.group.*;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.ValuesSourceMetricsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.min.MinBuilder;

import java.text.MessageFormat;
import java.util.*;

/**
 * Helper class for the ELS native client that provides the different <code>AggregationBuilder</code>'s given a group operation.
 *
 * @since 0.5.0
 */
public class NativeClientAggregationsBuilder {

    private final ElasticSearchClientFactory clientFactory;
    private final IntervalBuilderDynamicDate intervalBuilder;
    private final ElasticSearchUtils utils;
    private final DataSetMetadata metadata;
    private final List<DataColumn> columns; 
    private final SearchRequest request;
    
    public NativeClientAggregationsBuilder(ElasticSearchClientFactory clientFactory,
                                           IntervalBuilderDynamicDate intervalBuilder,
                                           ElasticSearchUtils utils,
                                           DataSetMetadata metadata,
                                           List<DataColumn> columns,
                                           SearchRequest request) {
        this.clientFactory = clientFactory;
        this.intervalBuilder = intervalBuilder;
        this.utils = utils;
        this.metadata = metadata;
        this.columns = columns;
        this.request = request;
    }

    public List<AbstractAggregationBuilder> build( DataSetGroup groupOp ) throws ElasticSearchClientGenericException {

        ColumnGroup columnGroup = groupOp.getColumnGroup();
        List<GroupFunction> groupFunctions = groupOp.getGroupFunctions();

        List<GroupFunction> columnPickUps = new LinkedList<GroupFunction>();

        // Group functions.
        final List<AbstractAggregationBuilder> aggregationBuilders = new LinkedList<>();
        
        if (groupFunctions != null && !groupFunctions.isEmpty()) {
            
            for (GroupFunction groupFunction : groupFunctions) {
                
                // If not a "group" lookup operation (not the groupby column), seralize the core function.
                if (groupFunction.getFunction() != null) {

                    ValuesSourceMetricsAggregationBuilder b = serializeCoreFunction( groupFunction );
                    
                    if ( null != b ) {
                        
                        aggregationBuilders.add( b );
                        
                    }
                    
                } else {
                    
                    columnPickUps.add(groupFunction);
                    
                }
            }
            
        }

        // Group by columns.
        if (columnGroup != null) {
            
            String columnId = columnGroup.getColumnId();
            String sourceId = columnGroup.getSourceId();

            // Check that all column pickups are also column groups.
            if (!columnPickUps.isEmpty()) {
                
                for (GroupFunction groupFunction : columnPickUps) {
                    
                    if (groupFunction.getFunction() == null && sourceId.equals(groupFunction.getSourceId())) {
                        columnId = groupFunction.getColumnId();
                        
                        if ( !existColumnInMetadataDef( sourceId ) ) {
                            throw new RuntimeException("Aggregation by column [" + sourceId + "] failed. No column with the given id.");
                        }
                    }
                    
                }
                
            }

            AbstractAggregationBuilder b = serializeGroupByFunction( columnGroup, columnId, aggregationBuilders );
            
            if ( null != b ) {
                
                return new ArrayList<AbstractAggregationBuilder>() {{
                    add( b );
                }};
                
            }

        } else {

            // If there is no group function, cannot use column pickups.
            if ( !columnPickUps.isEmpty() ) {
                throw new RuntimeException("Column [" + columnPickUps.get(0).getSourceId() + "] pickup  failed. " +
                        "No grouping is set for this column.");
            }

        }

        return aggregationBuilders;
        
    }

    /**
     * <p>Serializes a core function.</p>
     * <p>Example of SUM function serialization:</p>
     * <code>
     *     "column_id" : {
     *          "sum" : { "field" : "change" }
     *     }
     * </code>
     * @return
     */
    protected ValuesSourceMetricsAggregationBuilder serializeCoreFunction(GroupFunction groupFunction) {

        if ( null != groupFunction ) {

            String sourceId = groupFunction.getSourceId();
            
            if ( sourceId != null && !existColumnInMetadataDef( sourceId ) ) {
                throw new RuntimeException("Aggregation by column [" + sourceId + "] failed. No column with the given id.");
            }
            
            if (sourceId == null) {
                sourceId = metadata.getColumnId(0);
            }
            
            if (sourceId == null) {
                throw new IllegalArgumentException("Aggregation from unknown column id.");
            }
            
            String columnId = groupFunction.getColumnId();
            if (columnId == null) columnId = sourceId;

            AggregateFunctionType type = groupFunction.getFunction();
            ColumnType sourceColumnType = metadata.getColumnType(sourceId);
            // ColumnType resultingColumnType = sourceColumnType.equals(ColumnType.DATE) ? ColumnType.DATE : ColumnType.NUMBER;
            ValuesSourceMetricsAggregationBuilder result = null;
            
            switch (type) {
                
                case COUNT:
                    result = AggregationBuilders.count(columnId).field(sourceId);
                    break;
                case DISTINCT:
                    result = AggregationBuilders.cardinality(columnId).field(sourceId);
                    break;
                case AVERAGE:
                    result = AggregationBuilders.avg(columnId).field(sourceId);
                    break;
                case SUM:
                    result = AggregationBuilders.sum(columnId).field(sourceId);
                    break;
                case MIN:
                    result = AggregationBuilders.min(columnId).field(sourceId);
                    break;
                case MAX:
                    result = AggregationBuilders.max(columnId).field(sourceId);
                    break;

            }
            
            if ( null == result ) {
                throw new RuntimeException( "Core function not supported as an Elastic Search aggregation [type=" + type.name() + "]" );
            }

            return result;
        }

        return null;
    }

    protected AbstractAggregationBuilder serializeGroupByFunction( ColumnGroup columnGroup, 
                                               String resultingColumnId,
                                               List<AbstractAggregationBuilder> aggregationBuilders ) throws ElasticSearchClientGenericException {
        if (columnGroup == null || metadata == null) {
            return null;
        }

        DataSetDef dataSetDef = metadata.getDefinition();
        String sourceId = columnGroup.getSourceId();
        if (resultingColumnId == null) resultingColumnId = sourceId;
        boolean asc = columnGroup.isAscendingOrder();
        ColumnType columnType = metadata.getColumnType(sourceId);
        GroupStrategy groupStrategy = columnGroup.getStrategy();
        String intervalSize = columnGroup.getIntervalSize();
        boolean areEmptyIntervalsAllowed = columnGroup.areEmptyIntervalsAllowed();
        int minDocCount = areEmptyIntervalsAllowed ? 0 : 1;
        // TODO: Support for maxIntervals.
        int maxIntervals = columnGroup.getMaxIntervals();
        
        AbstractAggregationBuilder theResult = null;
        
        if (ColumnType.LABEL.equals(columnType)) {
            
            // Translate into a TERMS aggregation.
            TermsBuilder termsBuilder = new TermsBuilder( resultingColumnId )
                    .field( sourceId )
                    .size( 0 )
                    .minDocCount( minDocCount )
                    .order( Terms.Order.term(asc) );

            addSubAggregations( termsBuilder, aggregationBuilders );
            
            // Add the resulting data set column.
            if (columns != null) {
                DataColumn column = getColumn(resultingColumnId);
                column.setColumnGroup(new ColumnGroup(sourceId, resultingColumnId, columnGroup.getStrategy(), columnGroup.getMaxIntervals(), columnGroup.getIntervalSize()));
            }

            theResult = termsBuilder;
            
        } else if (ColumnType.NUMBER.equals(columnType)) {
            
            // Translate into a HISTOGRAM aggregation.
            HistogramBuilder histogramBuilder = new HistogramBuilder( resultingColumnId )
                    .field( sourceId )
                    .minDocCount( minDocCount )
                    .order( asc ? Histogram.Order.KEY_ASC : Histogram.Order.KEY_DESC );

            if ( null != intervalSize ) {
                histogramBuilder.interval( Long.parseLong(intervalSize) );
            }

            addSubAggregations( histogramBuilder, aggregationBuilders );
            
            // Add the resulting dataset column.
            if (columns != null) {
                DataColumn column = getColumn(resultingColumnId);
                column.setColumnGroup(new ColumnGroup(sourceId, resultingColumnId, columnGroup.getStrategy(), columnGroup.getMaxIntervals(), columnGroup.getIntervalSize()));
            }
            
            theResult = histogramBuilder;
            
        } else if (ColumnType.DATE.equals(columnType)) {
            DateIntervalType dateIntervalType = null;

            // Fixed grouping -> use term field aggregation with a date format script.
            if (GroupStrategy.FIXED.equals(columnGroup.getStrategy())) {
                // String dateColumnPattern = dataSetDef.getPattern(sourceId);

                if (intervalSize != null) {
                    dateIntervalType = DateIntervalType.valueOf(intervalSize);
                }

                if (dateIntervalType == null) {
                    throw new RuntimeException("Column [" + columnGroup.getColumnId() + "] is type Date and grouped using a fixed strategy, but the ate interval type is not specified. Please specify it.");
                }

                String[] scripts = buildIntervalExtractorScript(sourceId, columnGroup);
                String valueScript = scripts[0];
                String orderScript = scripts[1];

                TermsBuilder termsBuilder = new TermsBuilder( resultingColumnId )
                        .size( 0 )
                        .minDocCount( minDocCount )
                        .script( new Script( valueScript ) );

                if ( null == orderScript ) {
                    
                    termsBuilder.order( Terms.Order.term(asc) );
                    
                } else {
                    
                    termsBuilder.order(  Terms.Order.aggregation( "_sortOrder", true) );
                    
                }
                
                addSubAggregations( termsBuilder, aggregationBuilders );
                
                if ( null != orderScript ) {

                    MinBuilder orderAggBuilder = new MinBuilder( "_sortOrder" );
                    orderAggBuilder.script( new Script( orderScript ) );
                    
                    termsBuilder.subAggregation( orderAggBuilder );
                    
                }

                theResult = termsBuilder;
               
            }

            // Dynamic grouping -> use date histograms.
            if (GroupStrategy.DYNAMIC.equals(columnGroup.getStrategy())) {
                
                if (intervalSize != null) {

                    // If interval size specified by the lookup group operation, use it.
                    dateIntervalType = DateIntervalType.valueOf(intervalSize);
                    
                } else {

                    // If interval size is not specified by the lookup group operation, calculate the current date limits for index document's date field and the interval size that fits..
                    try {

                        ElasticSearchClient anotherClient = clientFactory.newClient( (ElasticSearchDataSetDef) metadata.getDefinition() );
                                
                        Date[] limits = utils.calculateDateLimits(anotherClient, metadata, columnGroup.getSourceId(), this.request != null ? this.request.getQuery() : null);
                        if (limits != null) {
                            dateIntervalType = intervalBuilder.calculateIntervalSize(limits[0], limits[1], columnGroup);
                        }
                        
                        anotherClient.close();
                        
                    } catch (Exception e) {
                        throw new ElasticSearchClientGenericException("Cannot calculate date limits.", e);
                    }
                }

                if (dateIntervalType == null) {
                    // Not limits found. No matches. No matter the interval type used.
                    dateIntervalType = DateIntervalType.MILLISECOND;
                }

                String intervalPattern = DateIntervalPattern.getPattern(dateIntervalType);
                
                DateHistogramBuilder builder = new DateHistogramBuilder( resultingColumnId )
                        .field( sourceId )
                        .interval( getInterval( dateIntervalType ) )
                        .format( intervalPattern )
                        .minDocCount( minDocCount )
                        .order( asc ? Histogram.Order.KEY_ASC : Histogram.Order.KEY_DESC );

                addSubAggregations( builder, aggregationBuilders );
                
                theResult = builder;
            }

            // Add the resulting dataset column.
            if (columns != null) {
                
                DataColumn column = getColumn(resultingColumnId);
                column.setColumnType(ColumnType.LABEL);
                column.setIntervalType(dateIntervalType.name());
                ColumnGroup cg = new ColumnGroup(sourceId, resultingColumnId, columnGroup.getStrategy(), columnGroup.getMaxIntervals(), columnGroup.getIntervalSize());
                cg.setEmptyIntervalsAllowed(areEmptyIntervalsAllowed);
                cg.setFirstMonthOfYear(columnGroup.getFirstMonthOfYear());
                cg.setFirstDayOfWeek(columnGroup.getFirstDayOfWeek());
                column.setColumnGroup(cg);
                
            }
            
        } else {
            
            throw new RuntimeException("No translation supported for column group with sourceId [" + sourceId + "] and group strategy [" + groupStrategy.name() + "].");
            
        }
        
        return theResult;
    }
    
    private void addSubAggregations( AggregationBuilder parent, 
                                     List<AbstractAggregationBuilder> aggregationBuilders ) {
        
        if ( null != aggregationBuilders && !aggregationBuilders.isEmpty() ) {
            
            for ( AbstractAggregationBuilder b : aggregationBuilders ) {
                parent.subAggregation( b );
            }
            
        }
        
    }

    protected DateHistogramInterval getInterval(DateIntervalType dateIntervalType) {
        String intervalExpression;
        switch (dateIntervalType) {
            case MILLISECOND:
                intervalExpression = "0.001s";
                break;
            case HUNDRETH:
                intervalExpression = "0.01s";
                break;
            case TENTH:
                intervalExpression = "0.1s";
                break;
            case SECOND:
                intervalExpression = "1s";
                break;
            case MINUTE:
                intervalExpression = "1m";
                break;
            case HOUR:
                intervalExpression = "1h";
                break;
            case DAY:
                intervalExpression = "1d";
                break;
            case DAY_OF_WEEK:
                intervalExpression = "1d";
                break;
            case WEEK:
                intervalExpression = "1w";
                break;
            case MONTH:
                intervalExpression = "1M";
                break;
            case QUARTER:
                intervalExpression = "1q";
                break;
            case YEAR:
                intervalExpression = "1y";
                break;
            case DECADE:
                intervalExpression = "10y";
                break;
            case CENTURY:
                intervalExpression = "100y";
                break;
            case MILLENIUM:
                intervalExpression = "1000y";
                break;
            default:
                throw new RuntimeException("No interval mapping for date interval type [" + dateIntervalType.name() + "].");
        }
        
        return new DateHistogramInterval( intervalExpression );
    }

    private String[] buildIntervalExtractorScript(String sourceId, ColumnGroup columnGroup) {
        DateIntervalType intervalType = DateIntervalType.getByName(columnGroup.getIntervalSize());
        Month firstMonth = columnGroup.getFirstMonthOfYear();
        DayOfWeek firstDayOfWeek = columnGroup.getFirstDayOfWeek();

        String script = "new Date(doc[\"{0}\"].value).toCalendar().";
        switch (intervalType) {
            case QUARTER:
                // For quarters use this pseudocode script: <code>quarter = round-up(date.month / 3)</code>
                script = "ceil( ( " + script + "get(Calendar.MONTH) + 1 ) / 3 ).toInteger()";
                break;
            case MONTH:
                script = script + "get(Calendar.MONTH) + 1";
                break;
            case DAY_OF_WEEK:
                script = script + "get(Calendar.DAY_OF_WEEK)";
                break;
            case HOUR:
                script = script + "get(Calendar.HOUR_OF_DAY)";
                break;
            case MINUTE:
                script = script + "get(Calendar.MINUTE)";
                break;
            case SECOND:
                script = script + "get(Calendar.SECOND)";
                break;
            default:
                throw new UnsupportedOperationException("Fixed grouping strategy by interval type " + intervalType.name() + " is not supported.");
        }

        String valueScript = MessageFormat.format( script, sourceId );

        String orderScript = null;

        if (firstMonth != null && intervalType.equals(DateIntervalType.MONTH)) {
            int firstMonthIndex = firstMonth.getIndex();
            int[] positions = buildPositionsArray(firstMonthIndex, 12, columnGroup.isAscendingOrder());
            orderScript = "month="+valueScript+".toInteger(); list = "+Arrays.toString(positions)+"; list.indexOf(month)";
        }

        if (firstDayOfWeek!= null && intervalType.equals(DateIntervalType.DAY_OF_WEEK)) {
            int firstDayIndex = firstDayOfWeek.getIndex();
            int[] positions = buildPositionsArray(firstDayIndex, 7, columnGroup.isAscendingOrder());
            orderScript = "day="+valueScript+".toInteger(); list = "+Arrays.toString(positions)+"; list.indexOf(day)";
        }

        return new String[] { valueScript, orderScript};
    }

    private int[] buildPositionsArray( int firstElementIndex, 
                                       int end, 
                                       boolean asc) {
        int[] positions = new int[end];

        for (int x = 0, month = firstElementIndex; x < end; x++) {
            
            if ( month > end ) {
                month = 1;
            }
            
            if ( month < 1 ) {
                month = end;
            }
            
            positions[x] = month;
            
            if ( asc ) {
                month ++;
            } else {
                month--;
            }
            
        }

        return positions;

    }

    protected boolean existColumnInMetadataDef( String name ) {
        if (name == null || metadata == null) return false;

        int cols = metadata.getNumberOfColumns();
        for (int x = 0; x < cols; x++) {
            String colName = metadata.getColumnId(x);
            if (name.equals(colName)) return true;
        }
        return false;
    }

    protected DataColumn getColumn(String columnId) {
        if (columns != null && columnId != null && !columns.isEmpty()) {
            for (DataColumn column : columns) {
                if (columnId.equals(column.getId())) return column;
            }
        }
        return null;
    }


}
