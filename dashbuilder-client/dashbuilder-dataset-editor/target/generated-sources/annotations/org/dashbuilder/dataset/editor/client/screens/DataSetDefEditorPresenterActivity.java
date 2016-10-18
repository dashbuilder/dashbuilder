/*
 * Copyright 2012 JBoss Inc
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

package org.dashbuilder.dataset.editor.client.screens;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Generated;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import javax.inject.Named;
import org.uberfire.client.workbench.annotations.AssociatedResources;
import org.uberfire.client.workbench.annotations.Priority;
import org.uberfire.client.mvp.AbstractWorkbenchEditorActivity;
import org.uberfire.client.mvp.PlaceManager;

import org.uberfire.mvp.PlaceRequest;

import org.uberfire.workbench.model.menu.Menus;

import org.uberfire.backend.vfs.ObservablePath;

import com.google.gwt.user.client.ui.IsWidget;

@Dependent
@Generated("org.uberfire.annotations.processors.WorkbenchEditorProcessor")
@Named("DataSetDefEditor")
@AssociatedResources({
    org.dashbuilder.dataset.editor.client.screens.DataSetDefType.class
})

@Priority(2147483647)
/*
 * WARNING! This class is generated. Do not modify.
 */
public class DataSetDefEditorPresenterActivity extends AbstractWorkbenchEditorActivity {

    private static final Collection<String> ROLES = Collections.emptyList();

    private static final Collection<String> TRAITS = Collections.emptyList();

    @Inject
    private DataSetDefEditorPresenter realPresenter;

    @Inject
    //Constructor injection for testing
    public DataSetDefEditorPresenterActivity(final PlaceManager placeManager) {
        super( placeManager );
    }

    @Override
    public void onStartup(final ObservablePath path,
                        final PlaceRequest place) {
        super.onStartup( path, place );
        realPresenter.onStartup( path, place );
    }

    @Override
    public boolean onMayClose() {
        return realPresenter.onMayClose();
    }

    @Override
    public IsWidget getTitleDecoration() {
        return realPresenter.getTitle();
    }

    @Override
    public String getTitle() {
        return realPresenter.getTitleText();
    }

    @Override
    public IsWidget getWidget() {
        return realPresenter.getWidget();
    }
    
    @Override
    public Menus getMenus() {
        return realPresenter.getMenus();
    }
    
    @Override
    public Collection<String> getRoles() {
        return ROLES;
    }

    @Override
    public Collection<String> getTraits() {
        return TRAITS;
    }

    @Override
    public String getSignatureId() {
        return "org.dashbuilder.dataset.editor.client.screens.DataSetDefEditorPresenterActivity";
    }
}
