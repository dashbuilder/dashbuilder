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
package org.dashbuilder.model.displayer;

import java.util.List;

public interface XAxisChart {

    int INTERVALS_SORT_CRITERIA_LABEL = 0;
    int INTERVALS_SORT_CRITERIA_VALUE = 1;
    int INTERVALS_SORT_ORDER_NONE = 0;
    int INTERVALS_SORT_ORDER_ASC = 1;
    int INTERVALS_SORT_ORDER_DESC = -1;

    XAxis getXAxis();
    List<YAxis> getYAxes();
}
