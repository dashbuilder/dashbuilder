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
package org.dashbuilder.dataset.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.*;

import javax.enterprise.context.Dependent;

// TODO
@Dependent
public class DataSetExplorer extends Composite {

    interface DataSetExplorerBinder extends UiBinder<Widget, DataSetExplorer> {}
    private static DataSetExplorerBinder uiBinder = GWT.create(DataSetExplorerBinder.class);

    public DataSetExplorer() {
        initWidget(uiBinder.createAndBindUi(this));
    }

}
