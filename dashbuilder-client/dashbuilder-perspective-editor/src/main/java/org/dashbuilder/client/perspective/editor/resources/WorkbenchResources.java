package org.dashbuilder.client.perspective.editor.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;

public interface WorkbenchResources
        extends
        ClientBundle {

    WorkbenchResources INSTANCE = GWT.create(WorkbenchResources.class);

    @Source("css/workbench.css") WorkbenchCss CSS();
}
