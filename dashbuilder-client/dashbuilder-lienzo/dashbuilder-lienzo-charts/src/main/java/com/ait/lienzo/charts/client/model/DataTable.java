package com.ait.lienzo.charts.client.model;

import com.google.gwt.core.client.JsArray;

import java.util.Date;

/**
 * <h2>Introduction</h2>
 * <p>Data table model class to use as common source data set for charts.</p>
 * <p>It is designed as a bi-dimensional array that provide a set of rows and columns.</p>
 * 
 * <p>Rows:</p>
 * <ul>
 *     <li><p>Rows are used to represent data set entries providing the values for each column.</p></li>
 *     <li><p>A row is uniquely identified by an integer value that represents the index inside the data table.</p></li>
 * </ul>
 * 
 * <p>Columns:</p>
 * <p>Columns are used to represent data types and other definition parameters for the entries' values.</p>
 * @see DataTableColumn
 * 
 * <h2>Usage</h2>
 * <p>Creating and populating a data table consists of three steps:</p>
 * <ol>
 *     <li><p>Create the javascript object</p></li>
 *     <li><p>Add column definitions</p></li>
 *     <li><p>Populate with data</p></li>
 * </ol>
 * 
 * <p>Example:</p>
 * <code>
 * #Create the javascript object.
 * DataTable data = DataTable.create();
 * 
 * # Creating column definitions.
 * data.addColumn("department", DataTableColumnType.STRING);
 * data.addColumn("date", DataTableColumnType.DATE);
 * data.addColumn("amount", DataTableColumnType.NUMBER);
 * 
 * # Inserting a row
 * data.addValue("department", "Sales" );
 * data.addValue("date", new Date() );
 * data.addValue("amount", 128.5 );
 * </code>
 * 
 * <p>You can also modify existing values or add new ones at a certain row index:</p>
 * <code>
 *     
 * </code>
 * data.setValue("department", 0, "Sales" );
 * data.setValue("date", 0, new Date() );
 * data.setValue("amount", 0, 128.5 );
 */
public final class DataTable {

    private DataTableJSO m_jso;
    
    public DataTable() {
        this.m_jso = DataTableJSO.make();
    }

    /**
     * The javascript native object.
     */
    public static final class DataTableJSO extends JsArray<DataTableColumn.DataTableColumnJSO> {
        protected DataTableJSO() {
        }

        public static final DataTableJSO make()
        {
            return JsArray.createArray().cast();
        }

        public final void addColumn(DataTableColumn.DataTableColumnJSO column) {
            push(column);
        }
    }

    public final void addColumn(DataTableColumn column) {
        if (column != null && getColumn(column.getId()) == null) this.m_jso.addColumn(column.getJSO());
    }

    public final void addColumn(String id, DataTableColumn.DataTableColumnType type) {
        if (getColumn(id) == null) this.addColumn(new DataTableColumn(id, type));
    }

    public final DataTableColumn getColumn(String id) {
        if (id == null) return null;
        for (int x = 0; x < this.m_jso.length(); x++) {
            DataTableColumn column = new DataTableColumn(this.m_jso.get(x));
            if (column.getId().equals(id)) return column;
        }
        return null;
    }

    public final DataTableColumn getColumn(int pos) {
        return  new DataTableColumn(this.m_jso.get(pos));
    }
    
    public final void addValue(String columnId, String value) {
        if (columnId != null) {
            DataTableColumn column = getColumn(columnId);
            if  (column != null) {
                column.addValue(value);
            }
        }
    }

    public final void addValue(String columnId, double value) {
        if (columnId != null) {
            DataTableColumn column = getColumn(columnId);
            if  (column != null) {
                column.addValue(value);
            }
        }
    }

    public final void addValue(String columnId, Date value) {
        if (columnId != null) {
            DataTableColumn column = getColumn(columnId);
            if  (column != null) {
                column.addValue(value);
            }
        }
    }

    public final void setValue(String columnId, int pos, String value) {
        if (columnId != null) {
            DataTableColumn column = getColumn(columnId);
            if  (column != null) {
                column.setValue(pos, value);
            }
        }
    }

    public final void setValue(String columnId, int pos, double value) {
        if (columnId != null) {
            DataTableColumn column = getColumn(columnId);
            if  (column != null) {
                column.setValue(pos, value);
            }
        }
    }

    public final void setValue(String columnId, int pos, Date value) {
        if (columnId != null) {
            DataTableColumn column = getColumn(columnId);
            if  (column != null) {
                column.setValue(pos, value);
            }
        }
    }

    public final String getStringValue(String columnId, int pos) {
        if (columnId != null) {
            DataTableColumn column = getColumn(columnId);
            if  (column != null) {
                return column.getStringValue(pos);
            }
        }
        
        return null;
    }

    public final Double getNumericValue(String columnId, int pos) {
        if (columnId != null) {
            DataTableColumn column = getColumn(columnId);
            if  (column != null) {
                return column.getNumericValue(pos);
            }
        }

        return null;
    }

    public final Date getDateValue(String columnId, int pos) {
        if (columnId != null) {
            DataTableColumn column = getColumn(columnId);
            if  (column != null) {
                return column.getDateValue(pos);
            }
        }

        return null;
    }

    public final String[] getStringValues(String columnId) {
        if (columnId != null) {
            DataTableColumn column = getColumn(columnId);
            if  (column != null) {
                return column.getStringValues();
            }
        }

        return null;
    }

    public final Double[] getNumericValues(String columnId) {
        if (columnId != null) {
            DataTableColumn column = getColumn(columnId);
            if  (column != null) {
                return column.getNumericValues();
            }
        }

        return null;
    }

    public final Date[] getDateValues(String columnId) {
        if (columnId != null) {
            DataTableColumn column = getColumn(columnId);
            if  (column != null) {
                return column.getDateValues();
            }
        }

        return null;
    }
    
    public final int size() {
        if (this.m_jso.length() > 0) {
            return getColumn(0).length();
        }
        return 0;
    }
    
}
