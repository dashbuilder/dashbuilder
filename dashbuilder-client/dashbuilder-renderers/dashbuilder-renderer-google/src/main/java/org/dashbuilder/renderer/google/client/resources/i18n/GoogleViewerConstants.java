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

package org.dashbuilder.renderer.google.client.resources.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface GoogleViewerConstants extends Messages {

    public static final GoogleViewerConstants INSTANCE = GWT.create( GoogleViewerConstants.class );

    public String googleViewer_resetAnchor();

    public String googleTableViewer_gotoFirstPage();

    public String googleTableViewer_gotoPreviousPage();

    public String googleTableViewer_gotoNextPage();

    public String googleTableViewer_gotoLastPage();

    public String googleTableViewer_pages( String leftMostPageNumber, String rightMostPageNumber, String totalPages);

    public String googleTableViewer_rows( String from, String to, String totalRows);

    public String googleTableViewer_noData();

}
