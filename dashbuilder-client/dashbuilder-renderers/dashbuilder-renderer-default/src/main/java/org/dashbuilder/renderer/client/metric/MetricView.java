/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.renderer.client.metric;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSubType;
import org.dashbuilder.displayer.client.AbstractDisplayerView;
import org.dashbuilder.renderer.client.resources.i18n.MetricConstants;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Jumbotron;
import org.gwtbootstrap3.client.ui.html.Paragraph;

public class MetricView extends AbstractDisplayerView<MetricDisplayer> implements MetricDisplayer.View {

    @UiField
    protected Jumbotron heroPanel;

    @UiField
    protected FocusPanel centerPanel;

    @UiField
    protected Paragraph titlePanel;

    @UiField
    protected Panel metricPanel;

    @UiField
    protected Heading metricHeading;

    interface Binder extends UiBinder<Widget, MetricView> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    @Override
    public void init(MetricDisplayer presenter) {
        super.setPresenter(presenter);
        super.setVisualization(uiBinder.createAndBindUi(this));

        Style style = heroPanel.getElement().getStyle();
        style.setPadding(0, Style.Unit.PX);
        style.setPropertyPx("borderRadius", 6);
        style.setFontWeight(Style.FontWeight.BOLD);
        style.setTextAlign(Style.TextAlign.CENTER);
        style.setVerticalAlign(Style.VerticalAlign.MIDDLE);
    }

    @Override
    public String getColumnsTitle() {
        return MetricConstants.INSTANCE.metricDisplayer_columnsTitle();
    }

    @Override
    public void showTitle(String title) {
        titlePanel.setVisible(true);
        titlePanel.getElement().getStyle().setProperty("fontWeight", "400");
        titlePanel.setText(title);
    }

    @Override
    public void setWidth(int width) {
        heroPanel.getElement().getStyle().setWidth(width, Style.Unit.PX);
        centerPanel.getElement().getStyle().setWidth(width, Style.Unit.PX);
    }

    @Override
    public void setHeight(int height) {
        heroPanel.getElement().getStyle().setHeight(height, Style.Unit.PX);
        centerPanel.getElement().getStyle().setHeight(height, Style.Unit.PX);
    }

    @Override
    public void setBgColor(String color) {
        Style style = heroPanel.getElement().getStyle();
        style.setBackgroundColor(color);
    }

    public String getBgColor() {
        Style style = heroPanel.getElement().getStyle();
        return style.getBackgroundColor();
    }

    @Override
    public void setMarginTop(int marginTop) {
        Style style = centerPanel.getElement().getStyle();
        style.setPaddingTop(marginTop, Style.Unit.PX);
    }

    @Override
    public void setMarginBottom(int marginBottom) {
        Style style = centerPanel.getElement().getStyle();
        style.setPaddingBottom(marginBottom, Style.Unit.PX);
    }

    @Override
    public void setMarginRight(int marginRight) {
        Style style = centerPanel.getElement().getStyle();
        style.setPaddingRight(marginRight, Style.Unit.PX);
    }

    @Override
    public void setMarginLeft(int marginLeft) {
        Style style = centerPanel.getElement().getStyle();
        style.setPaddingLeft(marginLeft, Style.Unit.PX);
    }

    @Override
    public void setFilterEnabled(boolean enabled) {
        Style style = centerPanel.getElement().getStyle();
        if (enabled) {
            style.setCursor(Style.Cursor.POINTER);
            centerPanel.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent clickEvent) {
                    getPresenter().updateFilter();
                }
            });
        } else {
            style.setCursor(Style.Cursor.DEFAULT);
        }
    }

    protected String filterOffColor = null;

    @Override
    public void setFilterActive(boolean active) {
        // Switch on
        if (active && filterOffColor == null) {
            filterOffColor = getBgColor();
            this.setBgColor("#DDDDDD");
        }
        // Switch off
        if (!active && filterOffColor != null) {
            this.setBgColor(filterOffColor);
            filterOffColor = null;
        }
    }

    @Override
    public void setValue(String value) {
        metricHeading.setText(value);
    }

    @Override
    public void nodata() {
        metricHeading.setText(MetricConstants.INSTANCE.metricDisplayer_noDataAvailable());
    }
}
