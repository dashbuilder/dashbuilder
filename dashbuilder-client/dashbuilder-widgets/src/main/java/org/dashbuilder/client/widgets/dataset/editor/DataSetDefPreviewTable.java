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
package org.dashbuilder.client.widgets.dataset.editor;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.def.DataColumnDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.TableDisplayerSettingsBuilder;
import org.dashbuilder.displayer.client.DataSetEditHandler;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerListener;
import org.dashbuilder.displayer.client.DisplayerLocator;
import org.dashbuilder.displayer.impl.TableDisplayerSettingsBuilderImpl;
import org.dashbuilder.renderer.client.DefaultRenderer;
import org.uberfire.client.mvp.UberView;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collection;

/**
 * <p>Data Set table preview presenter.</p>
 * 
 * @since 0.4.0 
 */
@Dependent
public class DataSetDefPreviewTable implements IsWidget {

    public interface View extends UberView<DataSetDefPreviewTable> {
        
        View setDisplayer(IsWidget widget);
        
        View clear();
    }

    DataSetClientServices clientServices;
    DisplayerLocator displayerLocator;
    public View view;

    Displayer tableDisplayer;

    @Inject
    public DataSetDefPreviewTable(final DisplayerLocator displayerLocator,
                                  final DataSetClientServices clientServices,
                                  final View view) {
        this.displayerLocator = displayerLocator;
        this.clientServices = clientServices;
        this.view = view;
    }

    @PostConstruct
    public void init() {
        view.init(this);
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public void show(final DataSetDef dataSetDef, final Collection<DataColumnDef> columns,
                     final DisplayerListener displayerListener) {
        clear();
        
        if (dataSetDef != null) {
            
            // Build the table displayer settings.
            final TableDisplayerSettingsBuilder<TableDisplayerSettingsBuilderImpl> tableDisplayerSettingsBuilder = DisplayerSettingsFactory.newTableSettings()
                    .dataset(dataSetDef.getUUID())
                    .renderer(DefaultRenderer.UUID)
                    .titleVisible(false)
                    .tablePageSize(6)
                    .tableOrderEnabled(true)
                    .filterOn(true, false, false);

            if (columns != null && !columns.isEmpty()) {
                for (final DataColumnDef column : columns) {
                    tableDisplayerSettingsBuilder.column(column.getId());
                }
            }

            final DisplayerSettings settings = tableDisplayerSettingsBuilder.buildSettings();
            // Disable backend cache for preview.
            DataSetDef editCloneWithoutCacheSettings = dataSetDef.clone();
            editCloneWithoutCacheSettings.setCacheEnabled(false);

            // Configure the table displayer and the data set handler for edition.
            tableDisplayer = displayerLocator.lookupDisplayer(settings);
            tableDisplayer.setDataSetHandler(new DataSetEditHandler(clientServices, settings.getDataSetLookup(), editCloneWithoutCacheSettings));
            draw(displayerListener);
        }

    }

    // Show the table displayer.
    void draw(final DisplayerListener displayerListener) {
        tableDisplayer.addListener(displayerListener);
        view.setDisplayer(tableDisplayer);
        tableDisplayer.draw();
    }
    
    public void clear() {
        tableDisplayer = null;
        view.clear();
    }
    
}
