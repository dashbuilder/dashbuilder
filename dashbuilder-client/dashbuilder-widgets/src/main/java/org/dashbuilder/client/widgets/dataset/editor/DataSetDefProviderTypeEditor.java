/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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

import com.google.gwt.editor.client.EditorError;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.widgets.resources.i18n.DataSetEditorConstants;
import org.dashbuilder.common.client.editor.list.HorizImageListEditor;
import org.dashbuilder.common.client.editor.list.ImageListEditor;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.client.resources.bundles.DataSetClientResources;
import org.uberfire.client.mvp.UberView;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>Data Set provider type editor presenter.</p>
 * 
 * @since 0.4.0 
 */
@Dependent
public class DataSetDefProviderTypeEditor implements IsWidget, org.dashbuilder.dataset.client.editor.DataSetDefProviderTypeEditor  {

    public interface View extends UberView<DataSetDefProviderTypeEditor> {
        /**
         * <p>Specify the views to use for each sub-editor before calling <code>initWidget</code>.</p>
         */
        void initWidgets(IsWidget listEditorView);
    }
    
    HorizImageListEditor<DataSetProviderType> provider;
    public View view;


    @Inject
    public DataSetDefProviderTypeEditor(final HorizImageListEditor<DataSetProviderType> provider, final View view) {
        this.provider = provider;
        this.view = view;
    }

    @PostConstruct
    public void init() {
        view.init(this);
        
        // Initialize the acceptable values for DataSetProviderType.
        final Collection<ImageListEditor<DataSetProviderType>.Entry> entries = getDefaultEntries();
        provider.setEntries(entries);
        view.initWidgets(provider.view);
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    
    /*************************************************************
            ** GWT EDITOR CONTRACT METHODS **
     *************************************************************/

    @Override
    public void showErrors(final List<EditorError> errors) {
        // Defaults to no-operation. Errors are delegated to the ImageListEditor component.
    }

    @Override
    public HorizImageListEditor<DataSetProviderType> provider() {
        return provider;
    }

    protected Collection<ImageListEditor<DataSetProviderType>.Entry> getDefaultEntries() {
        final DataSetProviderType[] providerTypes = DataSetProviderType.values();
        final Collection<ImageListEditor<DataSetProviderType>.Entry> entries = new ArrayList<ImageListEditor<DataSetProviderType>.Entry>(providerTypes.length);
        for (final DataSetProviderType type : providerTypes) {
            if (isSupported(type)) {
                final String title = getTypeSelectorTitle(type);
                final String text = getTypeSelectorText(type);
                final SafeUri uri = getTypeSelectorImageUri(type);
                final ImageListEditor<DataSetProviderType>.Entry entry = provider.newEntry(type, uri,
                        new SafeHtmlBuilder().appendEscaped(title). toSafeHtml(),
                        new SafeHtmlBuilder().appendEscaped(text). toSafeHtml());
                entries.add(entry);
            }
        }
        return entries;
    }
    
    
    private boolean isSupported(final DataSetProviderType type) {
        switch (type) {
            case BEAN:
                return true;
            case CSV:
                return true;
            case SQL:
                return true;
            case ELASTICSEARCH:
                return true;
            default:
                return false;
        }
    }

    String getTypeSelectorTitle(final DataSetProviderType type) {
        String description = null;
        switch (type) {
            case BEAN:
                description = DataSetEditorConstants.INSTANCE.bean();
                break;
            case CSV:
                description = DataSetEditorConstants.INSTANCE.csv();
                break;
            case SQL:
                description = DataSetEditorConstants.INSTANCE.sql();
                break;
            case ELASTICSEARCH:
                description = DataSetEditorConstants.INSTANCE.elasticSearch();
                break;
        }
        return description;
    }

    String getTypeSelectorText(final DataSetProviderType type) {
        String description = null;
        switch (type) {
            case BEAN:
                description = DataSetEditorConstants.INSTANCE.bean_description();
                break;
            case CSV:
                description = DataSetEditorConstants.INSTANCE.csv_description();
                break;
            case SQL:
                description = DataSetEditorConstants.INSTANCE.sql_description();
                break;
            case ELASTICSEARCH:
                description = DataSetEditorConstants.INSTANCE.elasticSearch_description();
                break;
        }
        return description;
    }

    SafeUri getTypeSelectorImageUri(final DataSetProviderType type) {
        SafeUri result = null;
        switch (type) {
            case BEAN:
                result = DataSetClientResources.INSTANCE.images().javaIcon160().getSafeUri();
                break;
            case CSV:
                result = DataSetClientResources.INSTANCE.images().csvIcon160().getSafeUri();
                break;
            case SQL:
                result = DataSetClientResources.INSTANCE.images().sqlIcon160().getSafeUri();
                break;
            case ELASTICSEARCH:
                result = DataSetClientResources.INSTANCE.images().elIcon160().getSafeUri();
                break;
        }
        return result;
    }
}
