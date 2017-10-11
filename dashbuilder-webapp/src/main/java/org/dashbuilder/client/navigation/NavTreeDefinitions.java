/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.client.navigation;

import org.dashbuilder.navigation.NavTree;
import org.dashbuilder.navigation.impl.NavTreeBuilder;

import static org.dashbuilder.navigation.workbench.NavWorkbenchCtx.*;
import static org.dashbuilder.perspectives.PerspectiveIds.*;

/**
 * Navigation tree definitions such as the top menu bar
 */
public interface NavTreeDefinitions {

    String GROUP_APP = "app";
    String ENTRY_HOME = "app_home";
    String ENTRY_GALLERY = "app_gallery";
    String GROUP_ADMIN = "app_admin";
    String ENTRY_DATASETS = "app_datasets";
    String ENTRY_SECURITY = "app_security";
    String ENTRY_CONTENT_MGR = "app_contentMgr";
    String GROUP_DASHBOARDS = "app_dashboards";
    String ENTRY_SALES_DASHBOARD = "app_salesDashboard";
    String ENTRY_SALES_REPORTS = "app_salesReports";

    NavTree NAV_TREE_DEFAULT = new NavTreeBuilder()
            .group(GROUP_APP, "Default (Top Menu)", "The items displayed by the application's top menu bar", false)
                .item(ENTRY_HOME, "Home", "The home page", true, perspective(HOME))
                .item(ENTRY_GALLERY, "Gallery", "The displayer gallery", true, perspective(GALLERY))
                .group(GROUP_ADMIN, "Administration", "The administration tools", false)
                    .item(ENTRY_DATASETS, "Datasets", "The dataset authoring tool", false, perspective(DATA_SETS))
                    .item(ENTRY_SECURITY, "Security", "The security configuration tool", false, perspective(SECURITY))
                    .item(ENTRY_CONTENT_MGR, "Content manager", "The content manager tool", false, perspective(CONTENT_MANAGER))
                    .endGroup()
                .group(GROUP_DASHBOARDS, "Dashboards", "The default location for dashboard items", false)
                    .item(ENTRY_SALES_DASHBOARD, "Sales dashboard", null, true, perspective(SALES_DASHBOARD))
                    .item(ENTRY_SALES_REPORTS, "Sales reports", null, true, perspective(SALES_REPORTS))
                    .endGroup()
            .endGroup()
        .build();
}
