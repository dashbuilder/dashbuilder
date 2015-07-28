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

import java.util.Date;
import java.util.List;
import javax.enterprise.context.Dependent;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.ListBox;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.client.resources.i18n.CoreFunctionTypeConstants;
import org.dashbuilder.dataset.filter.ColumnFilter;
import org.dashbuilder.dataset.filter.CoreFunctionFilter;
import org.dashbuilder.dataset.filter.CoreFunctionType;
import org.dashbuilder.dataset.filter.FilterFactory;
import org.dashbuilder.dataset.date.TimeFrame;

@Dependent
public class ColumnFilterEditor extends Composite {

    public interface Listener {
        void columnFilterChanged(ColumnFilterEditor editor);
        void columnFilterDeleted(ColumnFilterEditor editor);
    }

    interface Binder extends UiBinder<Widget, ColumnFilterEditor> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    private static DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT);
    private static NumberFormat numberFormat = NumberFormat.getDecimalFormat();

    Listener listener = null;
    ColumnFilter filter = null;
    DataSetMetadata metadata = null;

    @UiField
    ListBox filterListBox;

    @UiField
    Icon filterDeleteIcon;

    @UiField
    Icon filterExpandIcon;

    @UiField
    Panel filterDetailsPanel;

    public ColumnFilterEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        filterExpandIcon.addDomHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                expandOrCollapse();
            }
        }, ClickEvent.getType());
        filterDeleteIcon.addDomHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                delete();
            }
        }, ClickEvent.getType());
    }

    public void init(DataSetMetadata metadata, ColumnFilter filter, Listener listener) {
        this.filter = filter;
        this.listener = listener;
        this.metadata = metadata;

        filterExpandIcon.setType(IconType.ARROW_DOWN);
        filterExpandIcon.setVisible(hasDetails());
        initFilterListBox();
    }

    public ColumnFilter getFilter() {
        return filter;
    }

    public CoreFunctionFilter getCoreFilter() {
        try {
            return (CoreFunctionFilter) filter;
        }
        catch (Exception e) {
            return null;
        }
    }

    public boolean hasDetails() {
        CoreFunctionFilter coreFilter = getCoreFilter();
        return (coreFilter != null && coreFilter.getType().getParametersCount() > 0);
    }

    public void expand() {
        if (hasDetails()) {
            filterExpandIcon.setVisible(true);
            filterExpandIcon.setType(IconType.ARROW_UP);
            filterDetailsPanel.setVisible(true);
            initFilterDetailsPanel();
        }
    }

    public void collapse() {
        filterDetailsPanel.setVisible(false);
        filterExpandIcon.setType(IconType.ARROW_DOWN);
        filterExpandIcon.setVisible(hasDetails());
    }

    public void expandOrCollapse() {
        if (filterDetailsPanel.isVisible()) {
            collapse();
        } else {
            expand();
        }
    }

    public void delete() {
        listener.columnFilterDeleted(this);
    }

    // UI events

    @UiHandler(value = "filterListBox")
    public void onFilterSelected(ChangeEvent changeEvent) {
        int selectedIdx = filterListBox.getSelectedIndex();
        if (selectedIdx > 0) {
            CoreFunctionFilter coreFilter = getCoreFilter();
            ColumnType columnType = metadata.getColumnType(coreFilter.getColumnId());
            List<CoreFunctionType> functionTypes = CoreFunctionType.getSupportedTypes(columnType);
            CoreFunctionType functionType = functionTypes.get(selectedIdx-1);

            List params = FilterFactory.createParameters(columnType, functionType);
            coreFilter.setType(functionType);
            coreFilter.setParameters(params);
            filterUpdated();

            if (hasDetails()) expand();
            else collapse();
        }
    }

    // Internals

    protected void initFilterListBox() {
        CoreFunctionFilter coreFilter = getCoreFilter();
        if (coreFilter != null) {
            filterListBox.clear();
            String currentFilter = formatFilter(coreFilter);
            filterListBox.addItem(currentFilter);
            filterListBox.setTitle(currentFilter);

            // Add the remain available functions
            ColumnType columnType = metadata.getColumnType(coreFilter.getColumnId());
            List<CoreFunctionType> functionTypes = CoreFunctionType.getSupportedTypes(columnType);
            for (int i = 0; i < functionTypes.size(); i++) {
                CoreFunctionType ft = functionTypes.get(i);
                String function = CoreFunctionTypeConstants.INSTANCE.getString(ft.name());
                filterListBox.addItem(function);
            }
        }
    }

    protected void initFilterDetailsPanel() {
        filterDetailsPanel.clear();

        CoreFunctionFilter coreFilter = getCoreFilter();
        for (int i=0; i<coreFilter.getType().getParametersCount(); i++) {
            Widget paramInput = createParamInputWidget(coreFilter, i);
            filterDetailsPanel.add(paramInput);
        }
    }

    protected void filterUpdated() {
        listener.columnFilterChanged(this);
        initFilterListBox();
    }

    protected Widget createParamInputWidget(final CoreFunctionFilter coreFilter, final int paramIndex) {
        final List paramList = coreFilter.getParameters();
        ColumnType columnType = metadata.getColumnType(coreFilter.getColumnId());

        if (ColumnType.DATE.equals(columnType)) {
            if (CoreFunctionType.TIME_FRAME.equals(coreFilter.getType())) {
                return createTimeFrameWidget(paramList, paramIndex);
            }
            return createDateInputWidget(paramList, paramIndex);
        }
        if (ColumnType.NUMBER.equals(columnType)) {
            return createNumberInputWidget(paramList, paramIndex);
        }
        return createTextInputWidget(paramList, paramIndex);
    }

    protected Widget createDateInputWidget(final List paramList, final int paramIndex) {
        Date param = (Date) paramList.get(paramIndex);

        DateParameterEditor input = new DateParameterEditor();
        input.init(param, new DateParameterEditor.Listener() {
            public void valueChanged(Date d) {
                paramList.set(paramIndex, d);
                filterUpdated();
            }
        });
        return input;
    }

    protected Widget createNumberInputWidget(final List paramList, final int paramIndex) {
        Number param = Double.parseDouble(paramList.get(paramIndex).toString());

        NumberParameterEditor input = new NumberParameterEditor();
        input.init(param, new NumberParameterEditor.Listener() {
            public void valueChanged(Number n) {
                paramList.set(paramIndex, n);
                filterUpdated();
            }
        });
        return input;
    }

    protected Widget createTextInputWidget(final List paramList, final int paramIndex) {
        String param = (String) paramList.get(paramIndex);

        TextParameterEditor input = new TextParameterEditor();
        input.init(param, new TextParameterEditor.Listener() {
            public void valueChanged(String s) {
                paramList.set(paramIndex, s);
                filterUpdated();
            }
        });
        return input;
    }

    protected Widget createTimeFrameWidget(final List paramList, final int paramIndex) {
        TimeFrame timeFrame = TimeFrame.parse((String) paramList.get(paramIndex));

        TimeFrameEditor input = new TimeFrameEditor();
        input.init(timeFrame, new TimeFrameEditor.Listener() {
            public void valueChanged(TimeFrame tf) {
                paramList.set(paramIndex, tf.toString());
                filterUpdated();
            }
        });
        return input;
    }

    protected String formatFilter(CoreFunctionFilter f) {
        String columnId = f.getColumnId();
        CoreFunctionType type = f.getType();
        StringBuilder out = new StringBuilder();

        if (CoreFunctionType.BETWEEN.equals(type)) {
            out.append(columnId).append(" [");
            formatParameters(out, f.getParameters());
            out.append("]");
        }
        else if (CoreFunctionType.GREATER_THAN.equals(type)) {
            out.append(columnId).append(" > ");
            formatParameters(out, f.getParameters());
        }
        else if (CoreFunctionType.GREATER_OR_EQUALS_TO.equals(type)) {
            out.append(columnId).append(" >= ");
            formatParameters(out, f.getParameters());
        }
        else if (CoreFunctionType.LOWER_THAN.equals(type)) {
            out.append(columnId).append(" < ");
            formatParameters(out, f.getParameters());
        }
        else if (CoreFunctionType.LOWER_OR_EQUALS_TO.equals(type)) {
            out.append(columnId).append(" <= ");
            formatParameters(out, f.getParameters());
        }
        else if (CoreFunctionType.EQUALS_TO.equals(type)) {
            out.append(columnId).append(" = ");
            formatParameters(out, f.getParameters());
        }
        else if (CoreFunctionType.NOT_EQUALS_TO.equals(type)) {
            out.append(columnId).append(" != ");
            formatParameters(out, f.getParameters());
        }
        else if (CoreFunctionType.LIKE_TO.equals(type)) {
            out.append(columnId).append(" like ");
            formatParameters(out, f.getParameters());
        }
        else if (CoreFunctionType.IS_NULL.equals(type)) {
            out.append(columnId).append(" = null ");
            formatParameters(out, f.getParameters());
        }
        else if (CoreFunctionType.NOT_NULL.equals(type)) {
            out.append(columnId).append(" != null ");
            formatParameters(out, f.getParameters());
        }
        else if (CoreFunctionType.TIME_FRAME.equals(type)) {
            out.append(columnId).append(" = ");
            formatParameters(out, f.getParameters());
        }
        return out.toString();
    }

    protected StringBuilder formatParameters(StringBuilder out, List parameters) {
        for (int i=0; i< parameters.size();  i++) {
            if (i > 0) out.append("  ");
            out.append(formatParameter(parameters.get(i)));
        }
        return out;
    }

    protected String formatParameter(Object p) {
        if (p == null) {
            return "";
        }
        if (p instanceof Date) {
            Date d = (Date) p;
            return dateTimeFormat.format(d);
        }
        if (p instanceof Number) {
            Number n = (Number) p;
            return numberFormat.format(n);
        }
        return p.toString();
    }
}
