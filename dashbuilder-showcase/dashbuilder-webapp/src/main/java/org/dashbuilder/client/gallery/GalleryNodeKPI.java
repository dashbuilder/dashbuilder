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

import java.util.Collections;
import java.util.List;

import org.dashbuilder.model.kpi.KPI;

/**
 * A KPI gallery node.
 */
public class GalleryNodeKPI implements GalleryNode {

    protected String name;
    protected KPI kpi;

    public GalleryNodeKPI(String name, KPI kpi) {
        this.name = name;
        this.kpi = kpi;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public KPI getKpi() {
        return kpi;
    }

    public void setKpi(KPI kpi) {
        this.kpi = kpi;
    }

    public List<GalleryNode> getChildren() {
        return Collections.emptyList();
    }
}
