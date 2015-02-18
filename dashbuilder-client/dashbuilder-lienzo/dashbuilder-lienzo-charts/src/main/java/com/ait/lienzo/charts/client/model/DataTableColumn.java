package com.ait.lienzo.charts.client.model;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.i18n.client.DateTimeFormat;

import java.util.Date;

/**
 * <p>Columns are used to represent data types and other definition parameters for the values of the table entries.</p>
 * 
 * <p>There exist three type of columns depending on the data type of the value that the column stores:</p>
 * <ul>
 *     <li><code>String</code> column types - Contains string values.</li>
 *     <li><code>Numberic</code> column types - Contains numeric values.</li>
 *     <li><code>Date</code> column types - Contains date values.</li>
 * </ul>
 */
public final class DataTableColumn {

    private DataTableColumnJSO m_jso;

    public DataTableColumn(String id, DataTableColumnType type) {
        this(DataTableColumnJSO.make(id, type));
    }
    
    public DataTableColumn(DataTableColumnJSO m_jso) {
        this.m_jso = m_jso;
    }

    public static final class DataTableColumnJSO extends JsArrayMixed{
        
        protected DataTableColumnJSO() {
            
        }
        
        public static final DataTableColumnJSO make()
        {
            return JsArray.createArray().cast();
        }

        public static final DataTableColumnJSO make(String id, DataTableColumnType type) {
            DataTableColumnJSO dataTableColumn = make();
            dataTableColumn.setId(id);
            dataTableColumn.setType(type);
            return dataTableColumn;
        }

        public final native void setId(String id) /*-{
            this.id = id;
        }-*/;

        public final native void setType(DataTableColumnType type) /*-{
            this.type = type;
        }-*/;

        public final native String getId() /*-{
            return this.id;
        }-*/;

        public final native DataTableColumnType getType() /*-{
            return this.type;
        }-*/;

        
    }

    public final String getId() {
        return this.m_jso.getId();
    }

    public final DataTableColumnType getType() {
        return this.m_jso.getType();
    }
    
    public final int length() {
        return this.m_jso.length();
    }
    
    protected DataTableColumnJSO getJSO() {
        return m_jso;
    }

    public final void addValue(String value) {
        this.m_jso.push(value); 
    }

    public final void addValue(double value) {
        this.m_jso.push(value);
    }

    public final void addValue(Date value) {
        String s = fromDate(value);
        this.m_jso.push(s);
    }

    public final void setValue(int pos, String value) {
        this.m_jso.set(pos, value);
    }

    public final void setValue(int pos, double value) {
        this.m_jso.set(pos, value);
    }

    public final void setValue(int pos, Date value) {
        this.m_jso.set(pos, fromDate(value));
    }
    
    public final String getStringValue(int pos) {
        return this.m_jso.getString(pos);
    }

    public final Double getNumericValue(int pos) {
        return this.m_jso.getNumber(pos);
    }

    public final Date getDateValue(int pos) {
        return toDate(this.m_jso.getString(pos));
    }
    
    public final String[] getStringValues() {
        if (!this.m_jso.getType().equals(DataTableColumnType.STRING)) return null;

        String[] result = new String[this.m_jso.length()];
        for (int x = 0; x < this.m_jso.length(); x++) {
            result[x] = getStringValue(x);
        }
        return result;
    }

    public final Double[] getNumericValues() {
        if (!this.m_jso.getType().equals(DataTableColumnType.NUMBER)) return null;

        Double[] result = new Double[this.m_jso.length()];
        for (int x = 0; x < this.m_jso.length(); x++) {
            result[x] = getNumericValue(x);
        }
        return result;
    }

    public final Date[] getDateValues() {
        if (!this.m_jso.getType().equals(DataTableColumnType.DATE)) return null;

        Date[] result = new Date[this.m_jso.length()];
        for (int x = 0; x < this.m_jso.length(); x++) {
            result[x] = getDateValue(x);
        }
        return result;
    }
    
    protected final Date toDate(String value) {
        return DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_FULL).parse(value);
    }
    
    protected final String fromDate(Date value) {
        return DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_FULL).format(value);
    }

    public enum DataTableColumnType {
        /**
         * Text column
         */
        STRING("string"),
        /**
         * Integer or decimal column
         */
        NUMBER("number"),
        /**
         * Date column (without time)
         */
        DATE("date");

        /**
         * Find the column type by name.
         *
         * @param name The column type name.
         * @return the type corresponding to the provided name.
         */
        public static DataTableColumnType lookup(String name) {
            for (DataTableColumnType columnType : DataTableColumnType.values()) {
                if (columnType.getName().equals(name)) {
                    return columnType;
                }
            }
            return null;
        }

        private final String name;

        private DataTableColumnType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
