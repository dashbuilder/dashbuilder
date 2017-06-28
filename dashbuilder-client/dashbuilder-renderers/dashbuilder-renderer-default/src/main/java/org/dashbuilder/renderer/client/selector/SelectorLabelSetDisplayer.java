/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.renderer.client.selector;

import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.dataset.group.ColumnGroup;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.GroupStrategy;
import org.dashbuilder.dataset.sort.DataSetSort;
import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.displayer.ColumnSettings;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerAttributeGroupDef;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.displayer.client.AbstractErraiDisplayer;
import org.jboss.errai.ioc.client.container.SyncBeanManager;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Dependent
public class SelectorLabelSetDisplayer extends AbstractErraiDisplayer<SelectorLabelSetDisplayer.View> {

    public interface View extends AbstractErraiDisplayer.View<SelectorLabelSetDisplayer> {

        void showTitle(String title);

        void setWidth(int width);

        void margins(int top, int bottom, int left, int right);

        void clearItems();

        void addItem(SelectorLabelItem item);

        String getGroupsTitle();

        String getColumnsTitle();

        void noData();
    }

    protected View view;
    protected boolean filterOn = false;
    protected boolean multipleSelections = false;
    protected SyncBeanManager beanManager;
    protected Set<SelectorLabelItem> itemCollection = new HashSet<>();

    @Inject
    public SelectorLabelSetDisplayer(View view, SyncBeanManager beanManager) {
        this.beanManager = beanManager;
        this.view = view;
        this.view.init(this);
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void close() {
        super.close();
        clearItems();
    }

    protected void clearItems() {
        view.clearItems();
        for (SelectorLabelItem item : itemCollection) {
            beanManager.destroyBean(item);
        }
        itemCollection.clear();
    }

    protected void resetItems() {
        for (SelectorLabelItem item : itemCollection) {
            item.reset();
        }
    }

    @Override
    public DisplayerConstraints createDisplayerConstraints() {

        DataSetLookupConstraints lookupConstraints = new DataSetLookupConstraints()
                .setGroupRequired(true)
                .setGroupColumn(true)
                .setMaxColumns(-1)
                .setMinColumns(1)
                .setExtraColumnsAllowed(true)
                .setGroupsTitle(view.getGroupsTitle())
                .setColumnsTitle(view.getColumnsTitle());

        return new DisplayerConstraints(lookupConstraints)
                .supportsAttribute(DisplayerAttributeDef.TYPE)
                .supportsAttribute(DisplayerAttributeDef.SUBTYPE)
                .supportsAttribute(DisplayerAttributeDef.RENDERER)
                .supportsAttribute(DisplayerAttributeDef.TITLE)
                .supportsAttribute(DisplayerAttributeDef.TITLE_VISIBLE)
                .supportsAttribute(DisplayerAttributeGroupDef.SELECTOR_GROUP)
                .excludeAttribute(DisplayerAttributeDef.SELECTOR_SHOW_INPUTS)
                .supportsAttribute(DisplayerAttributeGroupDef.CHART_MARGIN_GROUP)
                .supportsAttribute(DisplayerAttributeGroupDef.COLUMNS_GROUP)
                .supportsAttribute(DisplayerAttributeGroupDef.FILTER_GROUP)
                .supportsAttribute(DisplayerAttributeGroupDef.REFRESH_GROUP);
    }

    @Override
    protected void beforeDataSetLookup() {
        // Make sure the drop down entries are sorted
        DataSetGroup group = dataSetHandler.getCurrentDataSetLookup().getLastGroupOp();
        if (dataSetHandler.getCurrentDataSetLookup().getOperationList(DataSetSort.class).isEmpty() && group != null) {
            ColumnGroup column = group.getColumnGroup();
            if (!GroupStrategy.FIXED.equals(column.getStrategy())) {
                dataSetHandler.sort(column.getSourceId(), SortOrder.ASCENDING);
            }
        }
    }

    @Override
    protected void createVisualization() {
        if (displayerSettings.isTitleVisible()) {
            view.showTitle(displayerSettings.getTitle());
        }
        view.margins(displayerSettings.getChartMarginTop(),
                displayerSettings.getChartMarginBottom(),
                displayerSettings.getChartMarginLeft(),
                displayerSettings.getChartMarginRight());

        multipleSelections = displayerSettings.isSelectorMultiple();
        updateVisualization();
    }

    @Override
    protected void updateVisualization() {
        clearItems();
        if (dataSet.getRowCount() == 0) {
            view.noData();
        } else {
            // Generate the list entries from the current data set
            int sumOfItemLabelLengths = 0; // Accumulate item label lengths to set width percentage proportional to text length
            for (int i = 0; i < dataSet.getRowCount(); i++) {
                Object obj = dataSet.getValueAt(i, 0);
                if (obj == null) {
                    continue;
                }

                String value = super.formatValue(i, 0);
                String title = createTitle(i);
                final SelectorLabelItem item = createItem(i, value, title);

                view.addItem(item);
                itemCollection.add(item);
                sumOfItemLabelLengths += value.length();
            }

            // If size of displayer is restricted set both displayer view width and width of each item
            // Each item's width percentage will be proportional to the length of it's label (DASHBUILDE-TODO)
            // So that sum of all item's widths is 85% (the rest is spacing between buttons)
            if (displayerSettings.getSelectorWidth() > 0) {
                view.setWidth(displayerSettings.getSelectorWidth());
                for (SelectorLabelItem labelItem : itemCollection) {
                    int itemWidth = 85 * labelItem.getLabelLength() / sumOfItemLabelLengths;
                    labelItem.setWidth(itemWidth);
                }
            }
        }
    }

    /* Create item title of the form "col1=val1 col2=val2" that will appear on mouse hover */
    private String createTitle(int rowIdx) {
        StringBuilder title = new StringBuilder();

        int ncolumns = dataSet.getColumns().size();
        if (ncolumns > 1) {
            for (int colIdx = 1; colIdx < ncolumns; colIdx++) {
                DataColumn extraColumn = dataSet.getColumnByIndex(colIdx);
                ColumnSettings columnSettings = displayerSettings.getColumnSettings(extraColumn);
                String extraColumnName = columnSettings.getColumnName();
                Object extraValue = dataSet.getValueAt(rowIdx, colIdx);
                if (extraValue != null) {
                    title.append(colIdx > 1 ? " " : "");
                    String formattedValue = super.formatValue(rowIdx, colIdx);
                    title.append(extraColumnName).append("=").append(formattedValue);
                }
            }
        }
        return title.toString();
    }

    private SelectorLabelItem createItem(int id, String value, String title) {
        final SelectorLabelItem item = beanManager.lookupBean(SelectorLabelItem.class).newInstance();
        item.init(id, value, title);
        item.setOnSelectCommand(() -> onItemSelected(item));
        item.setOnResetCommand(() -> onItemReset(item));
        return item;
    }

    public String getFirstColumnId() {
        DataColumn firstColumn = dataSet.getColumnByIndex(0);
        return firstColumn.getId();
    }

    void onItemSelected(SelectorLabelItem item) {
        if (displayerSettings.isFilterEnabled()) {

            String firstColumnId = getFirstColumnId();

            // Reset current selection (if any) in single selection mode
            if (!multipleSelections) {
                List<Integer> currentFilter = filterIndexes(firstColumnId);
                if (currentFilter != null && !currentFilter.isEmpty()) {
                    resetItems();
                    super.filterReset();
                    item.select();
                }
            }
            // Filter by the selected entry
            filterUpdate(firstColumnId, item.getId());
        }
    }

    void onItemReset(SelectorLabelItem item) {
        if (displayerSettings.isFilterEnabled()) {

            String firstColumnId = getFirstColumnId();
            filterUpdate(firstColumnId, item.getId());
        }
    }
}
