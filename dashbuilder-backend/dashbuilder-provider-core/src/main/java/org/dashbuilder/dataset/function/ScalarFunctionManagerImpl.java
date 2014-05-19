/**
 * Copyright (C) 2012 JBoss Inc
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
package org.dashbuilder.dataset.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
public class ScalarFunctionManagerImpl implements ScalarFunctionManager {

    /**
     * The built-in scalar function registry.
     */
    protected Map<String,ScalarFunction> scalarFunctionMap = new HashMap<String,ScalarFunction>();

    @Inject
    protected Instance<ScalarFunction> _scalarFunctions;

    @PostConstruct
    protected void init() {
        for (ScalarFunction sf: _scalarFunctions) {
            scalarFunctionMap.put(sf.getCode(), sf);
        }
    }

    public Collection<ScalarFunction> getAllScalarFunctions() {
        return scalarFunctionMap.values();
    }

    public ScalarFunction getScalarFunctionByCode(String code) {
        return scalarFunctionMap.get(code);
    }

    public void registerScalarFunction(String code, ScalarFunction function) {
        scalarFunctionMap.put(code, function);
    }
}
