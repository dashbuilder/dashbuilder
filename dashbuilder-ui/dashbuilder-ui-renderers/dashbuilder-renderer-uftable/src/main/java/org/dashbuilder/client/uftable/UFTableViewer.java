package org.dashbuilder.client.uftable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
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

import static com.google.gwt.dom.client.BrowserEvents.CLICK;

public class UFTableViewer extends AbstractDataViewer<org.dashbuilder.model.displayer.TableDisplayer> {

    private static  final String[] COLOR_ARRAY = new String[] {
            "red", "blue", "yellow", "green", "orange", "brown", "magenta", "lime", "gold",
            "indigo", "pink", "purple", "aqua", "salmon", "coral", "fuchsia", "silver"
    };

    private int colorIndex = 0;
    private Map<String, String> columnColorMap = new HashMap<String, String>( 5 );

    private Map< String, List<KeyValue<String, Integer>> > columnCellSelections =
            new HashMap< String, List<KeyValue<String, Integer>> >(5);

    protected int pageSize = 20;
    protected int numberOfRows = 0;
    protected int dataSetLowerLimit = 0;
    protected int dataSetUpperLimit = 0;

    protected boolean drawn = false;

    protected FlowPanel panel = new FlowPanel();
    protected Label label = new Label();
    protected PagedTable<UFTableRow> table;

    protected DataSet dataSet;

    protected UFTableDataProvider dataProvider = new UFTableDataProvider( this );

    public UFTableViewer() {
        initWidget( panel );
    }

    public void draw() {
        if ( !drawn ) {
            drawn = true;

            if ( dataDisplayer == null ) {
                displayMessage( "ERROR: DataDisplayer property not set" );
            } else if ( dataSetHandler == null ) {
                displayMessage( "ERROR: DataSetHandler property not set" );
            } else {
                try {
                    displayMessage( "Initializing '" + dataDisplayer.getTitle() + "'..." );

                    lookupDataSet(
                            0,
                            pageSize,
                            new DataSetReadyCallback() {
                                public void callback( DataSet result ) {
                                    Widget w = createWidget();
                                    panel.clear();
                                    panel.add( w );
                                }

                                public void notFound() {
                                    displayMessage( "ERROR: Data set not found." );
                                }
                            }
                    );
                } catch ( Exception e ) {
                    displayMessage( "ERROR: " + e.getMessage() );
                }
            }
        }
    }

    /**
     * Just reload the data set and make the current google Viewer to redraw.
     */
    public void redraw() {
        table.redraw();
    }

    /**
     * Clear the current display and show a notification message.
     */
    public void displayMessage( String msg ) {
        panel.clear();
        panel.add( label );
        label.setText( msg );
    }

    protected Widget createWidget() {
        pageSize = dataDisplayer.getPageSize();
        numberOfRows = dataSetHandler.getDataSetMetadata().getNumberOfRows();

        final PagedTable<UFTableRow> table = createUFTable();

        String defaultSortColumn = dataDisplayer.getDefaultSortColumnId();

        // TODO get the table to show the order icon programatically
        if ( defaultSortColumn != null && !"".equals( defaultSortColumn ) ) {
            lookupDataSet(
                    defaultSortColumn,
                    dataDisplayer.getDefaultSortOrder(),
                    new DataSetReadyCallback() {
                        @Override
                        public void callback( DataSet dataSet ) {
                            table.redraw();
                        }

                        @Override
                        public void notFound() {
                            displayMessage( "ERROR in data lookup." );
                        }
                    }
            );
        }

        this.table = table;
        dataProvider.addDataDisplay( table );

        HTML titleHtml = new HTML();
        if ( dataDisplayer.isTitleVisible() ) {
            titleHtml.setText( dataDisplayer.getTitle() );
        }

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add( titleHtml );
        verticalPanel.add( table );
        return verticalPanel;
    }

    protected PagedTable<UFTableRow> createUFTable() {

        final ExtPT<UFTableRow> ufPagedTable = new ExtPT<UFTableRow>( pageSize );
        ColumnSortEvent.AsyncHandler sortHandler = new ColumnSortEvent.AsyncHandler( ufPagedTable ) {

            @Override
            public void onColumnSort( ColumnSortEvent event ) {
                lookupDataSet(
                        event.getColumn().getDataStoreName(),
                        event.isSortAscending() ? SortOrder.ASCENDING : SortOrder.DESCENDING,
                        new DataSetReadyCallback() {
                            @Override
                            public void callback( DataSet dataSet ) {
                                ufPagedTable.redraw();
                            }

                            @Override
                            public void notFound() {
                                displayMessage( "ERROR in data lookup." );
                            }
                        }
                );
            }

        };

        ufPagedTable.setColumnSortHandler( sortHandler );

//        PagedTable ufPagedTable = new PagedTable(pageSize, sortHandler);ufPagedTable.get
        ufPagedTable.setRowCount( numberOfRows, true );
        int height = 38 * pageSize;
        ufPagedTable.setHeight( ( height > ( Window.getClientHeight() - this.getAbsoluteTop() ) ? ( Window.getClientHeight() - this.getAbsoluteTop() ) : height ) + "px" );
        ufPagedTable.setWidth( Window.getClientWidth() - ( this.getAbsoluteLeft() + this.getOffsetWidth() ) + "px" );
        ufPagedTable.setEmptyTableCaption( "No data available" );

        List<DataColumn> columns = dataSet.getColumns();
        for ( int i = 0; i < columns.size(); i++ ) {
            final _Integer colNum = new _Integer( i );
            DataColumn dataColumn = columns.get( i );
            ColumnType columnType = dataColumn.getColumnType();
            String columnId = dataColumn.getId();

            switch ( columnType ) {
                case LABEL:
                    // Initialize the column selections map
                    columnCellSelections.put( columnId, new ArrayList<KeyValue<String, Integer>>(5) );

                    Column labelColumn =
                            new Column<UFTableRow, String>(
                                    new SelectableTextCell( columnId ) ) {
                                        @Override
                                        public String getValue( UFTableRow row ) {
                                            Object value = dataSet.getValueAt( row.getRowNumber(), colNum.getColNum() );
                                            return value.toString();
                                        }
                                    };

                    labelColumn.setSortable( true );
                    ufPagedTable.addColumn( labelColumn, columnId );
                    break;

                case NUMBER:
                    Column numberColumn =
                            new Column<UFTableRow, Number>( new NumberCell( NumberFormat.getFormat( "#.###" ) ) ) {
                                @Override
                                public Number getValue( UFTableRow row ) {
                                    return ( Number ) dataSet.getValueAt( row.getRowNumber(), colNum.getColNum() );
                                }
                            };
                    numberColumn.setSortable( true );
                    ufPagedTable.addColumn( numberColumn, columnId );
                    break;

                case DATE:
                    Column dateColumn =
                            new Column<UFTableRow, Date>( new DateCell( DateTimeFormat.getFormat( DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM ) ) ) {
                                @Override
                                public Date getValue( UFTableRow row ) {
                                    return ( Date ) dataSet.getValueAt( row.getRowNumber(), colNum.getColNum() );
                                }
                            };
                    dateColumn.setSortable( true );
                    ufPagedTable.addColumn( dateColumn, columnId );
                    break;

                default:
            }
        }
        return ufPagedTable;
    }

    protected void lookupDataSet( String columnId, SortOrder sortOrder, DataSetReadyCallback callback ) {
        doLookupDataSet( null, null, columnId, sortOrder, callback );
    }

    protected void lookupDataSet( int lowerLimit, int upperLimit, final DataSetReadyCallback callback ) {
        // Avoid double lookup at the time of widget creation
        if ( dataSetLowerLimit != lowerLimit || dataSetUpperLimit != upperLimit ) {
            this.dataSetLowerLimit = lowerLimit;
            this.dataSetUpperLimit = upperLimit;
            doLookupDataSet( lowerLimit, upperLimit, null, null, callback );
        } else {
            // If it was already looked up just return the dataSet
            callback.callback( dataSet );
        }
    }

    private void doLookupDataSet( Integer lowerLimit, Integer upperLimit, String columnId, SortOrder sortOrder, final DataSetReadyCallback callback ) {

        if ( lowerLimit != null && upperLimit != null ) {
            dataSetHandler.limitDataSetRows( lowerLimit, upperLimit );
        }

        if ( columnId != null && !"".equals( columnId ) ) {
            sortApply( columnId, sortOrder );
        }

        try {
            dataSetHandler.lookupDataSet(
                    // 'local' callback to set the new dataSet to the viewer
                    new DataSetReadyCallback() {
                        @Override
                        public void callback( DataSet dataSet ) {
                            UFTableViewer.this.dataSet = dataSet;
                            callback.callback( dataSet );
                        }

                        @Override
                        public void notFound() {
                            callback.notFound();
                        }
                    }
            );
        } catch ( Exception e ) {
            displayMessage( "ERROR: " + e.getMessage() );
        }
    }

    private String getColumnSelectedColor( String columnId ) {
        String columnColor = columnColorMap.get(columnId);
        if ( columnColor == null ) {
            columnColorMap.put( columnId, columnColor = COLOR_ARRAY[ colorIndex++ % COLOR_ARRAY.length ] );
        }
        return columnColor;
    }

    private class SelectableTextCell extends TextCell {

        private String columnId;
        private String filterColor;

        private SelectableTextCell( String columnId ) {
            this.columnId = columnId;
            this.filterColor = getColumnSelectedColor( columnId );
        }

        @Override
        public Set<String> getConsumedEvents() {
            Set<String> consumedEvents = new HashSet<String>();
            consumedEvents.add( CLICK );
            return consumedEvents;
        }

        @Override
        public void onBrowserEvent( Context context, Element parent, String value, NativeEvent event, ValueUpdater<String> valueUpdater ) {

            KeyValue<String, Integer> selectedCell = new KeyValue<String, Integer>( value, context.getIndex() );

            // In this column no cell has been selected so far
            List<KeyValue<String, Integer>> selectedCells = columnCellSelections.get( columnId );
            if ( selectedCells.size() == 0 ) {
                markSelected( parent.getParentElement() );
                selectedCells.add( selectedCell );

                filterUpdate( columnId, value, -1 );

            // Some value already selected in same column
            } else {
                // Same cell (i.e. same value, and same row) clicked --> remove styles and remove from filter
                if ( selectedCells.contains( selectedCell ) ) {
                    unMarkSelected( parent.getParentElement() );
                    selectedCells.remove( selectedCell );

                    filterUpdate( columnId, value, -1 );

                // Different cell clicked
                } else {
                    int i = 0;
                    boolean sameValueSelected = false;
                    while (!sameValueSelected && i < selectedCells.size() ) {
                        if ( selectedCell.getKey().equalsIgnoreCase( selectedCells.get( i++ ).getKey() ) ) sameValueSelected = true;
                    }
                    // Different cell, and with different value in the same column
                    if ( !sameValueSelected ) {
                        markSelected( parent.getParentElement() );
                        selectedCells.add( selectedCell );

                        filterUpdate( columnId, value, -1 );
                    } // else (i.e. different cell but with same value) do nothing
                }
            }
        }

        private void markSelected( Element element ) {
            Style style = element.getStyle();
            style.setBorderWidth( 1, Style.Unit.PX );
            style.setBorderStyle( Style.BorderStyle.SOLID );
            style.setBorderColor( filterColor );
        }

        private void unMarkSelected( Element element ) {
            Style style = element.getStyle();
            style.clearBorderWidth();
            style.clearBorderStyle();
            style.clearBorderColor();
        }

        private double parseBorderWidth( String former ) {
            String size = former.replaceAll( "[a-z %]", "" );
            return Double.parseDouble( "".equals( size ) ? "0" : size );
        }

        private Style.Unit parseBorderWidthUnit( String former ) {
            String unit = former.replaceAll( "[0-9 ]", "" ).toUpperCase();
            return Style.Unit.valueOf( "".equals( unit ) ? "PX" : unit );
        }
    }

    // TODO workaround, to be removed
    private final class ExtPT<T> extends PagedTable<T> {

        private ExtPT( int pageSize ) {
            super( pageSize );
        }

        private void setColumnSortHandler( ColumnSortEvent.AsyncHandler handler ) {
            dataGrid.addColumnSortHandler( handler );
        }
    }

    private final class _Integer {

        private int colNum;

        private _Integer( int colNum ) {
            this.colNum = colNum;
        }

        private int getColNum() {
            return colNum;
        }
    }

    private final class KeyValue<K, V> {
        private K key;
        private V value;

        private KeyValue( K key, V value ) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        @Override
        public boolean equals( Object o ) {
            if ( this == o ) {
                return true;
            }
            if ( o == null || getClass() != o.getClass() ) {
                return false;
            }

            KeyValue keyValue = ( KeyValue ) o;

            if ( key != null ? !key.equals( keyValue.key ) : keyValue.key != null ) {
                return false;
            }
            if ( value != null ? !value.equals( keyValue.value ) : keyValue.value != null ) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = key != null ? key.hashCode() : 0;
            result = 31 * result + ( value != null ? value.hashCode() : 0 );
            return result;
        }
    }
}
