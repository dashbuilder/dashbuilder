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
package org.dashbuilder.dataprovider.backend.elasticsearch;

import org.dashbuilder.dataprovider.backend.elasticsearch.rest.ElasticSearchQueryBuilder;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.ElasticSearchQueryBuilderImpl;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.util.ElasticSearchUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

// TODO: CDI injections
@ApplicationScoped
public class ElasticSearchQueryBuilderFactory {

    /*@Inject
    protected Instance<ElasticSearchQueryBuilder> builders;*/

    @Inject
    protected ElasticSearchValueTypeMapper typeMapper;

    @Inject
    protected ElasticSearchUtils utils;
    
    public ElasticSearchQueryBuilder newQueryBuilder() {
        return new ElasticSearchQueryBuilderImpl(typeMapper, utils);
        /*return builders.get();*/
    }
}
