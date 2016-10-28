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

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.client.navigation.resources.i18n.NavigationConstants;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Node;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Dependent
@Templated
public class NavCarouselWidgetView extends BaseNavWidgetView<NavCarouselWidget>
    implements NavCarouselWidget.View {

    @Inject
    @DataField
    Div carouselDiv;

    NavCarouselWidget presenter;

    @Override
    public void init(NavCarouselWidget presenter) {
        this.presenter = presenter;
        super.navWidget = carouselDiv;
    }

    @Override
    public void setActive(boolean active) {
        // Useless in a tab list
    }

    @Override
    public void addDivider() {
        // Useless in a tab list
    }

    @Override
    public void errorNavItemsEmpty() {
        super.errorNavItemsEmpty();
        DOMUtil.removeAllChildren(carouselDiv);
    }

    @Override
    public void addContentSlide(IsWidget widget) {
        DivElement div = Document.get().createDivElement();
        div.setClassName(carouselDiv.getChildNodes().getLength() == 0 ? "item active" : "item");
        div.appendChild(widget.asWidget().getElement());
        carouselDiv.appendChild((Node) div);
    }

    @Override
    public void recursivityError() {
        Element div = DOM.createDiv();
        Element span1 = DOM.createSpan();
        Element span2 = DOM.createSpan();

        div.setClassName("alert alert-warning");
        div.getStyle().setWidth(30, Style.Unit.PCT);
        div.getStyle().setMargin(10, Style.Unit.PX);
        span1.setClassName("pficon pficon-warning-triangle-o");
        span2.setInnerText(NavigationConstants.INSTANCE.navCarouselDragComponentRecursivityError());

        div.appendChild(span1);
        div.appendChild(span2);
        carouselDiv.appendChild((Node) div);
    }
}
