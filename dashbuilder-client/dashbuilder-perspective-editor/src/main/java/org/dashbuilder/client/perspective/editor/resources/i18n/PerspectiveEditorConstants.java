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
package org.dashbuilder.client.perspective.editor.resources.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.ConstantsWithLookup;

/**
 * <p>Perspective editor related widget's constants.</p>
 *
 * @since 0.3.0 
 */
public interface PerspectiveEditorConstants extends ConstantsWithLookup {

    public static final PerspectiveEditorConstants INSTANCE = GWT.create( PerspectiveEditorConstants.class );

    String addMenuItem();
    String enableEditMenu();
    String disableEditMenu();
    String newMenuItem();
    String name();
    String name_placeholder();
    String perspective();
    String perspective_placeholder();
    String ok();
    String create();
    String enableEdit();
    String disableEdit();
}
