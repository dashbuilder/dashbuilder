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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;
import javax.inject.Named;

import org.dashbuilder.dataset.def.DataSetDef;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.IOException;
import org.uberfire.java.nio.base.options.CommentedOption;
import org.uberfire.java.nio.file.FileSystem;
import org.uberfire.java.nio.file.FileSystemAlreadyExistsException;
import org.uberfire.java.nio.file.FileVisitResult;
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
public class DataSetDefGitRegistry extends DataSetDefRegistryImpl {

    public static final String DATASET_EXT = ".dset";

    @Inject
    @Named("ioStrategy")
    protected IOService ioService;

    @Inject
    protected DataSetDefJSONMarshaller jsonMarshaller;

    private FileSystem fileSystem;
    private Path root;

    @PostConstruct
    protected void init() {
        initFileSystem();
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
        if (subjectId == null || message == null) {
            ioService.startBatch(fileSystem);
        } else {
            ioService.startBatch(fileSystem, new CommentedOption(subjectId, message));
        }
        try {
            String defJson = jsonMarshaller.toJsonString(def);
            Path defPath = def.getVfsPath() == null ? resolvePath(def.getUUID()) : convert(def.getVfsPath());
            ioService.write(defPath, defJson);
            def.setVfsPath(convert(defPath));
            super.registerDataSetDef(def, subjectId, message);
        }
        catch (Exception e) {
            log.error("Can't register the data set definition\n" + def, e);
        }
        finally {
            ioService.endBatch();
        }
    }

    @Override
    public DataSetDef removeDataSetDef(String uuid, String subjectId, String message) {
        DataSetDef def = getDataSetDef(uuid);
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
                } finally {
                    ioService.endBatch();
                }
            }
        }
        return super.removeDataSetDef(uuid, subjectId, message);
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
                        } catch ( final Exception ex ) {
                            return FileVisitResult.TERMINATE;
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
        }
        return result;
    }

    protected Path resolvePath(String uuid) {
        return fileSystem.getPath(uuid + DATASET_EXT);
    }
}
