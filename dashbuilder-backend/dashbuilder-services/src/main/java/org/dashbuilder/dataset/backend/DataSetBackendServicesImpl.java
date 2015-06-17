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
package org.dashbuilder.dataset.backend;

import java.io.File;
import java.io.OutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import au.com.bytecode.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.dashbuilder.dataset.*;
import org.dashbuilder.dataset.def.DataColumnDef;
import org.dashbuilder.dataset.exception.DataSetLookupException;
import org.dashbuilder.dataset.backend.exception.ExceptionManager;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.dashbuilder.dataset.group.Interval;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.api.RpcContext;
import org.slf4j.Logger;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.Files;
import org.uberfire.java.nio.file.Path;

/**
 * Data set backend services implementation
 */
@ApplicationScoped
@Service
public class DataSetBackendServicesImpl implements DataSetBackendServices {

    @Inject
    private Logger log;

    @Inject
    private ExceptionManager exceptionManager;
    
    @Inject BackendDataSetManager dataSetManager;
    @Inject DataSetDefDeployer dataSetDefDeployer;
    @Inject DataSetDefRegistry dataSetDefRegistry;
    @Inject BackendUUIDGenerator backendUUIDGenerator;

/*
    @Inject
    @Named("ioStrategy")
*/
    private IOService ioService;

    /* TODO this is temporary (workaround to avoid an 'ambiguous dependency' WELD error */
    @Inject
    private Instance<IOService> ioServices;

    protected String DEFAULT_SEPARATOR_CHAR = ";";
    protected String DEFAULT_QUOTE_CHAR = "\"";
    protected String DEFAULT_ESCAPE_CHAR = "\\";

    protected String dateFormatPattern = "dd/MM/yyyy HH:mm:ss";
    protected String numberFormatPattern = "#,###.##########";

    protected DecimalFormat decf = new DecimalFormat(numberFormatPattern);
    protected DateFormat datef = new SimpleDateFormat(dateFormatPattern);

    @PostConstruct
    private void init() {
        /* TODO this is temporary (workaround to avoid an 'ambiguous dependency' WELD error */
        for ( IOService s : ioServices ) {
            ioService = s;
        }

        ServletContext servletContext = RpcContext.getHttpSession().getServletContext();
        if (!dataSetDefDeployer.isRunning() && servletContext != null) {
            String dir = servletContext.getRealPath("WEB-INF/datasets");
            if (dir != null && new File(dir).exists()) {
                dir = dir.replaceAll("\\\\", "/");
                dataSetDefDeployer.deploy(dir);
            }
        }
    }

    public String registerDataSetDef(DataSetDef definition) {
        // Data sets registered from the UI does not contain a UUID.
        if (definition.getUUID() == null) {
            final String uuid = backendUUIDGenerator.newUuid();
            definition.setUUID(uuid);
        }
        dataSetDefRegistry.registerDataSetDef(definition);
        return definition.getUUID();
    }

    public String updateDataSetDef(String uuid, DataSetDef definition) {
        // Data sets registered from the UI does not contain a UUID.
        final DataSetDef def = dataSetDefRegistry.getDataSetDef(uuid);
        if (def != null) {
            dataSetDefRegistry.removeDataSetDef(uuid);
        }
        definition.setUUID(uuid);
        dataSetDefRegistry.registerDataSetDef(definition);
        return definition.getUUID();
    }

    @Override
    public void removeDataSetDef(final String uuid) {
        final DataSetDef def = dataSetDefRegistry.getDataSetDef(uuid);
        if (def != null) {
            dataSetDefRegistry.removeDataSetDef(uuid);
        }
    }

    public DataSet lookupDataSet(DataSetLookup lookup) throws Exception {
        DataSet _d = null;
        try {
            _d = dataSetManager.lookupDataSet(lookup);
        } catch (DataSetLookupException e) {
            throw exceptionManager.handleException(e);
        }
        
        return _d;
    }

    public DataSet lookupDataSet(DataSetDef def, DataSetLookup lookup) throws Exception {
        try {
            // Although if using a not registered definition, it must have an uuid set for performing lookups.
            if (def.getUUID() == null) {
                final String uuid = backendUUIDGenerator.newUuid();
                def.setUUID(uuid);
                lookup.setDataSetUUID(uuid);
            }
            return dataSetManager.resolveProvider(def)
                    .lookupDataSet(def, lookup);
        } catch (Exception e) {
            throw exceptionManager.handleException(e);
        }
    }

    public DataSetMetadata lookupDataSetMetadata(String uuid) throws Exception {
        DataSetMetadata _d = null;
        try {
            _d = dataSetManager.getDataSetMetadata(uuid);
        } catch (DataSetLookupException e) {
            throw exceptionManager.handleException(e);
        }

        return _d;
    }

    @Override
    public EditDataSetDef prepareEdit(String uuid) throws Exception {
        DataSetMetadata _d = null;
        try {
            _d = dataSetManager.getDataSetMetadata(uuid);
            DataSetDef def = _d.getDefinition();
            
            // Clone the definition.
            DataSetDef cloned = def.clone();
            String newUuid = backendUUIDGenerator.newUuid();
            cloned.setUUID(newUuid);
            
            // Enable all columns and set columns to null, force to obtain metadata with all original columns 
            // and all original column types.
            boolean clonedAllColumns = cloned.isAllColumnsEnabled();
            List<DataColumnDef> clonedColumns = cloned.getColumns();
            cloned.setAllColumnsEnabled(true);
            cloned.setColumns(null);
            cloned.setPublic(false);
            
            // Obtain all original columns and all original column types.
            DataSetMetadata _cd = dataSetManager.resolveProvider(cloned)
                    .getDataSetMetadata(cloned);

            // Return the list of original columns and its types.
            List<DataColumnDef> columns = new ArrayList<DataColumnDef>();
            if (_cd.getNumberOfColumns() > 0) {
                for (int x = 0; x < _cd.getNumberOfColumns(); x++) {
                    String cId = _cd.getColumnId(x);
                    ColumnType cType = _cd.getColumnType(x);
                    DataColumnDef cdef = new DataColumnDef(cId, cType);
                    columns.add(cdef);
                }
            }
            
            // Set columns attributes as initialy were. 
            cloned.setAllColumnsEnabled(clonedAllColumns);
            cloned.setColumns(clonedColumns);
            return new EditDataSetDef(cloned, columns);
            
        } catch (DataSetLookupException e) {
            throw exceptionManager.handleException(e);
        }
    }

    public List<DataSetDef> getPublicDataSetDefs() {
        return dataSetDefRegistry.getDataSetDefs( true );
    }

    public String exportDataSetCSV(DataSetLookup lookup) {
        DataSet dataSet = dataSetManager.lookupDataSet( lookup );
        return exportDataSetCSV(dataSet);
    }

    public String exportDataSetCSV(DataSet dataSet) {
        if (dataSet == null) throw new IllegalArgumentException("Null dataSet specified!");
        int columnCount = dataSet.getColumns().size();
        int rowCount = dataSet.getRowCount();

        List<String[]> lines = new ArrayList<String[]>(rowCount+1);

        String[] line = new String[columnCount];
        for (int cc = 0; cc < columnCount; cc++) {
            DataColumn dc = dataSet.getColumnByIndex(cc);
            line[cc] = dc.getId();
        }
        lines.add(line);

        for (int rc = 0; rc < rowCount; rc++) {
            line = new String[columnCount];
            for (int cc = 0; cc < columnCount; cc++) {
                line[cc] = formatAsString(dataSet.getValueAt(rc, cc));
            }
            lines.add(line);
        }

        StringWriter swriter = new StringWriter();
        CSVWriter writer = new CSVWriter(swriter,   DEFAULT_SEPARATOR_CHAR.charAt(0),
                                                    DEFAULT_QUOTE_CHAR.charAt(0),
                                                    DEFAULT_ESCAPE_CHAR.charAt(0));

        Path tempCsvFilePath = null;
        try {
            writer.writeAll(lines);
            writer.close();
            tempCsvFilePath = ioService.createTempFile( "export", "csv", null );
            OutputStream os = Files.newOutputStream( tempCsvFilePath );
            os.write( swriter.getBuffer().toString().getBytes() );
            os.flush();
            os.close();
        } catch (Exception e) {
            log.error("Error in csv export: ", e);
        }
        return tempCsvFilePath.toString();
    }

    @Override
    public String exportDataSetExcel(DataSetLookup dataSetLookup) {
        DataSet dataSet = dataSetManager.lookupDataSet( dataSetLookup );
        return exportDataSetExcel( dataSet );
    }

    @Override
    public String exportDataSetExcel(DataSet dataSet) {
        // TODO?: Excel 2010 limits: 1,048,576 rows by 16,384 columns; row width 255 characters
        if (dataSet == null) throw new IllegalArgumentException("Null dataSet specified!");
        int columnCount = dataSet.getColumns().size();
        int rowCount = dataSet.getRowCount() + 1; //Include header row;
        int row = 0;

        SXSSFWorkbook wb = new SXSSFWorkbook(100); // keep 100 rows in memory, exceeding rows will be flushed to disk
        Map<String, CellStyle> styles = createStyles(wb);
        Sheet sh = wb.createSheet("Sheet 1");

        // General setup
        sh.setDisplayGridlines(true);
        sh.setPrintGridlines(false);
        sh.setFitToPage(true);
        sh.setHorizontallyCenter(true);
        PrintSetup printSetup = sh.getPrintSetup();
        printSetup.setLandscape(true);

        // Create header
        Row header = sh.createRow(row++);
        header.setHeightInPoints(20f);
        for (int i = 0; i < columnCount; i++) {
            Cell cell = header.createCell(i);
            cell.setCellStyle(styles.get("header"));
            cell.setCellValue(dataSet.getColumnByIndex(i).getId());
        }

        // Create data rows
        for (; row < rowCount; row++) {
            Row _row = sh.createRow(row);
            for (int cellnum = 0; cellnum < columnCount; cellnum++) {
                Cell cell = _row.createCell(cellnum);
                Object value = dataSet.getValueAt(row - 1, cellnum);
                if (value instanceof Short || value instanceof Long || value instanceof Integer || value instanceof BigInteger ) {
                    cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                    cell.setCellStyle(styles.get("integer_number_cell"));
                    cell.setCellValue(((Number) value).doubleValue());
                } else if (value instanceof Float || value instanceof Double || value instanceof BigDecimal ) {
                    cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                    cell.setCellStyle(styles.get("decimal_number_cell"));
                    cell.setCellValue(((Number) value).doubleValue());
                } else if (value instanceof Date) {
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    cell.setCellStyle(styles.get("date_cell"));
                    cell.setCellValue((Date) value);
                } else if (value instanceof Interval) {
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    cell.setCellStyle(styles.get("text_cell"));
                    cell.setCellValue(((Interval) value).getName());
                } else {
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    cell.setCellStyle(styles.get("text_cell"));
                    cell.setCellValue(value.toString());
                }
            }
        }

        // Adjust column size
        for (int i = 0; i < columnCount; i++) {
            sh.autoSizeColumn(i);
        }

        Path tempExcelFilePath = null;
        try {
            tempExcelFilePath = ioService.createTempFile( "export", "xlsx", null );
            OutputStream os = Files.newOutputStream( tempExcelFilePath );
            wb.write(os);
            os.flush();
            os.close();
        } catch (Exception e) {
            log.error("Error in excel export: ", e);
        }

        // Dispose of temporary files backing this workbook on disk
        if (!wb.dispose()) log.warn("Could not dispose of temporary file associated to data export!");

        return tempExcelFilePath.toString();
    }

    private String formatAsString(Object value) {
        if (value == null) return "";
        if (value instanceof Number) return decf.format(value);
        else if (value instanceof Date) return datef.format(value);
        // TODO verify if this is correct
        else if (value instanceof Interval) return ((Interval)value).getName();
        else return value.toString();
    }

    private Map<String, CellStyle> createStyles(Workbook wb){
        Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
        CellStyle style;

        Font titleFont = wb.createFont();
        titleFont.setFontHeightInPoints((short)12);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFillForegroundColor( IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setFont(titleFont);
        style.setWrapText(false);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());
        styles.put("header", style);

        Font cellFont = wb.createFont();
        cellFont.setFontHeightInPoints((short)10);
        cellFont.setBoldweight(Font.BOLDWEIGHT_NORMAL);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setVerticalAlignment(CellStyle.VERTICAL_BOTTOM);
        style.setFont(cellFont);
        style.setWrapText(false);
        style.setDataFormat(wb.createDataFormat().getFormat( BuiltinFormats.getBuiltinFormat( 3 )));
        styles.put("integer_number_cell", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setVerticalAlignment(CellStyle.VERTICAL_BOTTOM);
        style.setFont(cellFont);
        style.setWrapText(false);
        style.setDataFormat(wb.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(4)));
        styles.put("decimal_number_cell", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_LEFT);
        style.setVerticalAlignment(CellStyle.VERTICAL_BOTTOM);
        style.setFont(cellFont);
        style.setWrapText(false);
        style.setDataFormat( (short) BuiltinFormats.getBuiltinFormat("text") );
        styles.put("text_cell", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_BOTTOM);
        style.setFont(cellFont);
        style.setWrapText(false);
        style.setDataFormat(wb.createDataFormat().getFormat( DateFormatConverter.convert( Locale.getDefault(), dateFormatPattern )));
        styles.put("date_cell", style);
        return styles;
    }
    
    public void persistDataSetDef(final DataSetDef dataSetDef) throws Exception {
        dataSetDefDeployer.persist(dataSetDef);
    }

    @Override
    public void deleteDataSetDef(final String uuid) {
        final DataSetDef def = dataSetDefRegistry.getDataSetDef(uuid);
        if (def != null) {
            dataSetDefRegistry.removeDataSetDef(uuid);
            dataSetDefDeployer.delete(def);
        }
    }
}
