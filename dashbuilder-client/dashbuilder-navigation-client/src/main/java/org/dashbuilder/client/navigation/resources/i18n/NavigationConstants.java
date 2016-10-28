/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.client.navigation.resources.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface NavigationConstants extends Messages {

    NavigationConstants INSTANCE = GWT.create(NavigationConstants.class);

    String newItem(String itemName);

    String newItemName(String itemName);

    String itemMenuTitle();

    String deleteItem();

    String moveUp();

    String moveDown();

    String moveFirst();

    String moveLast();

    String gotoItem(String itemName);

    String saveChanges();

    String save();

    String cancel();

    String navTabListDragComponent();

    String navTilesDragComponent();

    String navCarouselDragComponent();

    String navTabListDragComponentHelp();

    String navTilesDragComponentHelp();

    String navCarouselDragComponentHelp();

    String navTabListDragComponentRecursivityError();

    String navTilesDragComponentRecursivityError();

    String navCarouselDragComponentRecursivityError();

    String navItemNotFound(String navItem);

    String navItemsEmpty();

    String navItemSelectorHint();

    String navItemSelectorHelp();

    String navItemSelectorLabel();

    String navItemSelectorHeader();

    String openNavItem(String itemName);

    String gotoNavItem(String itemName);

    String showNavItem(String itemName);
}
