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

/**
 * Base class for the implementation of scalar functions.
 */
public abstract class AbstractFunction implements ScalarFunction {

    protected int precission;

    public AbstractFunction() {
        precission = -1;
    }

    public boolean isTypeSupported(Class type) {
        return true;
    }

    public double round(double value, int precission) {
        if (precission < 0) return value;
        double result = value * Math.pow(10, precission);
        long temp = Math.round(result);
        result = temp / Math.pow(10, precission);
        return result;
    }
}