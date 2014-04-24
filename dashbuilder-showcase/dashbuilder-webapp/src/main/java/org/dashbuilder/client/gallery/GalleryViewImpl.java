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
package org.dashbuilder.client.gallery;

import javax.annotation.PostConstruct;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.displayer.DataDisplayerViewer;
import org.dashbuilder.model.displayer.DataDisplayerType;

public class GalleryViewImpl extends Composite implements GalleryPresenter.GalleryView {

    interface GalleryViewBinder extends UiBinder<Widget, GalleryViewImpl> {
    }

    private static GalleryViewBinder uiBinder = GWT.create(GalleryViewBinder.class);

    GalleryPresenter presenter;

    @UiField
    SimplePanel navigationPanel = new SimplePanel();

    @UiField
    SimplePanel detailsPanel = new SimplePanel();

    public GalleryViewImpl() {
        initWidget( uiBinder.createAndBindUi(this));
    }

    public void init(GalleryPresenter presenter) {
        this.presenter = presenter;
    }

    @PostConstruct
    private void initUI() {
        Tree navTree = initNavigationTree();
        navigationPanel.add(navTree);
    }

    private Tree initNavigationTree() {
        Tree tree = new Tree();

        DataDisplayerType[] types = DataDisplayerType.values();
        for (int i = 0; i < types.length; i++) {
            DataDisplayerType type = types[i];
            TreeItem ti = new TreeItem();
            ti.setText(type.name());
            ti.addTextItem("Basic");
            tree.addItem(ti);
        }

        tree.addSelectionHandler(new SelectionHandler<TreeItem>() {
            public void onSelection(SelectionEvent<TreeItem> event) {
                TreeItem ti = event.getSelectedItem();
                TreeItem parent = ti.getParentItem();
                if (parent != null) {
                    //Window.alert(ti.getText());
                    DataDisplayerType displayerType = DataDisplayerType.getByName(parent.getText());
                    menuOptionClicked(displayerType, ti);
                }
            }
        });
        return tree;
    }

    private void menuOptionClicked(DataDisplayerType displayerType, TreeItem ti) {
        try {
            DataDisplayerViewer dataDisplayerViewer = presenter.getDataDisplayerSample(displayerType);
            detailsPanel.clear();
            detailsPanel.add(dataDisplayerViewer);
        } catch (Exception e) {
            Window.alert(e.getMessage());
        }
    }
}
