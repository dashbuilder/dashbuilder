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
package org.dashbuilder.dataset.engine.function;

import java.util.List;

/**
 * It calculates the number of occurrences inside a given collection.
 */
public class CountFunction extends AbstractFunction {

    /**
     * The code of the function.
     */
    public static final String CODE = "count";

    protected String code;

    public CountFunction() {
        super();
        code = CODE;
    }

    public String getCode() {
        return code;
    }

    public double aggregate(List values) {
        if (values == null || values.isEmpty()) return 0;
        return values.size();
    }

    public double aggregate(List values, List<Integer> rows) {
        if (rows == null) return aggregate(values);
        if (rows.isEmpty()) return 0;
        return rows.size();
    }
}