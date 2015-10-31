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
package org.dashbuilder.client.widgets.dataset.editor;

import com.google.gwt.editor.client.EditorDelegate;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.widgets.dataset.event.FilterChangedEvent;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.displayer.client.widgets.filter.DataSetFilterEditor;
import org.uberfire.client.mvp.UberView;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * <p>Data Set filter editor presenter.</p>
 * 
 * @since 0.4.0 
 */
@Dependent
public class DataSetDefFilterEditor implements IsWidget, org.dashbuilder.dataset.client.editor.DataSetDefFilterEditor {
    
    public interface View extends UberView<DataSetDefFilterEditor> {
        View setWidget(IsWidget filterView);
    }
    
    Event<FilterChangedEvent> filterChangedEvent;
    public View view;

    DataSetFilterEditor filterEditor;
    DataSetFilter value;
    
    @Inject
    public DataSetDefFilterEditor(final Event<FilterChangedEvent> filterChangedEvent, final View view) {
        this.filterChangedEvent = filterChangedEvent;
        this.view = view;
    }

    @PostConstruct
    public void init() {
        view.init(this);
    }

    @Override
    public void init(final DataSetMetadata metadata) {
        filterEditor  = new DataSetFilterEditor();
        initFilterEditor(metadata);
    }
    
    void initFilterEditor(final DataSetMetadata metadata) {
        view.setWidget(filterEditor);
        filterEditor.init(metadata, value != null ? value.cloneInstance() : null, filterListener);
    }
    
    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    /*************************************************************
     ** GWT EDITOR CONTRACT METHODS **
     *************************************************************/

    @Override
    public void flush() {

    }

    @Override
    public void onPropertyChange(final String... paths) {

    }

    @Override
    public void setValue(final DataSetFilter value) {
        this.value = value != null ? value.cloneInstance() : null;
    }

    @Override
    public void setDelegate(final EditorDelegate<DataSetFilter> delegate) {
        // No delegation required.
    }

    void onValueChanged(final DataSetFilter value) {

        // Set the new value.
        DataSetFilter before = this.value;
        setValue(value);

        // Fire the value change event.
        filterChangedEvent.fire(new FilterChangedEvent(DataSetDefFilterEditor.this, before, value));

    }
    
    private final DataSetFilterEditor.Listener filterListener = new DataSetFilterEditor.Listener() {
        @Override
        public void filterChanged(final DataSetFilter filter) {
            onValueChanged(filter);
        }
    };

}
