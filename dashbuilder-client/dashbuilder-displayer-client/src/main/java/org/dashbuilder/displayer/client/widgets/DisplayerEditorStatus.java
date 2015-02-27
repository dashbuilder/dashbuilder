/**
 * Copyright (C) 2015 JBoss Inc
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
package org.dashbuilder.displayer.client.widgets;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;

@ApplicationScoped
public class DisplayerEditorStatus {

    public static DisplayerEditorStatus get() {
        Collection<IOCBeanDef<DisplayerEditorStatus>> beans = IOC.getBeanManager().lookupBeans(DisplayerEditorStatus.class);
        IOCBeanDef<DisplayerEditorStatus> beanDef = beans.iterator().next();
        return beanDef.getInstance();
    }

    Map<String,DisplayerStatus> displayerStatusMap = new HashMap<String,DisplayerStatus>();

    public int getSelectedTab(String displayerUuid) {
        DisplayerStatus status = displayerStatusMap.get(displayerUuid);
        return (status == null ? -1 : status.selectedTab);
    }

    public void saveSelectedTab(String displayerUuid, int tab) {
        DisplayerStatus status = fetch(displayerUuid);
        status.selectedTab = tab;
    }

    private DisplayerStatus fetch(String displayerUuid) {
        DisplayerStatus status = displayerStatusMap.get(displayerUuid);
        if (status != null) return status;
        displayerStatusMap.put(displayerUuid, status = new DisplayerStatus());
        return status;
    }

    private class DisplayerStatus {

        int selectedTab = -1;
    }
}
