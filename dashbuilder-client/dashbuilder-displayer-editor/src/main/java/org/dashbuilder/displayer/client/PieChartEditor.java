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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.displayer.PieChartDisplayer;
import org.dashbuilder.displayer.client.widgets.CommonAttributesEditor;

/**
 * Pie chart editor.
 */
@ApplicationScoped
@Named("piechart_editor")
public class PieChartEditor extends AbstractDisplayerEditor<PieChartDisplayer> {

    interface EditorBinder extends UiBinder<Widget, PieChartEditor>{}
    private static final EditorBinder uiBinder = GWT.create(EditorBinder.class);

    @UiField
    HorizontalPanel commonAttributesPanel;

    private CommonAttributesEditor commonAttributesEditor;

    public PieChartEditor() {

        // Init the editor from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));

        // TODO This nested widget setup really requires an MVP oriented approach
        commonAttributesEditor = new CommonAttributesEditor();
        commonAttributesPanel.add( commonAttributesEditor );
    }

    @Override
    public void setDataDisplayer( PieChartDisplayer dataDisplayer ) {
        super.setDataDisplayer( dataDisplayer );
        commonAttributesEditor.setDataDisplayer( dataDisplayer );
    }

    @Override
    public void setListener( DisplayerEditorListener listener ) {
        super.setListener( listener );
        commonAttributesEditor.setListener( listener );
    }
}
