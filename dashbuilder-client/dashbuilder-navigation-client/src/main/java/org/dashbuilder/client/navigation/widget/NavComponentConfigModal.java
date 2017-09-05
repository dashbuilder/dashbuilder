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
package org.dashbuilder.client.navigation.widget;

import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.navigation.layout.editor.NavPointList;
import org.dashbuilder.navigation.NavDivider;
import org.dashbuilder.navigation.NavGroup;
import org.dashbuilder.navigation.NavItem;
import org.uberfire.client.mvp.UberView;
import org.uberfire.ext.layout.editor.api.editor.LayoutTemplate;
import org.uberfire.ext.plugin.client.perspective.editor.layout.editor.TargetDivList;
import org.uberfire.mvp.Command;

@Dependent
public class NavComponentConfigModal implements IsWidget {

    public interface View extends UberView<NavComponentConfigModal> {

        void clearNavGroupItems();

        void setIdentifierVisible(boolean visible);

        void setIdentifier(String id);

        void clearIdentifierErrors();

        void errorIdentifierAlreadyExists();

        void setMaxLevelsVisible(boolean visible);

        void setMaxLevels(int maxLevels);

        void clearMaxLevelsErrors();

        void errorMaxLevelsNotNumeric();

        void clearNavPointItems();

        void navPointItemsNotFound();

        void setNavPointSelection(String name, Command onReset);

        void addNavPoint(String name, Command onSelect);

        void setNavGroupEnabled(boolean enabled);

        void addNavGroupItem(String name, Command onSelect);

        void setNavGroupSelection(String name, Command onReset);

        void setNavGroupHelpText(String text);

        void setDefaultNavItemEnabled(boolean enabled);

        void setDefaultNavItemVisible(boolean enabled);

        void clearDefaultItems();

        void defaultItemsNotFound();

        void setDefaultItemSelection(String name, Command onReset);

        void addDefaultItem(String name, Command onSelect);

        void setTargetDivVisible(boolean enabled);

        void clearTargetDivItems();

        void targetDivsNotFound();

        void addTargetDivItem(String name, Command onSelect);

        void setTargetDivSelection(String name, Command onReset);

        void show();

        void hide();
    }

    View view;
    LayoutTemplate layoutTemplate = null;
    int maxLevels = -1;
    String navId = null;
    String navPoint = null;
    String groupId = null;
    NavGroup group = null;
    String defaultItemId = null;
    String targetDivId = null;
    NavPointList navPointList = new NavPointList();
    List<NavItem> navItemList = null;
    List<String> targetDivIdList = null;
    Command onOk = null;
    Command onCancel = null;

    @Inject
    public NavComponentConfigModal(View view, NavPointList navPointList) {
        this.view = view;
        this.view.init(this);
        this.navPointList = navPointList;
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public View getView() {
        return view;
    }

    public void setLayoutTemplate(LayoutTemplate layoutTemplate) {
        this.layoutTemplate = layoutTemplate;
        targetDivIdList = TargetDivList.list(layoutTemplate);
        navPointList.init(layoutTemplate);
    }

    public void setOnOk(Command onOk) {
        this.onOk = onOk;
    }

    public void setOnCancel(Command onCancel) {
        this.onCancel = onCancel;
    }

    public void setNavIdSupported(boolean supported) {
        view.setIdentifierVisible(supported);
    }

    public String getNavId() {
        return navId;
    }

    public void setNavId(String navId) {
        this.navId = navId;
        if (navId != null) {
            view.setIdentifier(navId);
        }
    }

    public String getNavPoint() {
        return navPoint;
    }

    public void setNavPoint(String navPoint) {
        if (navPointList.getNavPoints().contains(navPoint)) {
            this.navPoint = navPoint;
        }
    }

    public void setMaxLevelsSupported(boolean supported) {
        view.setMaxLevelsVisible(supported);
    }

    public void setMaxLevels(int maxLevels) {
        this.maxLevels = maxLevels;
    }

    public int getMaxLevels() {
        return maxLevels;
    }

    public void setNavGroupHelpHint(String text) {
        view.setNavGroupHelpText(text);
    }

    public String getGroupId() {
        return groupId;
    }

    public String getDefaultItemId() {
        return defaultItemId;
    }

    public String getTargetDivId() {
        return targetDivId;
    }

    public void setTargetDivSupported(boolean supported) {
        view.setTargetDivVisible(supported);
    }

    public void setTargetDiv(String targetDivId) {
        this.targetDivId = targetDivId;
    }

    public void setDefaultNavItemSupported(boolean supported) {
        view.setDefaultNavItemVisible(supported);
    }

    public void setDefaultNavItemId(String navItemId) {
        defaultItemId = navItemId;
    }

    public void setNavGroup(List<NavItem> navItemList, String selectedGroupId) {
        this.navItemList = navItemList;
        this.groupId = selectedGroupId;
        this.group = null;
    }

    private void updateNavPoints() {
        List<String> navPoints = navPointList.getNavPoints().stream()
                .filter(p -> navId == null || !navId.equals(p))
                .collect(Collectors.toList());

        view.clearNavPointItems();
        if (navPoints.isEmpty()) {
            view.navPointItemsNotFound();
        } else {
            for (String navPoint : navPoints) {
                if (navPoint.equals(this.navPoint)) {
                    view.setNavPointSelection(navPoint, () -> onNavPointSelected(null));
                } else {
                    view.addNavPoint(navPoint, () -> onNavPointSelected(navPoint));
                }
            }
        }
    }

    private void updateNavGroups() {
        group = null;
        view.clearNavGroupItems();
        view.setNavGroupEnabled(navPoint == null);
        if (navPoint == null && navItemList != null) {
            updateNavGroups(navItemList);
        }
    }

    private void updateNavGroups(List<NavItem> navItemList) {
        for (NavItem navItem : navItemList) {

            // Divider N/A
            if (navItem instanceof NavDivider) {
                continue;
            }
            // Only groups
            if (!(navItem instanceof NavGroup)) {
                continue;
            }
            // Check if the group is already selected
            String fullPath = calculateFullPath(navItem);
            if (groupId == null || (groupId != null && navItem.getId().equals(groupId))) {
                groupId = navItem.getId();
                group = (NavGroup) navItem;
                view.setNavGroupSelection(fullPath, () -> {});
            } else {
                view.addNavGroupItem(fullPath, () -> onGroupSelected(navItem.getId()));
            }
            // Add the children items
            updateNavGroups(((NavGroup) navItem).getChildren());
        }
    }

    private void updateDefaultItems() {
        view.clearDefaultItems();
        view.setDefaultNavItemEnabled(group != null);
        if (group == null || group.getChildren().isEmpty()) {
            view.defaultItemsNotFound();
        } else {
            NavGroup clone = (NavGroup) group.cloneItem();
            clone.setParent(null);
            updateDefaultItems(clone, 1);
        }
    }

    private void updateDefaultItems(NavGroup navGroup, int level) {
        if (maxLevels < 1 || level <= maxLevels) {
            for (NavItem navItem : navGroup.getChildren()) {

                // Divider N/A
                if (navItem instanceof NavDivider) {
                    continue;
                }
                // Add the default item
                String fullPath = calculateFullPath(navItem);
                if (defaultItemId != null && navItem.getId().equals(defaultItemId)) {
                    view.setDefaultItemSelection(fullPath, () -> onDefaultItemSelected(null));
                } else {
                    view.addDefaultItem(fullPath, () -> onDefaultItemSelected(navItem.getId()));
                }
                // Append children
                if (navItem instanceof NavGroup) {
                    updateDefaultItems((NavGroup) navItem, level+1);
                }
            }
        }
    }

    private void updateTargetDivs() {
        view.clearTargetDivItems();

        if (targetDivIdList == null || targetDivIdList.isEmpty()) {
            view.targetDivsNotFound();
        } else {
            for (String divId : targetDivIdList) {
                if (targetDivId != null && divId.equals(targetDivId)) {
                    view.setTargetDivSelection(divId, () -> onTargetDivSelected(null));
                } else {
                    view.addTargetDivItem(divId, () -> onTargetDivSelected(divId));
                }
            }
        }
    }

    public String calculateFullPath(NavItem navItem) {
        StringBuilder out = new StringBuilder();
        NavItem parent = navItem.getParent();
        while (parent != null) {
            out.insert(0, parent.getName() + ">");
            parent = parent.getParent();
        }
        out.append(navItem.getName());
        return out.toString();
    }

    public void clear() {
        navPoint = null;
        groupId = null;
        group = null;
        defaultItemId = null;
        targetDivId = null;
        navPointList.clear();
        navItemList = null;
        targetDivIdList = null;
        view.clearMaxLevelsErrors();
        view.clearIdentifierErrors();
        view.clearNavPointItems();
        view.clearNavGroupItems();
        view.clearDefaultItems();
        view.clearTargetDivItems();
    }

    public void show() {
        updateNavPoints();
        updateNavGroups();
        updateDefaultItems();
        updateTargetDivs();
        view.setIdentifier(navId == null ? "" : navId);
        view.setMaxLevels(maxLevels);
        view.show();
    }

    // View callbacks

    public void onNavPointSelected(String id) {
        navPoint = id;
        groupId = null;
        defaultItemId = null;
        updateNavPoints();
        updateNavGroups();
        updateDefaultItems();
    }

    public void onGroupSelected(String id) {
        groupId = id;
        defaultItemId = null;
        updateNavGroups();
        updateDefaultItems();
    }

    public void onDefaultItemSelected(String id) {
        defaultItemId = id;
        updateDefaultItems();
    }

    public void onTargetDivSelected(String id) {
        targetDivId = id;
        updateTargetDivs();
    }

    public void onIdChanged(String id) {
        view.clearIdentifierErrors();
        if (navPointList.getNavPoints().contains(id.trim())) {
            view.errorIdentifierAlreadyExists();
        } else {
            navId = id.trim().length() == 0 ? null : id.trim();
        }
    }

    public void onMaxLevelsChanged(String value) {
        view.clearMaxLevelsErrors();
        try {
            int newMax = Integer.parseInt(value);
            if (newMax != maxLevels) {
                maxLevels = newMax;
                updateDefaultItems();
            }
        } catch (Exception e) {
            view.errorMaxLevelsNotNumeric();
        }
    }

    public void onOk() {
        if (navPoint != null || groupId != null) {
            view.hide();
            if (onOk != null) {
                onOk.execute();
            }
        }
    }

    public void onCancel() {
        view.hide();
        if (onCancel != null) {
            onCancel.execute();
        }
    }
}
