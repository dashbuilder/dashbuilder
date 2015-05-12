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
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.dashbuilder.dataset.group.AggregateFunction;
import org.dashbuilder.dataset.group.AggregateFunctionManager;
import org.dashbuilder.dataset.group.AggregateFunctionType;

@ApplicationScoped
public class AggregateFunctionManagerImpl implements AggregateFunctionManager {

    /**
     * The built-in aggregate function registry.
     */
    protected Map<AggregateFunctionType, AggregateFunction> functionMap
            = new EnumMap<AggregateFunctionType, AggregateFunction>(AggregateFunctionType.class);

    @PostConstruct
    protected void init() {
        // For some reason javax.enterprise.inject.Instance<AggregateFunction> is not working on client side.
        // As a work-around all the available functions are registered statically.
        registerFunction(new CountFunction());
        registerFunction(new DistinctFunction());
        registerFunction(new SumFunction());
        registerFunction(new AverageFunction());
        registerFunction(new MaxFunction());
        registerFunction(new MinFunction());
    }

    public Collection<AggregateFunction> getAllFunctions() {
        return functionMap.values();
    }

    public AggregateFunction getFunctionByType(AggregateFunctionType type) {
        return functionMap.get(type);
    }

    public void registerFunction(AggregateFunction function) {
        functionMap.put(function.getType(), function);
    }
}
