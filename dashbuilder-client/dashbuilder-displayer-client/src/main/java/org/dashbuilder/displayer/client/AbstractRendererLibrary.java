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
package org.dashbuilder.displayer.client;

import java.util.List;

/**
 * Base class for implementing custom renderer libraries.
 */
public abstract class AbstractRendererLibrary implements RendererLibrary {

    public void draw(List<DataViewer> viewerList) {
        for (DataViewer dataViewer : viewerList) {
            dataViewer.draw();
        }
    }

    public void redraw(List<DataViewer> viewerList) {
        for (DataViewer dataViewer : viewerList) {
            dataViewer.redraw();
        }
    }
}
