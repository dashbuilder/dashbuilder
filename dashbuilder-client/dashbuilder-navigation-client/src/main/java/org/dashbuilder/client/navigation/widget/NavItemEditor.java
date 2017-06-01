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

        String getItemName();

        void setItemNameError(boolean hasError);

        void setItemDescription(String description);

        void setItemType(ItemType type);

        void addCommand(String name, Command command);

        void addCommandDivider();

        void setCommandsEnabled(boolean enabled);

        void setItemEditable(boolean editable);

        void setItemDeletable(boolean deletable);

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
    boolean creationEnabled = false;
    boolean moveUpEnabled = true;
    boolean moveDownEnabled = true;
    boolean gotoPerspectiveEnabled = false;
    boolean editEnabled = false;
    boolean deleteEnabled = false;
    Set<String> visiblePerspectiveIds = null;
    Set<String> hiddenPerspectiveIds = null;
    PerspectiveNameProvider perspectiveNameProvider = null;
    NavItem navItem = null;
    ItemType itemType = null;
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
    Command onEditStartedCommand;
    Command onEditFinishedCommand;
    Command onEditCancelledCommand;
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

    public void setOnEditStartedCommand(Command onEditStartedCommand) {
        this.onEditStartedCommand = onEditStartedCommand;
    }

    public void setOnEditFinishedCommand(Command onEditFinishedCommand) {
        this.onEditFinishedCommand = onEditFinishedCommand;
    }

    public void setOnEditCancelledCommand(Command onEditCancelledCommand) {
        this.onEditCancelledCommand = onEditCancelledCommand;
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

        creationEnabled = false;
        editEnabled = navItem.isModifiable();
        deleteEnabled = navItem.isModifiable();

        // Nav group
        if (navItem instanceof NavGroup) {
            view.setItemType(itemType = ItemType.GROUP);
            creationEnabled = true;
        }
        // Divider
        else if (navItem instanceof NavDivider) {
            view.setItemType(itemType = ItemType.DIVIDER);
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
                perspectiveDropDown.setWidth(150);
                perspectiveDropDown.setSelectedPerspective(perspectiveId);
                view.setItemType(itemType = perspectivePluginManager.isRuntimePerspective(perspectiveId) ? ItemType.RUNTIME_PERSPECTIVE : ItemType.PERSPECTIVE);
                view.setContextWidget(perspectiveDropDown);
            }
        }
        else {
            // Ignore non supported items
        }

        view.setItemEditable(editEnabled);
        view.setItemDeletable(deleteEnabled);
        addCommands();
    }

    private void addCommands() {
        boolean dividerRequired = false;

        if (creationEnabled) {
            if (newGroupEnabled) {
                this.addCommand(view.i18nNewItem(literalGroup), this::onNewSubGroup);
                dividerRequired = true;
            }
            if (newDividerEnabled) {
                this.addCommand(view.i18nNewItem(literalDivider), this::onNewDivider);
                dividerRequired = true;
            }
            if (newPerspectiveEnabled) {
                this.addCommand(view.i18nNewItem(literalPerspective), this::onNewPerspective);
                dividerRequired = true;
            }
        }

        if (moveUpEnabled || moveDownEnabled) {
            if (dividerRequired) {
                view.addCommandDivider();
            }
            dividerRequired = true;
            if (moveUpEnabled) {
                this.addCommand(view.i18nMoveFirst(), this::onMoveFirstItem);
                this.addCommand(view.i18nMoveUp(), this::onMoveUpItem);
            }
            if (moveDownEnabled) {
                this.addCommand(view.i18nMoveDown(), this::onMoveDownItem);
                this.addCommand(view.i18nMoveLast(), this::onMoveLastItem);
            }
        }
        if (gotoPerspectiveEnabled && perspectiveId != null) {
            if (dividerRequired) {
                view.addCommandDivider();
            }
            dividerRequired = true;
            this.addCommand(view.i18nGotoItem(literalPerspective), this::onGotoPerspective);
        }
    }

    private void addCommand(String name, Command action) {
        view.addCommand(name, action);
        view.setCommandsEnabled(true);
    }

    public void onItemEdit() {
        if (editEnabled) {
            view.startItemEdition();
            onEditStarted();
        }
    }

    public void confirmChanges() {
        boolean error = false;
        boolean update = false;

        // Capture name changes
        String name = view.getItemName();
        if (name != null && !name.trim().isEmpty()) {
            if (!name.equals(navItem.getName())) {
                navItem.setName(name);
                view.setItemName(name);
                update = true;
            }
        } else {
            error = true;
        }
        view.setItemNameError(error);

        // Capture perspective changes
        if (ItemType.PERSPECTIVE.equals(itemType) || ItemType.RUNTIME_PERSPECTIVE.equals(itemType)) {
            String perspectiveId = perspectiveDropDown.getSelectedPerspective().getIdentifier();
            if (perspectiveId != null && !perspectiveId.trim().isEmpty()) {
                NavWorkbenchCtx navCtx = NavWorkbenchCtx.get(navItem);
                if (navCtx.getResourceId() != null && !navCtx.getResourceId().equals(perspectiveId)){
                    NavWorkbenchCtx newCtx = NavWorkbenchCtx.perspective(perspectiveId);
                    navItem.setContext(newCtx.toString());
                    update = true;
                }
            } else {
                error = true;
            }
        }

        // Process updates
        if (!error) {
            finishEditing();
            if (update && onUpdateCommand != null) {
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

    void onNewSubGroup() {
        if (onNewSubgroupCommand != null) {
            onNewSubgroupCommand.execute();
        }
    }

    void onNewPerspective() {
        if (onNewPerspectiveCommand != null) {
            onNewPerspectiveCommand.execute();
        }
    }

    void onNewDivider() {
        if (onNewDividerCommand != null) {
            onNewDividerCommand.execute();
        }
    }

    void onDeleteItem() {
        if (deleteEnabled && onDeleteCommand != null) {
            onDeleteCommand.execute();
        }
    }

    void onMoveUpItem() {
        if (onMoveUpCommand != null) {
            onMoveUpCommand.execute();
        }
    }

    void onMoveDownItem() {
        if (onMoveDownCommand != null) {
            onMoveDownCommand.execute();
        }
    }

    void onMoveFirstItem() {
        if (onMoveFirstCommand != null) {
            onMoveFirstCommand.execute();
        }
    }

    void onMoveLastItem() {
        if (onMoveLastCommand != null) {
            onMoveLastCommand.execute();
        }
    }

    void onEditStarted() {
        if (onEditStartedCommand != null) {
            onEditStartedCommand.execute();
        }
    }

    void onEditFinished() {
        if (onEditFinishedCommand != null) {
            onEditFinishedCommand.execute();
        }
    }

    void onEditCancelled() {
        if (onEditCancelledCommand != null) {
            onEditCancelledCommand.execute();
        }
    }

    public void finishEditing() {
        view.finishItemEdition();
        onEditFinished();
    }

    public void cancelEdition() {
        view.finishItemEdition();
        view.setItemName(navItem.getName());
        if (ItemType.PERSPECTIVE.equals(itemType) || ItemType.RUNTIME_PERSPECTIVE.equals(itemType)) {
            perspectiveDropDown.setSelectedPerspective(perspectiveId);
        }
        onEditCancelled();
    }
}
