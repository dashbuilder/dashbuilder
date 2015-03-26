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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import au.com.bytecode.opencsv.CSVWriter;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetBackendServices;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetMetadata;
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

    @Inject BackendDataSetManager dataSetManager;
    @Inject DataSetDefDeployer dataSetDefDeployer;
    @Inject DataSetDefRegistry dataSetDefRegistry;

/*
    @Inject
    @Named("ioStrategy")
*/
    private IOService ioService;

    /* TODO this is temporary (workaround to avoid an 'ambigous dependency' WELD error */
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
        /* TODO this is temporary (workaround to avoid an 'ambigous dependency' WELD error */
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

    public DataSet lookupDataSet(DataSetLookup lookup) throws Exception {
        return dataSetManager.lookupDataSet(lookup);
    }

    public DataSetMetadata lookupDataSetMetadata(String uuid) throws Exception {
        return dataSetManager.getDataSetMetadata( uuid );
    }

    public List<DataSetDef> getPublicDataSetDefs() {
        return dataSetDefRegistry.getDataSetDefs( true );
    }

    public String exportDataSetCSV(DataSetLookup lookup) {
        DataSet dataSet = dataSetManager.lookupDataSet( lookup );
        return exportDataSetCSV( dataSet );
    }

    public String exportDataSetCSV(DataSet dataSet) {
        if (dataSet == null) throw new IllegalArgumentException("Null dataSet specified!");
        int columnCount = dataSet.getColumns().size();
        int rowCount = dataSet.getRowCount();

        List<String[]> lines = new ArrayList<String[]>(rowCount+1);

        String[] line = new String[columnCount];
        for (int cc = 0; cc < columnCount; cc++) {
            DataColumn dc = dataSet.getColumnByIndex(cc);
            line[cc] = dc.getName();
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

    protected String formatAsString(Object value) {
        if (value == null) return "";
        if (value instanceof Number) return decf.format(value);
        else if (value instanceof Date) return datef.format(value);
        // TODO verify if this is correct
        else if (value instanceof Interval) return ((Interval)value).getName();
        else return value.toString();
    }
}
