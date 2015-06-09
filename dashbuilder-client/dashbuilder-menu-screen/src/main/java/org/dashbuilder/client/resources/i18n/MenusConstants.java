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
package org.dashbuilder.client.resources.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.ConstantsWithLookup;

/**
 * <p>Editable menu widget's constants.</p>
 *
 * @since 0.3.0 Editable menu
 */
public interface MenusConstants extends ConstantsWithLookup {

    public static final MenusConstants INSTANCE = GWT.create( MenusConstants.class );

    String addMenuItem();
    String removeMenuItem();
    String newMenuItem();
    String name();
    String name_placeholder();
    String perspective();
    String perspective_placeholder();
    String ok();
    String create();
    String editableWorkbenchMenuBar();
}
