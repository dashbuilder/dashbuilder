package org.dashbuilder.client.uftable;

import java.util.Date;
import java.util.List;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import org.dashbuilder.client.dataset.DataSetReadyCallback;
import org.dashbuilder.client.displayer.AbstractDataViewer;
import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.dataset.DataSet;

import org.dashbuilder.model.dataset.sort.SortOrder;
import org.uberfire.client.tables.PagedTable;

public class UFTableViewer extends AbstractDataViewer<org.dashbuilder.model.displayer.TableDisplayer> {

    protected int pageSize = 20;
    protected int numberOfRows = 0;
    protected int dataSetLowerLimit = 0;
    protected int dataSetUpperLimit = 0;

    protected boolean drawn = false;

    protected FlowPanel panel = new FlowPanel();
    protected Label label = new Label();

    protected DataSet dataSet;

    protected UFTableDataProvider dataProvider = new UFTableDataProvider(this);

    public UFTableViewer() {
        initWidget(panel);
    }

    public void draw() {
        if (!drawn) {
            drawn = true;

            if (dataDisplayer == null) {
                displayMessage("ERROR: DataDisplayer property not set");
            } else if (dataSetHandler == null) {
                displayMessage("ERROR: DataSetHandler property not set");
            } else {
                try {
                    displayMessage("Initializing '" + dataDisplayer.getTitle() + "'...");
                    lookupDataSet(
                            0,
                            pageSize,
                            new DataSetReadyCallback() {
                                public void callback(DataSet result) {
                                    Widget w = createWidget();
                                    panel.clear();
                                    panel.add(w);
                                }

                                public void notFound() {
                                    displayMessage("ERROR: Data set not found.");
                                }
                            }
                    );
                } catch (Exception e) {
                    displayMessage("ERROR: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Just reload the data set and make the current google Viewer to redraw.
     */
    public void redraw() {
        return;
    }

    /**
     * Clear the current display and show a notification message.
     */
    public void displayMessage(String msg) {
        panel.clear();
        panel.add(label);
        label.setText(msg);
    }

    protected PagedTable createUFTable() {

        final ExtPT ufPagedTable = new ExtPT(pageSize);
        ColumnSortEvent.AsyncHandler sortHandler = new ColumnSortEvent.AsyncHandler( ufPagedTable ) {
            @Override public void onColumnSort( ColumnSortEvent event ) {
                setSortOrder( event.getColumn().getDataStoreName(), event.isSortAscending() ? SortOrder.ASCENDING : SortOrder.DESCENDING );
                lookupDataSet( new DataSetReadyCallback() {
                    @Override public void callback( DataSet dataSet ) {
                        ufPagedTable.redraw();
                    }

                    @Override public void notFound() {
                        displayMessage("ERROR in data lookup.");
                    }
                });
            }
        };
        ufPagedTable.setColumnSortHandler( sortHandler );


//        PagedTable ufPagedTable = new PagedTable(pageSize, sortHandler);ufPagedTable.get
        ufPagedTable.setRowCount(numberOfRows, true);
        ufPagedTable.setHeight(Window.getClientHeight() - this.getAbsoluteTop() + "px");
        ufPagedTable.setWidth(Window.getClientWidth() - (this.getAbsoluteLeft() + this.getOffsetWidth()) + "px");
        ufPagedTable.setEmptyTableCaption("No data available");

        List<DataColumn> columns = dataSet.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            final _Integer colNum = new _Integer(i);
            DataColumn dataColumn = columns.get(i);
            ColumnType columnType = dataColumn.getColumnType();

            switch (columnType) {
                case LABEL:
                    Column labelColumn =
                            new Column<UFTableRow, String>(new TextCell()) {
                                @Override
                                public String getValue(UFTableRow row) {
                                    Object value = dataSet.getValueAt(row.getRowNumber(), colNum.getColNum());
                                    return value.toString();
                                }
                            };

                    labelColumn.setSortable( true );
                    ufPagedTable.addColumn( labelColumn, dataColumn.getId());
                    break;

                case NUMBER:
                    Column numberColumn =
                            new Column<UFTableRow, Number>(new NumberCell(NumberFormat.getFormat("#.###"))) {
                                @Override
                                public Number getValue(UFTableRow row) {
                                    return (Number) dataSet.getValueAt(row.getRowNumber(), colNum.getColNum());
                                }
                            };
                    numberColumn.setSortable( true );
                    ufPagedTable.addColumn( numberColumn, dataColumn.getId());
                    break;

                case DATE:
                    Column dateColumn =
                            new Column<UFTableRow, Date>(new DateCell(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM))) {
                                @Override
                                public Date getValue(UFTableRow row) {
                                    return (Date) dataSet.getValueAt(row.getRowNumber(), colNum.getColNum());
                                }
                            };
                    dateColumn.setSortable( true );
                    ufPagedTable.addColumn( dateColumn, dataColumn.getId());
                    break;

                default:
            }
        }
        return ufPagedTable;
    }

    protected Widget createWidget() {
        pageSize = dataDisplayer.getPageSize();
        numberOfRows = dataSetHandler.getDataSetMetadata().getNumberOfRows();

        PagedTable<UFTableRow> table = createUFTable();
        dataProvider.addDataDisplay(table);

        HTML titleHtml = new HTML();
        if (dataDisplayer.isTitleVisible()) {
            titleHtml.setText(dataDisplayer.getTitle());
        }

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(titleHtml);
        verticalPanel.add(table);
        return verticalPanel;
    }

    protected void lookupDataSet(int lowerLimit, int upperLimit, final DataSetReadyCallback callback) {
        // Avoid double lookup at the time of widget creation
        if (dataSetLowerLimit != lowerLimit || dataSetUpperLimit != upperLimit) {
            this.dataSetLowerLimit = lowerLimit;
            this.dataSetUpperLimit = upperLimit;
            dataSetHandler.limitDataSetRows(lowerLimit, upperLimit);
            lookupDataSet( callback );
        } else {
            // If it was already looked up just return the dataSet
            callback.callback(dataSet);
        }
    }

    private void lookupDataSet( final DataSetReadyCallback callback ) {
        try {
            dataSetHandler.lookupDataSet(
                    // 'local' callback to set the new dataSet to the viewer
                    new DataSetReadyCallback() {
                        @Override
                        public void callback(DataSet dataSet) {
                            UFTableViewer.this.dataSet = dataSet;
                            callback.callback(dataSet);
                        }

                        @Override
                        public void notFound() {
                            callback.notFound();
                        }
                    }
            );
        } catch (Exception e) {
            displayMessage("ERROR: " + e.getMessage());
        }
    }

    // TODO workaround, to be removed
    private final class ExtPT extends PagedTable {
        private ExtPT( int pageSize ) {
            super( pageSize );
        }
        private void setColumnSortHandler( ColumnSortEvent.AsyncHandler handler ) {
            dataGrid.addColumnSortHandler( handler );
        }
    }

    private final class _Integer {
        private int colNum;
        private _Integer(int colNum) {
            this.colNum = colNum;
        }
        private int getColNum() {
            return colNum;
        }
    }
}
