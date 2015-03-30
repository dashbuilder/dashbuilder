/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dashbuilder.common.client.resources.bundles;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * GWT managed images for common client components.
 */
public interface DashbuilderCommonImages extends ClientBundle {

    @Source("images/lessh.png")
    ImageResource lessh();

    @Source("images/lessv.png")
    ImageResource lessv();
    
    @Source("images/moreh.png")
    ImageResource moreh();

    @Source("images/morev.png")
    ImageResource morev();

    @Source("images/scaleh.png")
    DataResource scaleh();

    @Source("images/scalev.png")
    DataResource scalev();

    @Source("images/dragh.png")
    ImageResource dragh();

    @Source("images/dragv.png")
    ImageResource dragv();

}
