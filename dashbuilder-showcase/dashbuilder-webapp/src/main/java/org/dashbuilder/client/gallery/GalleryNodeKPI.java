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
package org.dashbuilder.client.gallery;

import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.kpi.KPIViewer;
import org.dashbuilder.kpi.KPI;

/**
 * A KPI gallery node.
 */
public class GalleryNodeKPI extends GalleryNode {

    protected KPI kpi;

    public GalleryNodeKPI(String name, KPI kpi) {
        super(name);
        this.kpi = kpi;
    }

    public KPI getKpi() {
        return kpi;
    }

    public void setKpi(KPI kpi) {
        this.kpi = kpi;
    }

    protected Widget createWidget() {
        KPIViewer viewer = new KPIViewer(kpi);
        viewer.draw();
        return viewer;
    }
}
