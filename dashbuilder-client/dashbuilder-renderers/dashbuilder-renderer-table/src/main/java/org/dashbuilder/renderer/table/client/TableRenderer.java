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
package org.dashbuilder.renderer.table.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.AbstractRendererLibrary;
import org.dashbuilder.displayer.client.Displayer;

/**
 * Table renderer based on the core PagedTable widget.
 */
@ApplicationScoped
@Named(TableRenderer.UUID + "_renderer")
public class TableRenderer extends AbstractRendererLibrary {

    public static final String UUID = "table";

    @Override
    public String getUUID() {
        return UUID;
    }

    @Override
    public Displayer lookupDisplayer(DisplayerSettings displayerSettings) {
        DisplayerType type = displayerSettings.getType();
        if ( DisplayerType.TABLE.equals(type)) return new TableDisplayer();

        return null;
    }
}
