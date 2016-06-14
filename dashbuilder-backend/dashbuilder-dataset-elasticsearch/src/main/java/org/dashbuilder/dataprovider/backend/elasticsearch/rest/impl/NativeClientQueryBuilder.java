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

import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.Query;
import org.elasticsearch.index.query.*;

import java.util.Collection;


/**
 * Helper class for the ELS native client that provides the different <code>QueryBuilder</code>'s given a query.
 *
 * @since 0.5.0
 */
public class NativeClientQueryBuilder {

    public NativeClientQueryBuilder( ) {
    }

    public QueryBuilder build( Query query ) {

        return translate( query );
        
    }

    private QueryBuilder translate( Query query ) {
        
        if ( query == null ) {
            return null;
        }

        Query.Type type = query.getType();

        switch (type) {
            
            case BOOL:
                return translateBool(query);
            case MATCH:
                return translateMatch(query);
            case MATCH_ALL:
                return translateMatchAll(query);
            case WILDCARD:
                return translateWildcard(query);
            case QUERY_STRING:
                return translateQueryString(query);
            case FILTERED:
                return translateFiltered(query);
            case AND:
                return translateAnd(query);
            case OR:
                return translateOr(query);
            case NOT:
                return translateNot(query);
            case EXISTS:
                return translateExists(query);
            case TERM:
                return translateTerm(query);
            case TERMS:
                return translateTerms(query);
            case RANGE:
                return translateRange(query);
            
        }

        return null;
    }

    
    private ExistsQueryBuilder translateExists(Query query) {
        if (query == null) return null;
        String field = query.getField();
        return new ExistsQueryBuilder( field );
    }

    private TermQueryBuilder translateTerm(Query query) {
        if (query == null) return null;

        String field = query.getField();
        Object value = query.getParam(Query.Parameter.VALUE.name());

        return new TermQueryBuilder( field, value );
    }
    
    private TermsQueryBuilder translateTerms(Query query) {
        if (query == null) return null;

        String field = query.getField();
        Collection<String> terms = (Collection<String>) query.getParam(Query.Parameter.VALUE.name());
        return new TermsQueryBuilder( field, terms);
    }

    private RangeQueryBuilder translateRange(Query query) {
        if (query == null) return null;

        String field = query.getField();

        RangeQueryBuilder builder = new RangeQueryBuilder( field );
        
        Object lt = query.getParam(Query.Parameter.LT.name());
        if ( null != lt ) {
            builder.lt( lt );
        }
        Object lte = query.getParam(Query.Parameter.LTE.name());
        if ( null != lte ) {
            builder.lte( lte );
        }
        Object gt = query.getParam(Query.Parameter.GT.name());
        if ( null != gt ) {
            builder.gt( gt );
        }
        Object gte = query.getParam(Query.Parameter.GTE.name());
        if ( null != gte ) {
            builder.gte( gte );
        }
       
        return builder;
    }

    private BoolQueryBuilder translateAnd(Query query) {
        return translateLogical( query, 1  );
    }

    private BoolQueryBuilder translateOr(Query query) {
        return translateLogical( query, 2  );
    }

    private BoolQueryBuilder translateNot(Query query) {
        return translateBooleanQueries( new BoolQueryBuilder(), 3, query.getParam(Query.Parameter.FILTER.name()) );
    }

    private BoolQueryBuilder translateLogical( Query query, int op ) {
        return translateBooleanQueries( new BoolQueryBuilder(), op, query.getParam(Query.Parameter.FILTERS.name()) );
    }
    
    @SuppressWarnings("unchecked")
    private BoolQueryBuilder translateBooleanQueries( BoolQueryBuilder builder, int op, Object queryArguments ) {

        if ( op == 2 ) {
            
            builder.minimumNumberShouldMatch( 1 );
            
        }

        if ( null != queryArguments && queryArguments instanceof Collection ) {

            Collection<Query> queries = (Collection<Query>) queryArguments;
            for ( Query q : queries ) {

                QueryBuilder queryBuilder = translate( q );
                applyLogicalOperator( builder, op, queryBuilder );
            }


        } else if ( null != queryArguments && queryArguments instanceof Query ) {

            QueryBuilder queryBuilder = translate( (Query) queryArguments );
            applyLogicalOperator( builder, op, queryBuilder );

        
        } else if ( null != queryArguments && queryArguments instanceof QueryBuilder ) {

            applyLogicalOperator( builder, op, (QueryBuilder) queryArguments );
            
        }
        
        return builder;
    }
    
    private void applyLogicalOperator( BoolQueryBuilder builder, 
                                       int op,
                                       QueryBuilder argument ) {
        
        if ( op == 1 ) {
            
            builder.must( argument );
            
        } else if ( op == 2 ) {

            builder.should( argument );
            
        } else if ( op == 3 ) {

            builder.mustNot( argument );
        }
        
    }

    private BoolQueryBuilder translateBool(Query query) {
        if (query == null) return null;

        BoolQueryBuilder builder = new BoolQueryBuilder();
        
        // Must clauses.
        translateBooleanQueries( builder, 1, query.getParam(Query.Parameter.MUST.name()) );

        // Should clauses.
        translateBooleanQueries( builder, 2, query.getParam(Query.Parameter.SHOULD.name()) );

        // Must Not clauses.
        translateBooleanQueries( builder, 3, query.getParam(Query.Parameter.MUST_NOT.name()) );

      
        return builder;
    }

    private BoolQueryBuilder translateFiltered(Query query) {
        if (query == null) return null;

        Query _query = (Query) query.getParam(Query.Parameter.QUERY.name());
        Query filter = (Query) query.getParam(Query.Parameter.FILTER.name());

        BoolQueryBuilder builder = new BoolQueryBuilder();

        QueryBuilder queryBuilder = translate( _query );
        
        if ( null != queryBuilder ) {
            builder.must( queryBuilder );
        }

        QueryBuilder filterBuilder = translate( filter );

        if ( null != filter ) {
            
            builder.filter( filterBuilder );
            
        }

        return builder;
    }

    private MatchQueryBuilder translateMatch( Query query ) {
        if (query == null) return null;

        String field = query.getField();
        Object value = query.getParam(Query.Parameter.VALUE.name());
        String operator = (String) query.getParam(Query.Parameter.OPERATOR.name());

        MatchQueryBuilder builder = new MatchQueryBuilder( field, value );
        if ( null != operator ) {
            builder.operator( getMatchOperator( operator ) );
            builder.minimumShouldMatch( "1" );
        }
       
        return builder;
    }
    
    private MatchQueryBuilder.Operator getMatchOperator( String op ) {
        
        if ( "OR".equalsIgnoreCase( op) ) {
            
            return MatchQueryBuilder.Operator.OR;
        }

        return MatchQueryBuilder.Operator.AND;
    }

    private WildcardQueryBuilder translateWildcard( Query query ) {
        if (query == null) return null;

        String field = query.getField();
        Object value = query.getParam( Query.Parameter.VALUE.name() );
        return new WildcardQueryBuilder( field, (String) value );        
    }

    private QueryStringQueryBuilder translateQueryString( Query query ) {
        if (query == null) return null;

        Object pattern = query.getParam(Query.Parameter.QUERY.name());
        Object defField = query.getParam(Query.Parameter.DEFAULT_FIELD.name());
        Object defOp = query.getParam(Query.Parameter.DEFAULT_OPERATOR.name());
        Object lowerCase = query.getParam(Query.Parameter.LOWERCASE_EXPANDED_TERMS.name());

        return new QueryStringQueryBuilder( pattern.toString() )
                .defaultField( defField.toString() )
                .defaultOperator( getQueryOperator( defOp.toString() ) )
                .lowercaseExpandedTerms( Boolean.valueOf( lowerCase.toString() ) );
        
    }

    private QueryStringQueryBuilder.Operator getQueryOperator( String op ) {

        if ( "OR".equalsIgnoreCase( op ) ) {

            return QueryStringQueryBuilder.Operator.OR;
        }

        return QueryStringQueryBuilder.Operator.AND;
    }

    private MatchAllQueryBuilder translateMatchAll( Query query ) {
        if (query == null) return null;
        
        return new MatchAllQueryBuilder();
    }

}
