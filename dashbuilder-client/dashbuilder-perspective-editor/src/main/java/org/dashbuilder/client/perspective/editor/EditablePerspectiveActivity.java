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
package org.dashbuilder.client.perspective.editor;

import java.util.Collection;
import java.util.Collections;

import org.dashbuilder.client.workbench.panels.impl.MultiListWorkbenchPanelPresenterExt;
import org.uberfire.client.mvp.PerspectiveActivity;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;
import org.uberfire.workbench.model.menu.Menus;
import org.uberfire.workbench.model.toolbar.ToolBar;

/**
 * An editable perspective.
 */
public class EditablePerspectiveActivity implements PerspectiveActivity {

    // TODO: Replace by a 'type' property on PerspectiveDefinition
    public static final String CLASSIFIER = "editable-";

    protected PlaceRequest place;
    protected String id;
    protected boolean persistent;

    public EditablePerspectiveActivity() {
    }

    public EditablePerspectiveActivity(String id) {
        this.id = id;
        this.persistent = true;
    }

    public String getDisplayName() {
        return id.substring(CLASSIFIER.length());
    }

    @Override
    public PlaceRequest getPlace() {
        return place;
    }

    @Override
    public void onStartup(final PlaceRequest place) {
        this.place = place;
    }

    @Override
    public void onOpen() {
    }

    @Override
    public void onClose() {
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public PerspectiveDefinition getDefaultPerspectiveLayout() {
        PerspectiveDefinition perspective = new PerspectiveDefinitionImpl(MultiListWorkbenchPanelPresenterExt.class.getName());
        perspective.setName(id);
        return perspective;
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    @Override
    public boolean isTransient() {
        return !persistent;
    }

    @Override
    public Menus getMenus() {
        return null;
    }

    @Override
    public ToolBar getToolBar() {
        return null;
    }

    @Override
    public String getSignatureId() {
        return id;
    }

    @Override
    public Collection<String> getRoles() {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> getTraits() {
        return Collections.emptyList();
    }

    // Internal stuff

    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

}
