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
package org.dashbuilder.client.kpi;

import java.util.List;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.dashbuilder.model.kpi.*;

@ApplicationScoped
public class KPILocator {

    private List<KPI> kpiList = new ArrayList<KPI>();

    public List<KPI> getAllKPIs() {
        return kpiList;
    }

    public void addKPI(KPI kpi) {
        kpiList.add(kpi);
    }

    public KPI getKPI(String uid) {
        for (KPI kpi : kpiList) {
            if (kpi.getUUID().equals(uid)){
                return kpi;
            }
        }
        return null;
    }
}
