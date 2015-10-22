/**
 * Copyright (C) 2015 JBoss Inc
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
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.dashbuilder.config.Config;
import org.dashbuilder.dataprovider.backend.csv.CSVFileStorage;
import org.dashbuilder.dataset.backend.exception.ExceptionManager;
import org.dashbuilder.dataset.def.CSVDataSetDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.json.DataSetDefJSONMarshaller;
import org.dashbuilder.dataset.uuid.UUIDGenerator;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.IOException;
import org.uberfire.java.nio.base.options.CommentedOption;
import org.uberfire.java.nio.file.FileSystem;
import org.uberfire.java.nio.file.FileSystemAlreadyExistsException;
import org.uberfire.java.nio.file.FileVisitResult;
import org.uberfire.java.nio.file.Files;
import org.uberfire.java.nio.file.Path;
import org.uberfire.java.nio.file.SimpleFileVisitor;
import org.uberfire.java.nio.file.StandardDeleteOption;
import org.uberfire.java.nio.file.attribute.BasicFileAttributes;

import static org.uberfire.backend.server.util.Paths.*;
import static org.uberfire.commons.validation.PortablePreconditions.*;
import static org.uberfire.java.nio.file.Files.*;

/**
 * Data set definition registry implementation which stores data sets under GIT
 * <p>It's provided as an extension to the default in-memory based registry.</p>
 */
@ApplicationScoped
@Specializes
public class DataSetDefGitStorage extends DataSetDefRegistryImpl implements CSVFileStorage {

    public static final String DATASET_EXT = ".dset";
    public static final String CSV_EXT = ".csv";

    @Inject @Config("10485760" /* 10 Mb */)
    protected int maxCsvLength;

    @Inject
    @Named("ioStrategy")
    protected IOService ioService;

    @Inject
    protected DataSetDefJSONMarshaller jsonMarshaller;

    @Inject
    protected ExceptionManager exceptionManager;

    @Inject
    protected UUIDGenerator uuidGenerator;

    private FileSystem fileSystem;
    private Path root;

    @PostConstruct
    protected void init() {
        initFileSystem();
        deleteTempFiles();
        registerDataSetDefs();
    }

    protected void initFileSystem() {
        try {
            fileSystem = ioService.newFileSystem(URI.create("default://datasets"),
                    new HashMap<String, Object>() {{
                        put( "init", Boolean.TRUE );
                        put( "internal", Boolean.TRUE );
                    }});
        } catch ( FileSystemAlreadyExistsException e ) {
            fileSystem = ioService.getFileSystem(URI.create("default://datasets"));
        }
        this.root = fileSystem.getRootDirectories().iterator().next();
    }

    protected void registerDataSetDefs() {
        for (DataSetDef def : listDataSetDefs()) {
            super.dataSetDefMap.put(def.getUUID(), new DataSetDefEntry(def));
        }
    }

    @Override
    public void registerDataSetDef(DataSetDef def, String subjectId, String message) {

        if (def.getUUID() == null) {
            final String uuid = uuidGenerator.newUuid();
            def.setUUID(uuid);
        }

        if (subjectId == null || message == null) {
            ioService.startBatch(fileSystem);
        } else {
            ioService.startBatch(fileSystem, new CommentedOption(subjectId, message));
        }

        try {
            String defJson = jsonMarshaller.toJsonString(def);
            Path defPath = def.getVfsPath() == null ? resolvePath(def) : convert(def.getVfsPath());
            ioService.write(defPath, defJson);
            def.setVfsPath(convert(defPath));

            // CSV specific
            if (def instanceof CSVDataSetDef) {
                saveCSVFile((CSVDataSetDef) def);
            }
            super.registerDataSetDef(def, subjectId, message);
        }
        catch (Exception e) {
            throw exceptionManager.handleException(
                    new Exception("Can't register the data set definition\n" + def, e));
        }
        finally {
            ioService.endBatch();
        }
    }

    @Override
    public DataSetDef removeDataSetDef(String uuid, String subjectId, String message) {
        DataSetDef def = getDataSetDef(uuid);
        return removeDataSetDef(def, subjectId, message);
    }

    public void removeDataSetDef(org.uberfire.backend.vfs.Path path, String subjectId, String comment) {
        DataSetDef def = loadDataSetDef(path);
        if (def != null) {
            removeDataSetDef(def, subjectId, comment);
        }
    }

    public DataSetDef removeDataSetDef(DataSetDef def, String subjectId, String message) {
        if (def.getVfsPath() != null) {

            Path defPath = convert(def.getVfsPath());

            if (ioService.exists(defPath)) {
                if (subjectId == null || message == null) {
                    ioService.startBatch(fileSystem);
                } else {
                    ioService.startBatch(fileSystem, new CommentedOption(subjectId, message));
                }
                try {
                    ioService.deleteIfExists(defPath, StandardDeleteOption.NON_EMPTY_DIRECTORIES);

                    // CSV specific
                    if (def instanceof CSVDataSetDef) {
                        deleteCSVFile((CSVDataSetDef) def);
                    }
                } finally {
                    ioService.endBatch();
                }
            }
        }
        return super.removeDataSetDef(def.getUUID(), subjectId, message);
    }

    public Collection<DataSetDef> listDataSetDefs() {
        final Collection<DataSetDef> result = new ArrayList<DataSetDef>();

        if (ioService.exists(root)) {
            walkFileTree(checkNotNull("root", root),
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                        try {
                            checkNotNull("file", file);
                            checkNotNull("attrs", attrs);

                            if (file.getFileName().toString().endsWith(DATASET_EXT) && attrs.isRegularFile()) {
                                final org.uberfire.backend.vfs.Path path = convert(file);
                                final String json = ioService.readAllString(file);
                                DataSetDef def = jsonMarshaller.fromJson(json);
                                def.setVfsPath(path);
                                result.add(def);
                            }
                        } catch (final Exception e) {
                            log.error("Data set definition read error: " + file.getFileName(), e);
                            return FileVisitResult.TERMINATE;
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
        }
        return result;
    }

    public DataSetDef loadDataSetDef(org.uberfire.backend.vfs.Path path) {
        Path nioPath = convert(path);
        if (ioService.exists(nioPath)) {
            try {
                String json = ioService.readAllString(nioPath);
                DataSetDef def = jsonMarshaller.fromJson(json);
                def.setVfsPath(path);
                return def;
            } catch (Exception e) {
                String msg = "Error parsing data set JSON definition: " + path.getFileName();
                throw exceptionManager.handleException(new Exception(msg, e));
            }
        }
        return null;
    }

    public DataSetDef copyDataSetDef(DataSetDef def, String newName, String subjectId, String message) {
        DataSetDef clone = def.clone();
        clone.setUUID(uuidGenerator.newUuid());
        clone.setName(newName);
        clone.setVfsPath(null);

        if (subjectId == null || message == null) {
            ioService.startBatch(fileSystem);
        } else {
            ioService.startBatch(fileSystem, new CommentedOption(subjectId, message));
        }
        try {
            // CSV specific
            if (def instanceof CSVDataSetDef) {
                CSVDataSetDef csvDef = (CSVDataSetDef) def;
                CSVDataSetDef csvCloneDef = (CSVDataSetDef) clone;
                Path csvPath = resolveCsvPath(csvDef);
                Path cloneCsvPath = resolveCsvPath(csvCloneDef);
                ioService.copy(csvPath, cloneCsvPath);
                csvCloneDef.setFilePath(convert(cloneCsvPath).toURI());
            }
            String defJson = jsonMarshaller.toJsonString(clone);
            Path clonePath = resolvePath(clone);
            ioService.write(clonePath, defJson);
            clone.setVfsPath(convert(clonePath));

            super.registerDataSetDef(clone, subjectId, message);
            return clone;
        }
        catch (Exception e) {
            throw exceptionManager.handleException(
                    new Exception("Can't register the data set definition\n" + def, e));
        }
        finally {
            ioService.endBatch();
        }
    }

    public Path createTempFile(String fileName) {
        Path tempPath = resolveTempPath(fileName);
        return tempPath;
    }

    public void deleteTempFiles() {
        Path tempPath = getTempPath();
        if (ioService.exists(tempPath)) {
            ioService.startBatch(fileSystem, new CommentedOption("system", "Delete temporal files"));
            try {
                walkFileTree(tempPath,
                    new SimpleFileVisitor<Path>() {

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }
                    });
            } finally {
                ioService.endBatch();
            }
        }
    }

    private Path getDataSetsPath() {
        return root.resolve("definitions");
    }

    private Path getTempPath() {
        return root.resolve("tmp");
    }

    protected Path resolveTempPath(String fileName) {
        return getTempPath().resolve(fileName);
    }

    protected Path resolvePath(DataSetDef def) {
        return getDataSetsPath().resolve(def.getUUID() + DATASET_EXT);
    }

    //
    // CSV files storage
    //

    @Override
    public String getCSVString(CSVDataSetDef def) {
        Path nioPath = resolveCsvPath(def);
        if (ioService.exists(nioPath)) {
            return ioService.readAllString(nioPath);
        }
        return null;
    }

    @Override
    public InputStream getCSVInputStream(CSVDataSetDef def) {
        Path nioPath = resolveCsvTempPath(def);
        if (ioService.exists(nioPath)) {
            // In edition process ...
            return ioService.newInputStream(nioPath);
        }
        nioPath = resolveCsvPath(def);
        if (ioService.exists(nioPath)) {
            // Already created & persisted
            return ioService.newInputStream(nioPath);
        }
        return null;
    }

    @Override
    public void deleteCSVFile(CSVDataSetDef def) {
        Path csvPath = resolveCsvPath(def);

        if (ioService.exists(csvPath)) {
            ioService.deleteIfExists(csvPath, StandardDeleteOption.NON_EMPTY_DIRECTORIES);
        }
    }

    @Override
    public void saveCSVFile(CSVDataSetDef def) {
        String path = def.getFilePath();
        if (StringUtils.isBlank(path)) {
            return;
        }

        // The CSV file was uploaded from UI to the temp directory => move the file to the definitions directory
        Path csvTempPath = resolveCsvTempPath(def);
        if (ioService.exists(csvTempPath)) {
            Path csvPath = resolveCsvPath(def);
            if (ioService.exists(csvPath)) {
                // Avoid FileAlreadyExistsException on call to move (see below)
                ioService.delete(csvPath);
            }
            ioService.move(csvTempPath, csvPath);
            return;
        }
        // The CSV was registered or deployed via API => Copy the file contents to the definitions directory
        File csvFile = new File(path);
        if (csvFile.exists()) {
            if (csvFile.length() > maxCsvLength) {
                String msg = "CSV file length exceeds the maximum allowed: " + maxCsvLength / 1024 + " Kb";
                throw exceptionManager.handleException(new Exception(msg));
            }

            try {
                Path defPath = resolveCsvPath(def);
                String csvContent = FileUtils.readFileToString(csvFile);
                ioService.write(defPath, csvContent);
            } catch (Exception e) {
                String msg = "Error saving CSV file: " + csvFile;
                throw exceptionManager.handleException(new Exception(msg, e));
            }
        }
    }

    protected Path resolveCsvPath(CSVDataSetDef def) {
        return getDataSetsPath().resolve(def.getUUID() + CSV_EXT);
    }

    protected Path resolveCsvTempPath(CSVDataSetDef def) {
        return resolveTempPath(def.getUUID() + CSV_EXT);
    }
}
