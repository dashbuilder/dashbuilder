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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.filter.ColumnFilter;
import org.dashbuilder.dataset.filter.CoreFunctionFilter;
import org.dashbuilder.dataset.filter.CoreFunctionType;
import org.dashbuilder.dataset.filter.FilterFactory;
import org.dashbuilder.dataset.date.TimeFrame;
import org.dashbuilder.displayer.client.events.ColumnFilterChangedEvent;
import org.dashbuilder.displayer.client.events.ColumnFilterDeletedEvent;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.uberfire.client.mvp.UberView;
import org.uberfire.mvp.Command;

@Dependent
public class ColumnFilterEditor implements IsWidget {

    public interface View extends UberView<ColumnFilterEditor> {

        void clearFunctionSelector();

        void addFunctionItem(CoreFunctionType function);

        void setFunctionSelected(String function);

        int getSelectedFunctionIndex();

        void showFilterConfig();

        void clearFilterConfig();

        void addFilterConfigWidget(IsWidget widget);

        String formatDate(Date date);

        String formatNumber(Number number);
    }

    View view = null;
    SyncBeanManager beanManager = null;
    ColumnFilter filter = null;
    DataSetMetadata metadata = null;
    Event<ColumnFilterChangedEvent> changedEvent = null;
    Event<ColumnFilterDeletedEvent> deletedEvent = null;

    @Inject
    public ColumnFilterEditor(View view,
                              SyncBeanManager beanManager,
                              Event<ColumnFilterChangedEvent> changedEvent,
                              Event<ColumnFilterDeletedEvent> deletedEvent) {
        this.view = view;
        this.beanManager = beanManager;
        this.changedEvent = changedEvent;
        this.deletedEvent = deletedEvent;
        this.view.init(this);
    }

    public void init(DataSetMetadata metadata, ColumnFilter filter) {
        this.filter = filter;
        this.metadata = metadata;
        initFilterSelector();
        initFilterConfig();
    }

    public ColumnFilter getFilter() {
        return filter;
    }

    public View getView() {
        return view;
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public void expand() {
        view.showFilterConfig();
    }

    // View notifications

    public void onSelectFilterFunction() {
        int selectedIdx = view.getSelectedFunctionIndex();
        if (selectedIdx >= 0) {
            CoreFunctionFilter coreFilter = getCoreFilter();
            CoreFunctionType functionType = getAvailableFunctions(coreFilter).get(selectedIdx);

            ColumnType columnType = metadata.getColumnType(coreFilter.getColumnId());
            List params = FilterFactory.createParameters(columnType, functionType);
            coreFilter.setType(functionType);
            coreFilter.setParameters(params);

            initFilterSelector();
            fireFilterChanged();

            if (!initFilterConfig().isEmpty()) {
                view.showFilterConfig();
            }
        }
    }

    public void onDeleteFilter() {
        deletedEvent.fire(new ColumnFilterDeletedEvent(filter));
    }

    // Internals

    protected CoreFunctionFilter getCoreFilter() {
        try {
            return (CoreFunctionFilter) filter;
        }
        catch (Exception e) {
            return null;
        }
    }

    protected List<IsWidget> createFilterInputControls() {
        List<IsWidget> filterInputControls = new ArrayList<IsWidget>();
        CoreFunctionFilter coreFilter = getCoreFilter();
        if (CoreFunctionType.LIKE_TO.equals(coreFilter.getType())) {
            IsWidget paramInput = createLikeToFunctionWidget(coreFilter);
            filterInputControls.add(paramInput);
        }
        else {
            for (int i = 0; i < coreFilter.getType().getParametersCount(); i++) {
                IsWidget paramInput = createParamInputWidget(coreFilter, i);
                filterInputControls.add(paramInput);
            }
        }
        return filterInputControls;
    }

    protected void initFilterSelector() {
        CoreFunctionFilter coreFilter = getCoreFilter();
        if (coreFilter != null) {
            view.clearFunctionSelector();
            String currentFunction = formatFilterFunction(coreFilter);
            view.setFunctionSelected(currentFunction);

            List<CoreFunctionType> calculateAvailableFunctions = getAvailableFunctions(coreFilter);
            for (CoreFunctionType functionType : calculateAvailableFunctions) {
                view.addFunctionItem(functionType);
            }
        }
    }

    protected List<CoreFunctionType> getAvailableFunctions(CoreFunctionFilter coreFilter) {
        ColumnType columnType = metadata.getColumnType(coreFilter.getColumnId());
        List<CoreFunctionType> functionTypes = CoreFunctionType.getSupportedTypes(columnType);
        Iterator<CoreFunctionType> it = functionTypes.iterator();
        while (it.hasNext()) {
            CoreFunctionType next = it.next();
            if (next.equals(coreFilter.getType())) {
                it.remove();
            }
        }
        return functionTypes;
    }

    protected List<IsWidget> initFilterConfig() {
        view.clearFilterConfig();
        List<IsWidget> inputs = createFilterInputControls();
        for (IsWidget input : inputs) {
            view.addFilterConfigWidget(input);
        }
        return inputs;
    }

    protected void updateSelectedFilter() {
        String currentFunction = formatFilterFunction(getCoreFilter());
        view.setFunctionSelected(currentFunction);
        fireFilterChanged();
    }

    protected void fireFilterChanged() {
        changedEvent.fire(new ColumnFilterChangedEvent(filter));
    }

    protected IsWidget createParamInputWidget(final CoreFunctionFilter coreFilter, final int paramIndex) {
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

    protected IsWidget createDateInputWidget(final List paramList, final int paramIndex) {
        Date param = (Date) paramList.get(paramIndex);

        final DateParameterEditor input = beanManager.lookupBean(DateParameterEditor.class).newInstance();
        input.setCurrentValue(param);
        input.setOnChangeCommand(new Command() {
            public void execute() {
                paramList.set(paramIndex, input.getCurrentValue());
                updateSelectedFilter();
            }
        });
        return input;
    }

    protected IsWidget createNumberInputWidget(final List paramList, final int paramIndex) {
        Double param = Double.parseDouble(paramList.get(paramIndex).toString());

        final NumberParameterEditor input = beanManager.lookupBean(NumberParameterEditor.class).newInstance();
        input.setCurrentValue(param);
        input.setOnChangeCommand(new Command() {
            public void execute() {
                paramList.set(paramIndex, input.getCurrentValue());
                updateSelectedFilter();
            }
        });
        return input;
    }

    protected IsWidget createTextInputWidget(final List paramList, final int paramIndex) {
        String param = (String) paramList.get(paramIndex);

        final TextParameterEditor input = beanManager.lookupBean(TextParameterEditor.class).newInstance();
        input.setCurrentValue(param);
        input.setOnChangeCommand(new Command() {
            @Override
            public void execute() {
                paramList.set(paramIndex, input.getCurrentValue());
                updateSelectedFilter();
            }
        });
        return input;
    }

    protected IsWidget createTimeFrameWidget(final List paramList, final int paramIndex) {
        TimeFrame timeFrame = TimeFrame.parse((String) paramList.get(paramIndex));

        final TimeFrameEditor input = beanManager.lookupBean(TimeFrameEditor.class).newInstance();
        input.init(timeFrame, new Command() {
            public void execute() {
                paramList.set(paramIndex, input.getTimeFrame().toString());
                updateSelectedFilter();
            }
        });
        return input;
    }

    protected IsWidget createLikeToFunctionWidget(final CoreFunctionFilter coreFilter) {
        final LikeToFunctionEditor input = beanManager.lookupBean(LikeToFunctionEditor.class).newInstance();
        final List paramList = coreFilter.getParameters();
        String pattern = (String) paramList.get(0);
        boolean caseSensitive = paramList.size() < 2 || Boolean.parseBoolean(paramList.get(1).toString());

        input.setCaseSensitive(caseSensitive);
        input.setPattern(pattern);
        input.setOnChangeCommand(new Command() {
            public void execute() {
                paramList.clear();
                paramList.add(input.getPattern());
                if (!input.isCaseSensitive()) {
                    // Only add if disabled since case sensitive is enabled by default.
                    paramList.add(input.isCaseSensitive());
                }
                updateSelectedFilter();
            }
        });
        return input;
    }

    public String formatFilterFunction(CoreFunctionFilter filter) {
        String columnId = filter.getColumnId();
        CoreFunctionType type = filter.getType();
        List parameters = filter.getParameters();

        StringBuilder out = new StringBuilder();

        if (CoreFunctionType.BETWEEN.equals(type)) {
            out.append(columnId).append(" [");
            formatParameters(out, parameters);
            out.append("]");
        }
        else if (CoreFunctionType.GREATER_THAN.equals(type)) {
            out.append(columnId).append(" > ");
            formatParameters(out, parameters);
        }
        else if (CoreFunctionType.GREATER_OR_EQUALS_TO.equals(type)) {
            out.append(columnId).append(" >= ");
            formatParameters(out, parameters);
        }
        else if (CoreFunctionType.LOWER_THAN.equals(type)) {
            out.append(columnId).append(" < ");
            formatParameters(out, parameters);
        }
        else if (CoreFunctionType.LOWER_OR_EQUALS_TO.equals(type)) {
            out.append(columnId).append(" <= ");
            formatParameters(out, parameters);
        }
        else if (CoreFunctionType.EQUALS_TO.equals(type)) {
            out.append(columnId).append(" = ");
            formatParameters(out, parameters);
        }
        else if (CoreFunctionType.NOT_EQUALS_TO.equals(type)) {
            out.append(columnId).append(" != ");
            formatParameters(out, parameters);
        }
        else if (CoreFunctionType.LIKE_TO.equals(type)) {
            out.append(columnId).append(" like ");
            formatParameters(out, parameters);
        }
        else if (CoreFunctionType.IS_NULL.equals(type)) {
            out.append(columnId).append(" = null ");
            formatParameters(out, parameters);
        }
        else if (CoreFunctionType.NOT_NULL.equals(type)) {
            out.append(columnId).append(" != null ");
            formatParameters(out, parameters);
        }
        else if (CoreFunctionType.TIME_FRAME.equals(type)) {
            out.append(columnId).append(" = ");
            formatParameters(out, parameters);
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
            return view.formatDate(d);
        }
        if (p instanceof Number) {
            Number n = (Number) p;
            return view.formatNumber(n);
        }
        return p.toString();
    }
}
