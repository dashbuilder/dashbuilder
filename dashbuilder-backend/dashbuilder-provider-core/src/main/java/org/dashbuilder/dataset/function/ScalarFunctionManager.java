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

import java.util.Collection;

/**
 * Manager class that it keeps a registry of ScalarFunction instances.
 */
public interface ScalarFunctionManager {

    /**
     * Get all the scalar functions registered..
     */
    Collection<ScalarFunction> getAllScalarFunctions();

    /**
     * Get a scalar function by its code.
     */
    ScalarFunction getScalarFunctionByCode(String code);

    /**
     * Register a scalar function.
     */
    void registerScalarFunction(String code, ScalarFunction function);
}

