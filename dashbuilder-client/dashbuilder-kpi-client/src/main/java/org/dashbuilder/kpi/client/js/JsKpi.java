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
package org.dashbuilder.kpi.client.js;

/**
 * {
 *     "title": "Expenses amount per department",
 *     "type": "piechart",
 *     "renderer": "google",
 *     "xAxis": {
 *          "columnId": "department",
 *          "displayName": "Department"
 *      }
 *     "yAxis": {
 *          "columnId": "amount",
 *          "displayName": "Total amount"
 *      }
 *     "dataSet": {
 *        "columns": [{
 *              "id": "department",
 *              "type": "label",
 *              "values": {"sales", engineering", "administration", "HR"}
 *            }, {
 *              "id": "amount",
 *              "type": "label",
 *              "values": {"10300.45", "9000.00", "3022.44", "22223.56"}
 *          }]
 *      }
 * }
 */

public interface JsKpi {

    String getUID();
    String getTitle();
    String getProviderUid();
    JsDataDisplayer getDataDisplayer();
}
