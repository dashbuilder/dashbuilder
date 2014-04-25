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

import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasTreeItems;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.dashbuilder.client.google.GoogleRenderer;
import org.dashbuilder.client.kpi.KPIViewer;
import org.dashbuilder.client.samples.gallery.GalleryNode;
import org.dashbuilder.client.samples.gallery.GalleryNodeKPI;
import org.dashbuilder.client.samples.gallery.GalleryTree;
import org.dashbuilder.model.kpi.KPI;

public class GalleryViewImpl extends Composite implements GalleryPresenter.GalleryView {

    GalleryPresenter presenter;

    @Inject
    GoogleRenderer googleRenderer;

    @Inject
    GalleryTree galleryTree;

    @Inject
    KPIViewer kpiViewer;

    private final HorizontalPanel mainPanel = new HorizontalPanel();
    private final VerticalPanel leftPanel = new VerticalPanel();
    private final SimplePanel rightPanel = new SimplePanel();

    public void init(GalleryPresenter presenter) {
        this.presenter = presenter;
    }

    @PostConstruct
    private void initUI() {
        initWidget(mainPanel);

        Tree leftTree = initNavigationTree();
        leftTree.setWidth("200px");
        Style leftStyle = leftPanel.getElement().getStyle();
        leftStyle.setPropertyPx("margin", 5);
        leftPanel.add(leftTree);
        mainPanel.add(leftPanel);

        DecoratorPanel decorator = new DecoratorPanel();
        Style decoratorStyle = decorator.getElement().getStyle();
        decoratorStyle.setPropertyPx("marginLeft", 15);
        Style rightStyle = rightPanel.getElement().getStyle();
        rightStyle.setPropertyPx("margin", 5);
        decorator.add(rightPanel);
        mainPanel.add(decorator);

        // Greetings
        rightPanel.add(new HTML("Dashbuilder Displayer Gallery"));
    }

    private Tree initNavigationTree() {
        Tree navTree = new Tree();

        List<GalleryNode> mainNodes = galleryTree.getMainNodes();
        populateNavigationTree(mainNodes, navTree);

        navTree.addSelectionHandler(new SelectionHandler<TreeItem>() {
            public void onSelection(SelectionEvent<TreeItem> event) {
                TreeItem ti = event.getSelectedItem();
                if (ti.getUserObject() instanceof GalleryNodeKPI) {
                    GalleryNodeKPI node = (GalleryNodeKPI) ti.getUserObject();
                    treeItemClicked(ti, node.getKpi());
                }
            }
        });
        return navTree;
    }

    private void populateNavigationTree(List<GalleryNode> nodes, HasTreeItems items) {
        for (GalleryNode node: nodes) {
            TreeItem ti = new TreeItem();
            ti.setText(node.getName());
            ti.setUserObject(node);
            items.addItem(ti);
            populateNavigationTree(node.getChildren(), ti);
        }
    }

    private void treeItemClicked(TreeItem ti, KPI kpi) {
        try {
            rightPanel.clear();
            rightPanel.add(kpiViewer);
            kpiViewer.draw(kpi);

            // TODO: Find a way to make Google fullfill draw requests properly without the presence of an UF perspective change event
            googleRenderer.renderCharts();
        } catch (Exception e) {
            Window.alert(e.getMessage());
        }
    }
}
