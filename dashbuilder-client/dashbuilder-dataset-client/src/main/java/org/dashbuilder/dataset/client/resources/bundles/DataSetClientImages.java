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

package org.dashbuilder.dataset.client.resources.bundles;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * GWT managed images for Data Set client components.
 */
public interface DataSetClientImages extends ClientBundle {

    @Source("images/csv_icon.png")
    DataResource csvIcon();

    @Source("images/java_icon.png")
    DataResource javaIcon();

    @Source("images/sql_icon.png")
    DataResource sqlIcon();

    @Source("images/el_icon.png")
    DataResource elIcon();

    @Source("images/excel_icon.png")
    DataResource excelIcon();

    @Source("images/label_icon_16.png")
    DataResource labelIcon16();

    @Source("images/label_icon_32.png")
    DataResource labelIcon32();

    @Source("images/number_icon_16.png")
    DataResource numberIcon16();

    @Source("images/number_icon_32.png")
    DataResource numberIcon32();

    @Source("images/text_icon_16.png")
    DataResource textIcon16();

    @Source("images/text_icon_32.png")
    DataResource textIcon32();

    @Source("images/date_icon_16.png")
    DataResource dateIcon16();

    @Source("images/date_icon_16.png")
    DataResource dateIcon32();

    @Source("images/ok_icon_small.gif")
    ImageResource okIconSmall();

    @Source("images/cancel_icon_small.gif")
    ImageResource cancelIconSmall();

    @Source("images/loading_icon.gif")
    DataResource loadingIcon();
}
