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
package org.dashbuilder.dataset.engine.function;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
public class AggregateFunctionManagerImpl implements AggregateFunctionManager {

    /**
     * The built-in aggregate function registry.
     */
    protected Map<String,AggregateFunction> functionMap = new HashMap<String,AggregateFunction>();

    @PostConstruct
    protected void init() {
        // For some reason javax.enterprise.inject.Instance<AggregateFunction> is not working on client side.
        // As a work-around all the available functions are registered statically.
        registerFunction(new CountFunction());
        registerFunction(new DistinctFunction());
        registerFunction(new SumFunction());
        registerFunction(new CountFunction());
        registerFunction(new AverageFunction());
        registerFunction(new MaxFunction());
        registerFunction(new MinFunction());
    }

    public Collection<AggregateFunction> getAllFunctions() {
        return functionMap.values();
    }

    public AggregateFunction getFunctionByCode(String code) {
        return functionMap.get(code);
    }

    public void registerFunction(AggregateFunction function) {
        functionMap.put(function.getCode(), function);
    }
}
