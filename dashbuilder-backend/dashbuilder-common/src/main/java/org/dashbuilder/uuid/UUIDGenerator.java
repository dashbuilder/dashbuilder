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

import java.util.UUID;
import javax.crypto.KeyGenerator;
import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.codec.binary.StringUtils;
import org.dashbuilder.service.UIDGeneratorService;
import org.jboss.errai.bus.server.annotations.Service;

/**
 * UUIDs generator tool
 */
@ApplicationScoped
public class UUIDGenerator implements UIDGeneratorService {

     public String generateUUID() {
         UUID uuid = UUID.randomUUID();
         return uuid.toString();
     }
}
