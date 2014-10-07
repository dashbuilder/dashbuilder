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
package org.dashbuilder.renderer.google.client;

import java.util.List;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.Label;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.Pagination;
import com.github.gwtbootstrap.client.ui.Tooltip;
import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.Bootstrap;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gwt.charts.client.ChartPackage;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.event.SortEvent;
import com.googlecode.gwt.charts.client.event.SortHandler;
import com.googlecode.gwt.charts.client.options.TableSort;
import com.googlecode.gwt.charts.client.table.Table;
import com.googlecode.gwt.charts.client.table.TableOptions;
import org.dashbuilder.common.client.SpacerWidget;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerAttributeGroupDef;
import org.dashbuilder.displayer.DisplayerEditorConfig;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.impl.DisplayerEditorConfigImpl;
import org.dashbuilder.renderer.google.client.resources.i18n.GoogleDisplayerConstants;

public class GoogleTableDisplayer extends GoogleDisplayer {

    protected int pageSize = 20;
    protected int currentPage = 1;
    protected int numberOfRows = 0;
    protected int numberOfPages = 1;
    protected int pageSelectorSize = 10;

    protected boolean showTotalRowsHint = true;
    protected boolean showTotalPagesHint = true;

    private Table table;
    private HorizontalPanel pagerPanel = new HorizontalPanel();
    private SortInfo sortInfo = new SortInfo();

    public void setShowTotalPagesHint(boolean showTotalPagesHint) {
        this.showTotalPagesHint = showTotalPagesHint;
    }

    public void setShowTotalRowsHint(boolean showTotalRowsHint) {
        this.showTotalRowsHint = showTotalRowsHint;
    }

    @Override
    public DisplayerEditorConfig getDisplayerEditorConfig() {
        return new DisplayerEditorConfigImpl()
                   .supportsAttribute( DisplayerAttributeDef.TYPE )
                   .supportsAttribute( DisplayerAttributeDef.RENDERER )
                   .supportsAttribute( DisplayerAttributeDef.COLUMNS )
                   .supportsAttribute( DisplayerAttributeGroupDef.FILTER_GROUP )
                   .supportsAttribute( DisplayerAttributeGroupDef.TITLE_GROUP )
                   .supportsAttribute( DisplayerAttributeGroupDef.TABLE_GROUP );
    }

    @Override
    public ChartPackage getPackage() {
        return ChartPackage.TABLE;
    }

    @Override
    protected void beforeDataSetLookup() {
        // Draw only the data subset corresponding to the current page.
        int pageSize = displayerSettings.getTablePageSize();
        int offset = (currentPage - 1) * pageSize;
        dataSetHandler.limitDataSetRows(offset, pageSize);
    }

    @Override
    protected void afterDataSetLookup(DataSet dataSet) {
        pageSize = displayerSettings.getTablePageSize();
        numberOfRows = dataSet.getRowCountNonTrimmed();
        numberOfPages = ((numberOfRows - 1) / pageSize) + 1;
        if (currentPage > numberOfPages) {
            currentPage = 1;
        }
    }

    @Override
    public void onGroupIntervalsSelected(Displayer displayer, DataSetGroup groupOp) {
        // Reset the current navigation status on filter requests from external displayers.
        currentPage = 1;
        super.onGroupIntervalsSelected(displayer, groupOp);
    }

    @Override
    public void onGroupIntervalsReset(Displayer displayer, List<DataSetGroup> groupOps) {
        // Reset the current navigation status on filter requests from external displayers.
        currentPage = 1;
        super.onGroupIntervalsReset(displayer, groupOps);
    }

    @Override
    public Widget createVisualization() {
        final DataTable dataTable = createTable();
        table = new Table();
        table.addSortHandler( new SortHandler() {
            @Override public void onSort( SortEvent sortEvent ) {
                String columnId = dataTable.getColumnId(sortEvent.getColumn());
                sortApply(columnId, sortInfo.getSortOrder(columnId));
                redraw();
            }
        } );

        table.draw(dataTable, createOptions());
        HTML titleHtml = new HTML();
        if (displayerSettings.isTitleVisible()) {
            titleHtml.setText(displayerSettings.getTitle());
        }

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(titleHtml);
        verticalPanel.add(table);
        verticalPanel.add(pagerPanel);
        createTablePager();
        return verticalPanel;
    }

    @Override
    protected void updateVisualization() {
        this.createTablePager();
        table.draw(createTable(), createOptions());
    }

    private TableOptions createOptions() {
        TableOptions options = TableOptions.create();
        options.setSort(TableSort.EVENT);
        // TODO options.setSortColumn(  );
        // TODO complete options.setSortAscending( SortOrder.ASCENDING.equals( displayerSettings.getTableDefaultSortOrder() ) );
        options.setPageSize(displayerSettings.getTablePageSize());
        options.setShowRowNumber(false);
        if ( displayerSettings.getTableWidth() != 0 ) options.setWidth( displayerSettings.getTableWidth() );
        return options;
    }


    private void gotoPage(int pageNumber) {
        if (pageNumber != currentPage && pageNumber > 0 && pageNumber < numberOfPages + 1) {
            currentPage = pageNumber;
            super.redraw();
        }
    }

    protected int getLeftMostPageNumber() {
        int page = currentPage - pageSelectorSize/2;
        if (page < 1) return 1;
        return page;
    }

    protected int getRightMostPageNumber() {
        int page = getLeftMostPageNumber() + pageSelectorSize - 1;
        if (page > numberOfPages) return numberOfPages;
        return page;
    }

    protected void createTablePager() {
        pagerPanel.clear();
        pagerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        Pagination pagination = new Pagination();
        pagination.setSize(Pagination.PaginationSize.NORMAL);
        pagination.setAlignment(Bootstrap.Pagination.LEFT.toString());

        for (int i = getLeftMostPageNumber(); i <= getRightMostPageNumber(); i++) {
            NavLink pageLink = new NavLink(Integer.toString(i));
            final Integer _currentPage = Integer.valueOf(i);
            if (currentPage != i) {
                pageLink.setActive(false);
                pageLink.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        gotoPage(_currentPage.intValue());
                    }
                });
            } else {
                pageLink.setActive(true);
            }
            pagination.add(pageLink);
        }

        Icon leftPageIcon = new Icon(IconType.ANGLE_LEFT);
        leftPageIcon.setIconSize(IconSize.LARGE);
        leftPageIcon.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        leftPageIcon.sinkEvents(Event.ONCLICK);
        leftPageIcon.addHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                gotoPage(currentPage - 1);
            }
        }, ClickEvent.getType());
        Tooltip leftPageTooltip = new Tooltip( GoogleDisplayerConstants.INSTANCE.googleTableDisplayer_gotoPreviousPage() );
        leftPageTooltip.add(leftPageIcon);

        Icon rightPageIcon = new Icon(IconType.ANGLE_RIGHT);
        rightPageIcon.setIconSize(IconSize.LARGE);
        rightPageIcon.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        rightPageIcon.sinkEvents(Event.ONCLICK);
        rightPageIcon.addHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                gotoPage(currentPage + 1);
            }
        }, ClickEvent.getType());
        Tooltip rightPageTooltip = new Tooltip( GoogleDisplayerConstants.INSTANCE.googleTableDisplayer_gotoNextPage() );
        rightPageTooltip.add(rightPageIcon);

        Icon firstPageIcon = new Icon(IconType.DOUBLE_ANGLE_LEFT);
        firstPageIcon.setIconSize(IconSize.LARGE);
        firstPageIcon.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        firstPageIcon.sinkEvents(Event.ONCLICK);
        firstPageIcon.addHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                gotoPage(1);
            }
        }, ClickEvent.getType());
        Tooltip firstPageTooltip = new Tooltip( GoogleDisplayerConstants.INSTANCE.googleTableDisplayer_gotoFirstPage() );
        firstPageTooltip.add(firstPageIcon);

        Icon lastPageIcon = new Icon(IconType.DOUBLE_ANGLE_RIGHT);
        lastPageIcon.setIconSize(IconSize.LARGE);
        lastPageIcon.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        lastPageIcon.sinkEvents(Event.ONCLICK);
        lastPageIcon.addHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                gotoPage(numberOfPages);
            }
        }, ClickEvent.getType());
        Tooltip lastPageTooltip = new Tooltip( GoogleDisplayerConstants.INSTANCE.googleTableDisplayer_gotoLastPage() );
        lastPageTooltip.add(lastPageIcon);

        Label totalPages = null;
        if ( showTotalPagesHint ) {
            totalPages = new Label(
                                    GoogleDisplayerConstants.INSTANCE.googleTableDisplayer_pages(
                                        Integer.toString( getLeftMostPageNumber() ),
                                        Integer.toString( getRightMostPageNumber() ),
                                        Integer.toString( numberOfPages ) )
                                  );
        }
        Label totalRows = null;
        if ( numberOfRows == 0) {
            totalRows = new Label( GoogleDisplayerConstants.INSTANCE.googleTableDisplayer_noData() );
        } else if ( showTotalRowsHint ) {
            int currentRowsShown = currentPage * pageSize > numberOfRows ? numberOfRows : currentPage * pageSize;
            totalRows = new Label(
                                    GoogleDisplayerConstants.INSTANCE.googleTableDisplayer_rows(
                                        Integer.toString( ( ( currentPage - 1 ) * pageSize) + 1 ),
                                        Integer.toString( currentRowsShown ),
                                        Integer.toString( numberOfRows ) )
                                 );
        }

        if ( numberOfPages > 1) {
            pagerPanel.add( firstPageTooltip );
            pagerPanel.add( SpacerWidget.DOUBLE );
            pagerPanel.add( leftPageTooltip );
            pagerPanel.add( SpacerWidget.DOUBLE );
            pagerPanel.add( pagination );
            pagerPanel.add( SpacerWidget.DOUBLE );
            pagerPanel.add( rightPageTooltip );
            pagerPanel.add( SpacerWidget.DOUBLE );
            pagerPanel.add( lastPageTooltip );
        }

        boolean both = showTotalPagesHint && showTotalRowsHint;
        if ( showTotalPagesHint || showTotalRowsHint ) {
            pagerPanel.add( SpacerWidget.DOUBLE );
            pagerPanel.add( SpacerWidget.DOUBLE );
            if ( totalPages != null && numberOfPages > 1 ) pagerPanel.add( totalPages );
            if ( both ) pagerPanel.add( SpacerWidget.DOUBLE );
            if ( totalRows != null ) pagerPanel.add( totalRows );
        }
    }

    private static class SortInfo {
        private String columnId;
        private SortOrder sortOrder;
        private SortInfo() {}
        private SortOrder getSortOrder( String columnId ) {
            if ( this.columnId == null || !this.columnId.equalsIgnoreCase( columnId ) ) {
                this.columnId = columnId;
                this.sortOrder = SortOrder.ASCENDING;
            } else {    //columnId != null && columnId == columnId) --> 'invert' order
                switch (this.sortOrder) {
                    case UNSPECIFIED: this.sortOrder = SortOrder.ASCENDING; break;
                    case ASCENDING: this.sortOrder = SortOrder.DESCENDING; break;
                    case DESCENDING: this.sortOrder = SortOrder.ASCENDING; break;
                    default: this.sortOrder = SortOrder.ASCENDING;
                }
            }
            return sortOrder;
        }
    }
}
