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

import java.util.Collection;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.displayer.DisplayerSettings;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;

/**
 * The locator service for DisplayerSettingsEditor implementations.
 */
@ApplicationScoped
public class DisplayerEditorLocator {

    public static DisplayerEditorLocator get() {
        Collection<IOCBeanDef<DisplayerEditorLocator>> beans = IOC.getBeanManager().lookupBeans(DisplayerEditorLocator.class);
        IOCBeanDef<DisplayerEditorLocator> beanDef = beans.iterator().next();
        return beanDef.getInstance();
    }

    @Inject SyncBeanManager beanManager;

    /**
     * Get the editor component for the specified data displayer
     */
    public <T extends DisplayerSettings> DisplayerSettingsEditor<T> lookupEditor(T displayerSettings) {

        String displayerType = displayerSettings.getType().toString().toLowerCase();
        String beanName =  displayerType + "_editor";
        Collection<IOCBeanDef> beans = beanManager.lookupBeans(beanName);
        if (beans == null || beans.isEmpty()) throw new RuntimeException(displayerSettings.getType().toString().toLowerCase() + " editor not found.");
        if (beans.size() > 1) throw new RuntimeException("Multiple editor implementations found for: " + displayerType);

        IOCBeanDef beanDef = beans.iterator().next();
        DisplayerSettingsEditor<T> editor = (DisplayerSettingsEditor<T> ) beanDef.getInstance();
        editor.setDisplayerSettings(displayerSettings);
        return editor;
    }
}