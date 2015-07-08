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
package org.dashbuilder.displayer.client.widgets.filter;

import javax.enterprise.context.Dependent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.filter.ColumnFilter;
import org.dashbuilder.dataset.filter.CoreFunctionFilter;
import org.dashbuilder.dataset.filter.CoreFunctionType;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.filter.FilterFactory;
import org.dashbuilder.displayer.client.resources.i18n.CommonConstants;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.ListBox;

@Dependent
public class DataSetFilterEditor extends Composite implements ColumnFilterEditor.Listener {

    public interface Listener {
        void filterChanged(DataSetFilter filter);
    }

    interface Binder extends UiBinder<Widget, DataSetFilterEditor> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    Listener listener = null;
    DataSetFilter filter = null;
    DataSetMetadata metadata = null;

    @UiField
    ListBox newFilterListBox;

    @UiField
    Panel filterListPanel;

    @UiField
    Button addFilterButton;

    @UiField
    Panel addFilterPanel;

    @UiField
    Icon filterDeleteIcon;

    public DataSetFilterEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        filterDeleteIcon.addDomHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                onNewFilterClosed(event);
            }
        }, ClickEvent.getType());
    }

    public void init(DataSetMetadata metadata, DataSetFilter filter, Listener listener) {
        this.metadata = metadata;
        this.filter = filter;
        this.listener = listener;

        addFilterButton.setVisible(true);
        addFilterPanel.setVisible(false);
        initNewFilterListBox();
        filterListPanel.clear();

        if (filter != null) {
            for (ColumnFilter columnFilter : filter.getColumnFilterList()) {
                ColumnFilterEditor columnFilterEditor = new ColumnFilterEditor();
                columnFilterEditor.init(metadata, columnFilter, this);
                filterListPanel.add(columnFilterEditor);
            }
        }
    }

    protected void initNewFilterListBox() {
        newFilterListBox.clear();
        newFilterListBox.addItem( CommonConstants.INSTANCE.filter_editor_selectcolumn());

        if (metadata != null) {
            for (int i = 0; i < metadata.getNumberOfColumns(); i++) {
                newFilterListBox.addItem(metadata.getColumnId(i));
            }
        }
    }

    // UI events

    @UiHandler(value = "addFilterButton")
    public void onAddFilterClicked(ClickEvent event) {
        addFilterButton.setVisible(false);
        addFilterPanel.setVisible(true);
    }

    public void onNewFilterClosed(ClickEvent event) {
        addFilterButton.setVisible(true);
        addFilterPanel.setVisible(false);
    }

    @UiHandler(value = "newFilterListBox")
    public void onNewFilterSelected(ChangeEvent changeEvent) {
        int selectedIdx = newFilterListBox.getSelectedIndex();
        if (selectedIdx > 0) {
            String columnId = metadata.getColumnId(selectedIdx-1);
            ColumnType columnType = metadata.getColumnType(selectedIdx - 1);
            CoreFunctionFilter columnFilter = FilterFactory.createCoreFunctionFilter(
                    columnId, columnType,
                    ColumnType.DATE.equals(columnType) ? CoreFunctionType.TIME_FRAME : CoreFunctionType.NOT_EQUALS_TO);

            if (filter == null) filter = new DataSetFilter();
            filter.addFilterColumn(columnFilter);

            ColumnFilterEditor columnFilterEditor = new ColumnFilterEditor();
            columnFilterEditor.init(metadata, columnFilter, this);
            columnFilterEditor.expand();
            filterListPanel.add(columnFilterEditor);

            newFilterListBox.setSelectedIndex(0);
            addFilterPanel.setVisible(false);
            addFilterButton.setVisible(true);

            columnFilterChanged(columnFilterEditor);
        }
    }

    // ColumnFilterEditor listener

    public void columnFilterDeleted(ColumnFilterEditor editor) {
        addFilterButton.setVisible(true);
        addFilterPanel.setVisible(false);
        filterListPanel.remove(editor);

        Integer index = filter.getColumnFilterIdx(editor.getFilter());
        if (index != null) {
            filter.getColumnFilterList().remove(index.intValue());

            if (listener != null) {
                listener.filterChanged(filter);
            }
        }
    }

    public void columnFilterChanged(ColumnFilterEditor editor) {
        if (listener != null) {
            listener.filterChanged(filter);
        }
    }
}
