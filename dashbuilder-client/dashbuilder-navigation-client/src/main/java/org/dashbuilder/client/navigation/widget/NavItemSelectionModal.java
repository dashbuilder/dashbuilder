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
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.navigation.NavDivider;
import org.dashbuilder.navigation.NavGroup;
import org.dashbuilder.navigation.NavItem;
import org.uberfire.client.mvp.UberView;

@Dependent
public class NavItemSelectionModal implements IsWidget {

    public interface View extends UberView<NavItemSelectionModal> {

        void clearItems();

        void addItem(String name, Command onSelect);

        void setCurrentSelection(String name);

        void setHelpText(String text);

        void show();

        void hide();
    }

    View view;
    NavItem selectedItem = null;
    boolean onlyGroups = true;
    List<NavItem> navItemList = null;
    Command onOk = null;
    Command onCancel = null;

    @Inject
    public NavItemSelectionModal(View view) {
        this.view = view;
        this.view.init(this);
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public View getView() {
        return view;
    }

    public void setOnlyGroups(boolean onlyGroups) {
        this.onlyGroups = onlyGroups;
    }

    public void setOnOk(Command onOk) {
        this.onOk = onOk;
    }

    public void setOnCancel(Command onCancel) {
        this.onCancel = onCancel;
    }

    public void setHelpHint(String text) {
        view.setHelpText(text);
    }

    public NavItem getSelectedItem() {
        return selectedItem;
    }

    public void show(List<NavItem> navItemList, String selectedItemId) {
        view.clearItems();
        this.navItemList = navItemList;
        addItems(navItemList, selectedItemId);
        view.show();
    }

    private void addItems(List<NavItem> navItemList, String selectedItemId) {
        for (NavItem navItem : navItemList) {

            // Divider N/A
            if (navItem instanceof NavDivider) {
                continue;
            }
            // Only groups option
            if (onlyGroups && !(navItem instanceof NavGroup)) {
                continue;
            }
            // Check if the item is already selected
            String fullPath = calculateFullPath(navItem);
            if (selectedItemId != null && navItem.getId().equals(selectedItemId)) {
                selectedItem = navItem;
                view.setCurrentSelection(fullPath);
            } else {
                view.addItem(fullPath, () -> onItemSelected(navItem));
            }
            // Add the children items
            if (navItem instanceof NavGroup) {
                addItems(((NavGroup) navItem).getChildren(), selectedItemId);
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

    // View callbacks

    public void onItemSelected(NavItem navItem) {
        // Re-paint the whole selector to reflect the new selection status
        show(navItemList, navItem.getId());
    }

    void onOk() {
        view.hide();
        if (onOk != null) {
            onOk.execute();
        }
    }

    void onCancel() {
        view.hide();
        if (onCancel != null) {
            onCancel.execute();
        }
    }
}
