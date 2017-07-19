/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.client.navigation.layout.editor;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;

import org.uberfire.ext.layout.editor.api.editor.LayoutColumn;
import org.uberfire.ext.layout.editor.api.editor.LayoutComponent;
import org.uberfire.ext.layout.editor.api.editor.LayoutRow;
import org.uberfire.ext.layout.editor.api.editor.LayoutTemplate;

@Dependent
public class NavPointList {

    private List<String> navPoints = new ArrayList<>();

    public void init(LayoutTemplate layout) {
        navPoints.clear();
        List<LayoutRow> rows = layout.getRows();
        searchForComponents(rows);
    }

    public List<String> getNavPoints() {
        return navPoints;
    }

    public void clear() {
        navPoints.clear();
    }

    private void searchForComponents(List<LayoutRow> rows) {
        for (LayoutRow layoutRow : rows) {
            for (LayoutColumn layoutColumn : layoutRow.getLayoutColumns()) {
                if (columnHasNestedRows(layoutColumn)) {
                    searchForComponents(layoutColumn.getRows());
                } else {
                    extractNavPoint(layoutColumn.getLayoutComponents());
                }
            }
        }
    }

    private void extractNavPoint(List<LayoutComponent> layoutComponents) {
        for (LayoutComponent layoutComponent : layoutComponents) {
            if (isANavComponent(layoutComponent)) {
                String navId = layoutComponent.getProperties().get(AbstractNavDragComponent.NAV_ID);
                if (navId != null && navId.trim().length() > 0) {
                    navPoints.add(navId);
                }
            }
        }
    }

    private boolean isANavComponent(LayoutComponent layoutComponent) {
        return layoutComponent.getDragTypeName().equalsIgnoreCase(NavMenuBarDragComponent.class.getName()) ||
        layoutComponent.getDragTypeName().equalsIgnoreCase(NavTilesDragComponent.class.getName());
    }

    private boolean columnHasNestedRows(LayoutColumn layoutColumn) {
        return layoutColumn.getRows() != null && !layoutColumn.getRows().isEmpty();
    }
}
