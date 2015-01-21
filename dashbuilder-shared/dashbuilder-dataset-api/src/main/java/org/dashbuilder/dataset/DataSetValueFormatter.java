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
package org.dashbuilder.dataset;

public interface DataSetValueFormatter {

    /**
     * Get a data set cell value and returns its string representation. There are three possible results:
     * <ul>
     *     <li>LABEL: return the string as is</li>
     *     <li>NUMBER: return the number formatted as #,###.##</li>
     *     <li>DATE: return the date formatted as yyyy-MM-dd HH:mm:ss</li>
     * </ul>
     */
    String formatValue(Object value);

    /**
     * Parse the given string and returns an object according to the type specified. Allowed values of the str parameter:
     *
     * <ul>
     *     <li>yyyy-MM-dd HH:mm:ss for DATE types</li>
     *     <li>#,###.## for NUMBER types</li>
     *     <li>Any string is allowed for LABEL types</li>
     * @return
     */
    Comparable parseValue(String str);
}
