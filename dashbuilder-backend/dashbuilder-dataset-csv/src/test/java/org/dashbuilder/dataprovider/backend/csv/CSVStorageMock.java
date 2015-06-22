package org.dashbuilder.dataprovider.backend.csv;

import java.io.InputStream;
import javax.enterprise.inject.Specializes;

import org.dashbuilder.dataset.def.CSVDataSetDef;
import org.uberfire.backend.vfs.Path;

public class CSVStorageMock implements CSVFileStorage {

    @Override public InputStream getCSVInputStream(CSVDataSetDef def) {
        return null;
    }

    @Override public String getCSVString(CSVDataSetDef def) {
        return null;
    }

    @Override public void saveCSVFile(CSVDataSetDef def) {
    }

    @Override public void deleteCSVFile(CSVDataSetDef def) {
    }
}
