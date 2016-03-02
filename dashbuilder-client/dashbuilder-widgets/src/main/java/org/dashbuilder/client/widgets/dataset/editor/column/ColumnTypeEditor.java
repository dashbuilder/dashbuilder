/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.client.widgets.dataset.editor.column;

import com.google.gwt.editor.client.EditorDelegate;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.editor.list.DropDownImageListEditor;
import org.dashbuilder.common.client.editor.list.ImageListEditor;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.client.resources.bundles.DataSetClientResources;
import org.dashbuilder.dataset.def.DataColumnDef;
import org.gwtbootstrap3.client.ui.constants.Placement;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * <p>Data Set column  type editor presenter.</p>
 * 
 * @since 0.4.0 
 */
@Dependent
public class ColumnTypeEditor implements IsWidget, org.dashbuilder.dataset.client.editor.ColumnTypeEditor  {

    DropDownImageListEditor<ColumnType> columnType;
    ColumnType originalColumnType;

    @Inject
    public ColumnTypeEditor(final DropDownImageListEditor<ColumnType> columnType) {
        this.columnType = columnType;
    }

    @PostConstruct
    public void init() {
        final Collection<ImageListEditor<ColumnType>.Entry> entries = getDefaultEntries();
        columnType.setImageSize("16px", "16px");
        columnType.setEntries(entries);
    }

    @Override
    public Widget asWidget() {
        return columnType.asWidget();
    }

    public void addHelpContent(final String title, final String content, final Placement placement) {
        columnType.addHelpContent(title, content, placement);
    }

    @Override
    public void setOriginalColumnType(final ColumnType columnType) {
        this.originalColumnType = columnType;
        final Collection<ImageListEditor<ColumnType>.Entry> acceptableEntries = getAcceptableEntries(columnType);
        this.columnType.setEntries(acceptableEntries);
    }

    /*************************************************************
            ** GWT EDITOR CONTRACT METHODS **
     *************************************************************/

    @Override
    public DropDownImageListEditor<ColumnType> columnType() {
        return columnType;
    }

    

    @Override
    public void flush() {
        
    }

    @Override
    public void onPropertyChange(final String... paths) {

    }

    @Override
    public void setValue(final DataColumnDef value) {
        final ColumnType ct = originalColumnType != null ? originalColumnType : ( value != null ? value.getColumnType() : null );
        final Collection<ImageListEditor<ColumnType>.Entry> acceptableEntries = getAcceptableEntries(ct);
        columnType.setEntries(acceptableEntries);
    }

    @Override
    public void setDelegate(final EditorDelegate<DataColumnDef> delegate) {

    }

    /**
     * Acceptable column types to be changed from:
     * LABEL -> TEXT
     * NUMBER -> LABEL
     */
    private Collection<ImageListEditor<ColumnType>.Entry> getAcceptableEntries(final ColumnType type) {
        final Collection<ImageListEditor<ColumnType>.Entry> result = new ArrayList<ImageListEditor<org.dashbuilder.dataset.ColumnType>.Entry>();
        if (type != null) {
            if (ColumnType.DATE.equals(type)) {
                result.add(buildEntry(ColumnType.DATE));
            } else if (ColumnType.LABEL.equals(type)) {
                result.add(buildEntry(ColumnType.TEXT));
                result.add(buildEntry(ColumnType.LABEL));
            } else if (ColumnType.TEXT.equals(type)) {
                result.add(buildEntry(ColumnType.TEXT));
            } else if (ColumnType.NUMBER.equals(type)) {
                result.add(buildEntry(ColumnType.LABEL));
                result.add(buildEntry(ColumnType.NUMBER));
            }
        }
        return result;
    }

    protected Collection<ImageListEditor<ColumnType>.Entry> getDefaultEntries() {
        final ColumnType[] providerTypes = ColumnType.values();
        final Collection<ImageListEditor<ColumnType>.Entry> entries = new ArrayList<ImageListEditor<ColumnType>.Entry>(providerTypes.length);
        for (final ColumnType type : providerTypes) {
            final ImageListEditor<ColumnType>.Entry entry = buildEntry(type);
            entries.add(entry);
        }
        return entries;
    }

    private ImageListEditor<ColumnType>.Entry buildEntry(final ColumnType type) {
        final String title = type.name();
        final String text = type.name();
        final SafeUri uri = getImageUri(type);
        return columnType.newEntry(type, uri,
                new SafeHtmlBuilder().appendEscaped(title). toSafeHtml(),
                new SafeHtmlBuilder().appendEscaped(text). toSafeHtml());
    }

    SafeUri getImageUri(final ColumnType type) {
        SafeUri result = null;
        switch (type) {
            case DATE:
                result = DataSetClientResources.INSTANCE.images().dateIcon32().getSafeUri();
                break;
            case NUMBER:
                result = DataSetClientResources.INSTANCE.images().numberIcon32V3().getSafeUri();
                break;
            case TEXT:
                result = DataSetClientResources.INSTANCE.images().textIcon32().getSafeUri();
                break;
            case LABEL:
                result = DataSetClientResources.INSTANCE.images().labelIcon32().getSafeUri();
                break;
        }
        return result;
    }

    @Override
    public void isEditMode(final boolean isEdit) {
        columnType.isEditMode(isEdit);
    }
    
}
