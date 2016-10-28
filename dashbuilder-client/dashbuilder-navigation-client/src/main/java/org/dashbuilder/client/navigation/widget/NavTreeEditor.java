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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.navigation.resources.i18n.NavigationConstants;
import org.dashbuilder.navigation.NavGroup;
import org.dashbuilder.navigation.NavItem;
import org.dashbuilder.navigation.NavTree;
import org.dashbuilder.navigation.workbench.NavWorkbenchCtx;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.uberfire.client.authz.PerspectiveTreeProvider;
import org.uberfire.client.mvp.PerspectiveActivity;
import org.uberfire.client.mvp.UberView;
import org.uberfire.ext.plugin.client.perspective.editor.generator.PerspectiveEditorActivity;
import org.uberfire.mvp.Command;

@Dependent
public class NavTreeEditor implements IsWidget {

    public interface View extends UberView<NavTreeEditor> {

        void clear();

        void setChangedFlag(boolean on);

        void goOneLevelDown();

        void goOneLevelUp();

        void addItemEditor(NavItemEditor navItemEditor);

        String generateId();
    }

    public static final NavigationConstants i18n = NavigationConstants.INSTANCE;

    View view;
    SyncBeanManager beanManager;
    PerspectiveTreeProvider perspectiveTreeProvider;
    NavTree navTree;
    NavTree navTreeBackup;
    boolean newDividerEnabled = true;
    boolean newGroupEnabled = true;
    boolean newPerspectiveEnabled = true;
    boolean gotoPerspectiveEnabled = true;
    boolean onlyRuntimePerspectives = false;
    int maxLevels = -1;
    Command onChangeCommand;
    String inCreationId;
    String literalGroup = "Group";
    String literalPerspective = "Perspective";
    String literalDivider = "Divider";

    @Inject
    public NavTreeEditor(View view, SyncBeanManager beanManager, PerspectiveTreeProvider perspectiveTreeProvider) {
        this.view = view;
        this.beanManager = beanManager;
        this.perspectiveTreeProvider = perspectiveTreeProvider;
        this.view.init(this);
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public NavTree getNavTree() {
        return navTree;
    }

    public void setLiteralGroup(String literalGroup) {
        this.literalGroup = literalGroup;
    }

    public void setLiteralPerspective(String literalPerspective) {
        this.literalPerspective = literalPerspective;
    }

    public void setLiteralDivider(String literalDivider) {
        this.literalDivider = literalDivider;
    }

    public boolean isNewDividerEnabled() {
        return newDividerEnabled;
    }

    public void setNewDividerEnabled(boolean newDividerEnabled) {
        this.newDividerEnabled = newDividerEnabled;
    }

    public boolean isNewGroupEnabled() {
        return newGroupEnabled;
    }

    public void setNewGroupEnabled(boolean newGroupEnabled) {
        this.newGroupEnabled = newGroupEnabled;
    }

    public boolean isNewPerspectiveEnabled() {
        return newPerspectiveEnabled;
    }

    public void setNewPerspectiveEnabled(boolean newPerspectiveEnabled) {
        this.newPerspectiveEnabled = newPerspectiveEnabled;
    }

    public boolean isGotoPerspectiveEnabled() {
        return gotoPerspectiveEnabled;
    }

    public void setGotoPerspectiveEnabled(boolean gotoPerspectiveEnabled) {
        this.gotoPerspectiveEnabled = gotoPerspectiveEnabled;
    }

    public int getMaxLevels() {
        return maxLevels;
    }

    public void setMaxLevels(int maxLevels) {
        this.maxLevels = maxLevels;
    }

    public boolean isOnlyRuntimePerspectives() {
        return onlyRuntimePerspectives;
    }

    public void setOnlyRuntimePerspectives(boolean onlyRuntimePerspectives) {
        this.onlyRuntimePerspectives = onlyRuntimePerspectives;
    }

    public void setOnChangeCommand(Command onChangeCommand) {
        this.onChangeCommand = onChangeCommand;
    }

    public void edit(NavTree navTree) {
        this.navTree = navTree.cloneTree();
        this.navTreeBackup = navTree.cloneTree();
        this.inCreationId = null;
        _edit(this.navTree);
    }

    private void _edit(NavTree navTree) {
        view.clear();
        _edit(navTree.getRootItems(), maxLevels);
    }

    private void _edit(List<NavItem> navItems, int max) {
        for (int i=0; i<navItems.size(); i++) {
            NavItem navItem = navItems.get(i);
            NavItemEditor navItemEditor = createNavItemEditor(navItem, i==0, i==navItems.size()-1, max <=0 || max > 0, max <=0 || max > 1);
            view.addItemEditor(navItemEditor);

            // Recursively edit the tree subgroups
            if (navItem instanceof NavGroup) {
                NavGroup subGroup = (NavGroup) navItem;
                view.goOneLevelDown();
                _edit(subGroup.getChildren(), max > 0 ? max-1 : -1);
                view.goOneLevelUp();
            }
        }
    }

    public NavItemEditor createNavItemEditor(NavItem navItem, boolean isFirst, boolean isLast, boolean childrenAllowed, boolean subGroupsAllowed) {
        NavItemEditor navItemEditor = beanManager.lookupBean(NavItemEditor.class).newInstance();
        navItemEditor.setLiteralGroup(literalGroup);
        navItemEditor.setLiteralPerspective(literalPerspective);
        navItemEditor.setLiteralDivider(literalDivider);
        navItemEditor.setOnUpdateCommand(() -> onChangeItem(navItem, navItemEditor.getNavItem()));
        navItemEditor.setOnErrorCommand(() -> onItemError(navItem, navItemEditor.getNavItem()));
        navItemEditor.setOnDeleteCommand(() -> onDeleteItem(navItem));
        navItemEditor.setOnMoveFirstCommand(() -> onMoveFirstItem(navItem));
        navItemEditor.setOnMoveLastCommand(() -> onMoveLastItem(navItem));
        navItemEditor.setOnMoveUpCommand(() -> onMoveUpItem(navItem));
        navItemEditor.setOnMoveDownCommand(() -> onMoveDownItem(navItem));
        navItemEditor.setOnNewSubgroupCommand(() -> onNewSubGroup((NavGroup) navItem));
        navItemEditor.setOnNewPerspectiveCommand(() -> onNewPerspective((NavGroup) navItem));
        navItemEditor.setOnNewDividerCommand(() -> onNewDivider((NavGroup) navItem));
        navItemEditor.setMoveUpEnabled(!isFirst);
        navItemEditor.setMoveDownEnabled(!isLast);
        navItemEditor.setNewGroupEnabled(newGroupEnabled && subGroupsAllowed);
        navItemEditor.setNewDividerEnabled(newDividerEnabled && childrenAllowed);
        navItemEditor.setNewPerspectiveEnabled(newPerspectiveEnabled && childrenAllowed);
        navItemEditor.setGotoPerspectiveEnabled(gotoPerspectiveEnabled);
        navItemEditor.setVisiblePerspectiveIds(getPerspectiveIds(true));
        navItemEditor.setHiddenPerspectiveIds(getPerspectiveIds(false));
        navItemEditor.setPerspectiveNameProvider(perspectiveTreeProvider::getPerspectiveName);
        navItemEditor.edit(navItem);
        if (inCreationId != null && inCreationId.equals(navItem.getId())) {
            navItemEditor.onItemClick();
        }
        return navItemEditor;
    }

    public Set<String> getPerspectiveIds(boolean visible) {
        Set<String> result = visible ? new HashSet<>() : new HashSet<>(perspectiveTreeProvider.getPerspectiveIdsExcluded());

        for (SyncBeanDef<PerspectiveActivity> beanDef : beanManager.lookupBeans(PerspectiveActivity.class)) {
            PerspectiveActivity p = beanDef.getInstance();
            try {
                String id = p.getIdentifier();
                boolean runtime = p instanceof PerspectiveEditorActivity;

                if (visible && !perspectiveTreeProvider.getPerspectiveIdsExcluded().contains(id)) {
                    if (!onlyRuntimePerspectives || (onlyRuntimePerspectives && runtime)) {
                        result.add(p.getIdentifier());
                    }
                }
                if (!visible && onlyRuntimePerspectives && !runtime) {
                    result.add(p.getIdentifier());
                }
            } finally {
                beanManager.destroyBean(p);
            }
        }
        return result;
    }

    private void onNewSubGroup(NavGroup navGroup) {
        inCreationId = "group_" + view.generateId();
        String name = i18n.newItemName(literalGroup);
        navTree.addGroup(inCreationId, name, null, navGroup.getId(), true);
        showChanges();
    }

    private void onNewPerspective(NavGroup navGroup) {
        inCreationId = "perspective_" + view.generateId();
        String name = i18n.newItemName(literalPerspective);
        navTree.addItem(inCreationId, name, null, navGroup.getId(), true, NavWorkbenchCtx.perspective("HomePerspective").toString());
        showChanges();
    }

    private void onNewDivider(NavGroup navGroup) {
        navTree.addDivider(navGroup.getId(), true);
        showChanges();
    }

    private void onItemError(NavItem oldItem, NavItem newItem) {
        view.setChangedFlag(false);
    }

    private void onChangeItem(NavItem oldItem, NavItem newItem) {
        navTree.setItemName(oldItem.getId(), newItem.getName());
        navTree.setItemDescription(oldItem.getId(), newItem.getDescription());
        navTree.setItemContext(oldItem.getId(), newItem.getContext());
        view.setChangedFlag(true);
    }

    private void onDeleteItem(NavItem navItem) {
        navTree.deleteItem(navItem.getId());
        showChanges();
    }

    private void onMoveUpItem(NavItem navItem) {
        navTree.moveItemUp(navItem.getId());
        showChanges();
    }

    private void onMoveDownItem(NavItem navItem) {
        navTree.moveItemDown(navItem.getId());
        showChanges();
    }

    private void onMoveFirstItem(NavItem navItem) {
        navTree.moveItemFirst(navItem.getId());
        showChanges();
    }

    private void onMoveLastItem(NavItem navItem) {
        navTree.moveItemLast(navItem.getId());
        showChanges();
    }

    private void showChanges() {
        _edit(navTree);
        view.setChangedFlag(true);
    }

    void onSaveClicked() {
        edit(navTree);
        if (onChangeCommand != null) {
            onChangeCommand.execute();
        }
    }

    void onCancelClicked() {
        edit(navTreeBackup);
    }
}
