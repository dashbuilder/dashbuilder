package org.dashbuilder.client.uftable;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import org.dashbuilder.client.dataset.DataSetReadyCallback;
import org.dashbuilder.model.dataset.DataSet;
import org.uberfire.client.tables.PagedTable;

public class UFTableDataProvider extends AsyncDataProvider<UFTableRow> {

    private UFTableViewer viewer;
    private List<UFTableRow> rows = new ArrayList<UFTableRow>(20);

    public UFTableDataProvider( UFTableViewer viewer ) {
        this.viewer = viewer;
    }

    @Override
    protected void onRangeChanged(final HasData<UFTableRow> display) {
        int start = ( ( PagedTable ) display ).getPageStart();
        int numberOfRows = viewer.getNumberOfRows();
        int _pageSize = ( ( PagedTable ) display ).getPageSize();
        int pageSize = numberOfRows <= _pageSize ? numberOfRows : _pageSize;
        // Only reload the row list if the page size were to change, because we're limiting the dataSet's number of rows
        // programatically, so its internal indexes will always range from 0 -> pageSize -1
        if ( pageSize != rows.size() ) {
            rows.clear();
            for (int i = 0; i < pageSize; i++) {
                rows.add( new UFTableRow( i ) );
            }
        }
        viewer.lookupDataSet(
                start,
                start + pageSize,
                new DataSetReadyCallback() {
                    @Override public void callback(DataSet dataSet) {
                        updateRowData( ( ( PagedTable ) display ).getPageStart(), rows );
                    }

                    @Override public void notFound() {
                        viewer.displayMessage("ERROR: Data set not found.");
                    }
                }
        );
    }
}
