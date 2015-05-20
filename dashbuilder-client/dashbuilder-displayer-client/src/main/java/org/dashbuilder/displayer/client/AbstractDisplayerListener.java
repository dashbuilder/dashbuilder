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
package org.dashbuilder.displayer.client;

import java.util.List;

import org.dashbuilder.dataset.client.DataSetClientServiceError;
import org.dashbuilder.dataset.group.DataSetGroup;

/**
 * Base class for implementing custom displayer listeners.
 */
public abstract class AbstractDisplayerListener implements DisplayerListener {

    @Override public void onDraw(Displayer displayer) {

    }

    @Override public void onRedraw(Displayer displayer) {

    }

    @Override public void onClose(Displayer displayer) {

    }

    @Override public void onFilterEnabled(Displayer displayer, DataSetGroup groupOp) {

    }

    @Override public void onFilterReset(Displayer displayer, List<DataSetGroup> groupOps) {

    }

    @Override public void onError(Displayer displayer, DataSetClientServiceError error) {

    }
}