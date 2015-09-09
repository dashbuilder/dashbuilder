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
package org.dashbuilder.dataprovider.backend.elasticsearch.rest.util;

import org.apache.commons.io.IOUtils;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.FieldMappingResponse;
import org.dashbuilder.test.ShrinkWrapHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.StringWriter;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Unit test for ElasticSearchJSONParser.java
 */
@RunWith(Arquillian.class)
public class ElasticSearchJSONParserTest {

    private static transient Logger log = LoggerFactory.getLogger(ElasticSearchJSONParserTest.class.getName());

    @Deployment
    public static Archive<?> createTestArchive()  {
        return ShrinkWrapHelper.createJavaArchive()
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    ElasticSearchJSONParser elasticSearchJSONParser;

    @Test
    public void testParseMappings() throws Exception {
        InputStream dataStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/dashbuilder/dataprovider/backend/elasticsearch/rest/client/util/fieldMappings.json");
        StringWriter writer = new StringWriter();
        IOUtils.copy(dataStream, writer, "UTF-8");
        String json = writer.toString();

        // Parse the JSON data.
        FieldMappingResponse[] mappings = elasticSearchJSONParser.parseFieldMappings(json);
        assertThat(mappings).isNotEmpty();
        assertThat(mappings).hasSize(2);

        FieldMappingResponse prop1 = mappings[0];
        assertThat(prop1).isNotNull();
        assertThat(prop1.getName()).isEqualTo("play_name");
        assertThat(prop1.getDataType()).isEqualTo(FieldMappingResponse.FieldType.STRING);
        assertThat(prop1.getIndexType()).isEqualTo(FieldMappingResponse.IndexType.NOT_ANALYZED);
        FieldMappingResponse prop2 = mappings[1];
        assertThat(prop2).isNotNull();
        assertThat(prop2.getName()).isEqualTo("speech_number");
        assertThat(prop2.getDataType()).isEqualTo(FieldMappingResponse.FieldType.INTEGER);
        assertThat(prop2.getIndexType()).isEqualTo(FieldMappingResponse.IndexType.ANALYZED);
    }
}
