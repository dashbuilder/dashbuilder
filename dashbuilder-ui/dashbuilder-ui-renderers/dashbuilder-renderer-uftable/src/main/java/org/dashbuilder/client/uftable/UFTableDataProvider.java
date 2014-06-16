package org.dashbuilder.client.uftable;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import org.uberfire.client.tables.PagedTable;

public class UFTableDataProvider extends AsyncDataProvider<UFTableRow> {

    List<UFTableRow> rows = new ArrayList<UFTableRow>(20);

    @Override
    protected void onRangeChanged(HasData<UFTableRow> display) {
        rows.clear();
        int start = ( ( PagedTable ) display ).getPageStart();
        int pageSize = ( ( PagedTable ) display ).getPageSize();
        for (int i = start; i < start + pageSize; i++) {
            rows.add( new UFTableRow( i ) );
        }
        updateRowData( start, rows );
    }
}
