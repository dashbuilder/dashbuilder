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
package org.dashbuilder.uuid;

import javax.inject.Inject;

import org.dashbuilder.test.ShrinkWrapHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.fest.assertions.api.Assertions.*;

/**
 * UUIDs generator tool
 */
@RunWith(Arquillian.class)
public class UUIDGeneratorTest {

    @Deployment
    public static Archive<?> createTestArchive()  {
        return ShrinkWrapHelper.createJavaArchive()
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private UUIDGenerator uuidGenerator;

    @Test
    public void testUUIDLength() {
        String uuid = uuidGenerator.newUuidBase64();
        assertThat(uuid.length()).isEqualTo(22);
    }

    @Test
    public void testURLSafe() {
        String uuid = uuidGenerator.newUuidBase64();
        assertThat(uuid.contains("\u003d")).isFalse();
        assertThat(uuid.contains("\u002f")).isFalse();
        assertThat(uuid.contains("\u002b")).isFalse();
        assertThat(uuid.contains("\u0026")).isFalse();
    }

    @Test
    public void testDecoding() {
        String uuid = uuidGenerator.newUuid();
        String base64 = uuidGenerator.uuidToBase64(uuid);
        String back = uuidGenerator.uuidFromBase64(base64);
        assertThat(back).isEqualTo(uuid);
    }

}

