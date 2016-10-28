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

import java.util.Set;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.navigation.plugin.PerspectivePluginManager;
import org.dashbuilder.navigation.NavDivider;
import org.dashbuilder.navigation.NavGroup;
import org.dashbuilder.navigation.NavItem;
import org.dashbuilder.navigation.workbench.NavWorkbenchCtx;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.UberView;
import org.uberfire.ext.widgets.common.client.dropdown.PerspectiveDropDown;
import org.uberfire.ext.widgets.common.client.dropdown.PerspectiveNameProvider;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.model.ActivityResourceType;

@Dependent
public class NavItemEditor implements IsWidget {

    public enum ItemType {
        DIVIDER,
        GROUP,
        PERSPECTIVE,
        RUNTIME_PERSPECTIVE;
    }

    public interface View extends UberView<NavItemEditor> {

        void setItemName(String name);

        void setItemDescription(String description);

        void setItemType(ItemType type);

        void addCommand(String name, Command command);

        void addCommandDivider();

        void setCommandsEnabled(boolean enabled);

        void setItemEditable(boolean editable);

        void startItemEdition();

        void finishItemEdition();

        void setContextWidget(IsWidget widget);

        String i18nNewItem(String item);

        String i18nGotoItem(String item);

        String i18nDeleteItem();

        String i18nMoveUp();

        String i18nMoveDown();

        String i18nMoveFirst();

        String i18nMoveLast();
    }

    View view;
    PlaceManager placeManager;
    PerspectiveDropDown perspectiveDropDown;
    PerspectivePluginManager perspectivePluginManager;
    boolean newDividerEnabled = true;
    boolean newGroupEnabled = true;
    boolean newPerspectiveEnabled = true;
    boolean commandsEnabled = false;
    boolean moveUpEnabled = true;
    boolean moveDownEnabled = true;
    boolean gotoPerspectiveEnabled = false;
    boolean editEnabled = false;
    boolean deleteEnabled = false;
    Set<String> visiblePerspectiveIds = null;
    Set<String> hiddenPerspectiveIds = null;
    PerspectiveNameProvider perspectiveNameProvider = null;
    NavItem navItem = null;
    String perspectiveId = null;
    Command onUpdateCommand;
    Command onErrorCommand;
    Command onMoveUpCommand;
    Command onMoveDownCommand;
    Command onMoveFirstCommand;
    Command onMoveLastCommand;
    Command onDeleteCommand;
    Command onNewSubgroupCommand;
    Command onNewPerspectiveCommand;
    Command onNewDividerCommand;
    String literalGroup = "Group";
    String literalPerspective = "Perspective";
    String literalDivider = "Divider";
    String dividerName = "--------------";

    @Inject
    public NavItemEditor(View view, PlaceManager placeManager, PerspectiveDropDown perspectiveDropDown, PerspectivePluginManager perspectivePluginManager) {
        this.view = view;
        this.placeManager = placeManager;
        this.perspectiveDropDown = perspectiveDropDown;
        this.perspectivePluginManager = perspectivePluginManager;
        this.view.init(this);
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public boolean isNewGroupEnabled() {
        return newGroupEnabled;
    }

    public void setNewGroupEnabled(boolean newGroupEnabled) {
        this.newGroupEnabled = newGroupEnabled;
    }

    public boolean isNewDividerEnabled() {
        return newDividerEnabled;
    }

    public void setNewDividerEnabled(boolean newDividerEnabled) {
        this.newDividerEnabled = newDividerEnabled;
    }

    public boolean isNewPerspectiveEnabled() {
        return newPerspectiveEnabled;
    }

    public void setNewPerspectiveEnabled(boolean newPerspectiveEnabled) {
        this.newPerspectiveEnabled = newPerspectiveEnabled;
    }

    public boolean isMoveUpEnabled() {
        return moveUpEnabled;
    }

    public void setMoveUpEnabled(boolean moveUpEnabled) {
        this.moveUpEnabled = moveUpEnabled;
    }

    public boolean isMoveDownEnabled() {
        return moveDownEnabled;
    }

    public void setMoveDownEnabled(boolean moveDownEnabled) {
        this.moveDownEnabled = moveDownEnabled;
    }

    public boolean isGotoPerspectiveEnabled() {
        return gotoPerspectiveEnabled;
    }

    public void setGotoPerspectiveEnabled(boolean gotoPerspectiveEnabled) {
        this.gotoPerspectiveEnabled = gotoPerspectiveEnabled;
    }

    public void setOnUpdateCommand(Command onUpdateCommand) {
        this.onUpdateCommand = onUpdateCommand;
    }

    public void setOnErrorCommand(Command onErrorCommand) {
        this.onErrorCommand = onErrorCommand;
    }

    public void setOnMoveFirstCommand(Command onMoveFirstCommand) {
        this.onMoveFirstCommand = onMoveFirstCommand;
    }

    public void setOnMoveLastCommand(Command onMoveLastCommand) {
        this.onMoveLastCommand = onMoveLastCommand;
    }

    public void setOnMoveUpCommand(Command onMoveUpCommand) {
        this.onMoveUpCommand = onMoveUpCommand;
    }

    public void setOnMoveDownCommand(Command onMoveDownCommand) {
        this.onMoveDownCommand = onMoveDownCommand;
    }

    public void setOnDeleteCommand(Command onDeleteCommand) {
        this.onDeleteCommand = onDeleteCommand;
    }

    public void setOnNewSubgroupCommand(Command onNewSubgroupCommand) {
        this.onNewSubgroupCommand = onNewSubgroupCommand;
    }

    public void setOnNewPerspectiveCommand(Command onNewPerspectiveCommand) {
        this.onNewPerspectiveCommand = onNewPerspectiveCommand;
    }

    public void setOnNewDividerCommand(Command onNewDividerCommand) {
        this.onNewDividerCommand = onNewDividerCommand;
    }

    public NavItem getNavItem() {
        return navItem;
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

    public void setVisiblePerspectiveIds(Set<String> visiblePerspectiveIds) {
        this.visiblePerspectiveIds = visiblePerspectiveIds;
    }

    public void setHiddenPerspectiveIds(Set<String> hiddenPerspectiveIds) {
        this.hiddenPerspectiveIds = hiddenPerspectiveIds;
    }

    public void setPerspectiveNameProvider(PerspectiveNameProvider perspectiveNameProvider) {
        this.perspectiveNameProvider = perspectiveNameProvider;
    }

    public void edit(NavItem navItem) {
        this.navItem = navItem.cloneItem();

        NavWorkbenchCtx navCtx = NavWorkbenchCtx.get(navItem);
        if (navItem.getName() != null) {
            view.setItemName(navItem.getName());
        } else {
            view.setItemName(dividerName);
        }

        if (navItem.getDescription() != null) {
            view.setItemDescription(navItem.getDescription());
        }

        editEnabled = navItem.isModifiable();
        deleteEnabled = navItem.isModifiable();

        // Nav group
        if (navItem instanceof NavGroup) {
            view.setItemType(ItemType.GROUP);
            commandsEnabled = true;
        }
        // Divider
        else if (navItem instanceof NavDivider) {
            view.setItemType(ItemType.DIVIDER);
            editEnabled = false;
        }
        else if (navCtx.getResourceId() != null) {

            // Nav perspective item
            if (ActivityResourceType.PERSPECTIVE.equals(navCtx.getResourceType())) {
                if (visiblePerspectiveIds == null || visiblePerspectiveIds.contains(navCtx.getResourceId())) {
                    perspectiveId = navCtx.getResourceId();
                } else {
                    perspectiveId = visiblePerspectiveIds.iterator().next();
                    navCtx.setResourceId(perspectiveId);
                    navItem.setContext(navCtx.toString());
                }
                if (hiddenPerspectiveIds != null) {
                    perspectiveDropDown.setPerspectiveIdsExcluded(hiddenPerspectiveIds);
                }
                if (perspectiveNameProvider != null) {
                    perspectiveDropDown.setPerspectiveNameProvider(perspectiveNameProvider);
                }
                perspectiveDropDown.setMaxItems(50);
                perspectiveDropDown.setOnChange(this::onPerspectiveChanged);
                perspectiveDropDown.setSelectedPerspective(perspectiveId);
                view.setItemType(perspectivePluginManager.isRuntimePerspective(perspectiveId) ? ItemType.RUNTIME_PERSPECTIVE : ItemType.PERSPECTIVE);
                view.setContextWidget(perspectiveDropDown);
            }
        }
        else {
            // Ignore non supported items
        }

        view.setItemEditable(editEnabled);
        addCommands();
    }

    private void addCommands() {
        boolean dividerRequired = false;
        view.setCommandsEnabled(commandsEnabled || moveUpEnabled || moveDownEnabled);

        if (commandsEnabled) {
            view.setCommandsEnabled(true);
            if (newGroupEnabled) {
                view.addCommand(view.i18nNewItem(literalGroup), this::onNewSubGroup);
                dividerRequired = true;
            }
            if (newDividerEnabled) {
                view.addCommand(view.i18nNewItem(literalDivider), this::onNewDivider);
                dividerRequired = true;
            }
            if (newPerspectiveEnabled) {
                view.addCommand(view.i18nNewItem(literalPerspective), this::onNewPerspective);
                dividerRequired = true;
            }
        }

        if (moveUpEnabled || moveDownEnabled) {
            if (dividerRequired) {
                view.addCommandDivider();
            }
            dividerRequired = true;
            if (moveUpEnabled) {
                view.addCommand(view.i18nMoveFirst(), this::onMoveFirstItem);
                view.addCommand(view.i18nMoveUp(), this::onMoveUpItem);
            }
            if (moveDownEnabled) {
                view.addCommand(view.i18nMoveDown(), this::onMoveDownItem);
                view.addCommand(view.i18nMoveLast(), this::onMoveLastItem);
            }
        }
        if (gotoPerspectiveEnabled && perspectiveId != null) {
            if (dividerRequired) {
                view.addCommandDivider();
            }
            dividerRequired = true;
            view.addCommand(view.i18nGotoItem(literalPerspective), this::onGotoPerspective);
        }
        if (deleteEnabled) {
            if (dividerRequired) {
                view.addCommandDivider();
            }
            dividerRequired = true;
            view.addCommand(view.i18nDeleteItem(), this::onDeleteItem);
        }
    }

    public void onItemClick() {
        if (editEnabled) {
            view.startItemEdition();
        }
    }

    public void onItemNameChanged(String name) {
        if (name != null && name.length() > 0) {
            navItem.setName(name);
            view.setItemName(name);
            if (onUpdateCommand != null) {
                onUpdateCommand.execute();
            }
        } else {
            if (onErrorCommand != null) {
                onErrorCommand.execute();
            }
        }
    }

    private void onPerspectiveChanged() {
        String perspectiveId = perspectiveDropDown.getSelectedPerspective().getIdentifier();
        if (perspectiveId != null && perspectiveId.length() > 0) {
            NavWorkbenchCtx navCtx = NavWorkbenchCtx.perspective(perspectiveId);
            navItem.setContext(navCtx.toString());
            if (onUpdateCommand != null) {
                onUpdateCommand.execute();
            }
        } else {
            if (onErrorCommand != null) {
                onErrorCommand.execute();
            }
        }
    }

    public void onGotoPerspective() {
        placeManager.goTo(perspectiveId);
    }

    private void onNewSubGroup() {
        if (onNewSubgroupCommand != null) {
            onNewSubgroupCommand.execute();
        }
    }

    private void onNewPerspective() {
        if (onNewPerspectiveCommand != null) {
            onNewPerspectiveCommand.execute();
        }
    }

    private void onNewDivider() {
        if (onNewDividerCommand != null) {
            onNewDividerCommand.execute();
        }
    }

    public void onDeleteItem() {
        if (onDeleteCommand != null) {
            onDeleteCommand.execute();
        }
    }

    private void onMoveUpItem() {
        if (onMoveUpCommand != null) {
            onMoveUpCommand.execute();
        }
    }

    private void onMoveDownItem() {
        if (onMoveDownCommand != null) {
            onMoveDownCommand.execute();
        }
    }

    private void onMoveFirstItem() {
        if (onMoveFirstCommand != null) {
            onMoveFirstCommand.execute();
        }
    }

    private void onMoveLastItem() {
        if (onMoveLastCommand != null) {
            onMoveLastCommand.execute();
        }
    }
}
