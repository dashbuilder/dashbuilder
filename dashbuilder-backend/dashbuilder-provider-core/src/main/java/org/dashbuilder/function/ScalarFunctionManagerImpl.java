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
package org.dashbuilder.function;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
public class ScalarFunctionManagerImpl implements ScalarFunctionManager {

    /**
     * The built-in scalar function registry.
     */
    protected List<ScalarFunction> scalarFunctionList = new ArrayList<ScalarFunction>();

    @Inject
    protected Instance<ScalarFunction> _scalarFunctions;

    @PostConstruct
    protected void init() {
        for (ScalarFunction sf: _scalarFunctions) {
            scalarFunctionList.add(sf);
        }
    }

    public List<ScalarFunction> getAllScalarFunctions() {
        return scalarFunctionList;
    }

    public ScalarFunction getScalarFunctionByCode(String code) {
        for (ScalarFunction scalarFunction : scalarFunctionList) {
            if (scalarFunction.getCode().equals(code)) return scalarFunction;
        }
        return null;
    }
}
