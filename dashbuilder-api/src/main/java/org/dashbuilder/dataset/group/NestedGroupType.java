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
package org.dashbuilder.dataset.group;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Type of nested group types. A nested group is a group operation performed just after another
 * group operation (called the parent group).
 */
@Portable
public enum NestedGroupType {

    /**
     * The group results are joined with the parent data set group.
     *
     * <p>Example:
     *  <ul>
     *  <li>.group(PIPELINE)</li>
     *  <li>.group(COUNTRY).join()</li>
     *  <li>.sum(AMOUNT, "TOTAL")</li>
     *  </ul>
     *
     * <p>Group by PIPELINE:</p>
     * <pre>
     *
     *   --------------------------
     *   | PIPELINE   | TOTAL     |
     *   --------------------------
     *   | EARLY      | 369.09    |
     *   | ADVANCED   | 246.06    |
     *   --------------------------
     * </pre>
     *
     * <p>Group by COUNTRY:</p>
     * <pre>
     *
     *   ------------------------
     *   | COUNTRY  | TOTAL     |
     *   ------------------------
     *   | USA      | 369.09    |
     *   | UK       | 246.06    |
     *   | Spain    | 369.09    |
     *   ------------------------
     * </pre>
     *
     * <p>Result:</p>
     * <pre>
     *
     *   ---------------------------------------
     *   | PIPELINE   |  COUNTRY   | TOTAL     |
     *   ---------------------------------------
     *   | EARLY      |  USA       | 123.03    |
     *   | EARLY      |  UK        | 123.03    |
     *   | EARLY      |  Spain     | 123.03    |
     *   | ADVANCED   |  USA       | 123.03    |
     *   | ADVANCED   |  Spain     | 123.03    |
     *   | STANDBY    |  USA       | 123.03    |
     *   | STANDBY    |  UK        | 123.03    |
     *   | STANDBY    |  Spain     | 123.03    |
     *   ---------------------------------------
     * </pre>
     * <p>A joined data set grouped by PIPELINE/COUNTRY is returned.
     */
    JOIN,

    /**
     * The resulting nested group data is melt with the parent group.
     * This means a new column is generated for each group interval.
     *
     * <p>Example:
     *  <ul>
     *  <li>.group(CLOSING_DATE, "MONTH").fixed(MONTH)</li>
     *  <li>.group(CLOSING_DATE, "TOTAL $interval", 10, YEAR).meld()</li>
     *  <li>.sum(AMOUNT)</li>
     *  </ul>
     *
     * <p>Result:</p>
     * <pre>
     *
     *   -----------------------------------------------------------
     *   | MONTH       |  TOTAL 2014   | TOTAL 2015 | TOTAL 2016   |
     *   ----------------------------------------------------------|
     *   | January     |  100.00       | 123.03     | 345.45       |
     *   | February    |  100.00       | 123.03     | 345.45       |
     *   | March       |  610.10       | 123.03     | 345.45       |
     *   | April       |  500.00       | 123.03     | 345.45       |
     *   | May         |  100.00       | 123.03     | 345.45       |
     *   | June        |  100.00       | 123.03     | 345.45       |
     *   | July        |  100.00       | 123.03     | 345.45       |
     *   | August      |  100.00       | 123.03     | 345.45       |
     *   | September   |  0.00         | 123.03     | 345.45       |
     *   | October     |  100.00       | 123.03     | 345.45       |
     *   | November    |  100.00       | 123.03     | 345.45       |
     *   | December    |  200.00       | 123.03     | 345.45       |
     *   -----------------------------------------------------------
     * </pre>
     */
    MELD
}
