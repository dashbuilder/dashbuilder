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
package org.dashbuilder.renderer.uftable.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.dashbuilder.displayer.client.AbstractRendererLibrary;
import org.dashbuilder.displayer.client.DataViewer;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DataDisplayerType;

/**
 * Table renderer based on the Uberfire's core PagedTable widget.
 */
@ApplicationScoped
@Named(UFTableRenderer.UUID + "_renderer")
public class UFTableRenderer extends AbstractRendererLibrary {

    public static final String UUID = "uftable";

    @Override public String getUUID() {
        return UUID;
    }

    @Override public DataViewer lookupViewer(DisplayerSettings displayerSettings) {
        DataDisplayerType type = displayerSettings.getType();
        if (DataDisplayerType.TABLE.equals(type)) return new UFTableViewer();

        return null;
    }
}
