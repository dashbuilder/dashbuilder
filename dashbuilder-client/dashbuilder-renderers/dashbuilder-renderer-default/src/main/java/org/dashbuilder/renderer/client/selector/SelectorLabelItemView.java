/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.renderer.client.selector;

import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import org.jboss.errai.common.client.dom.Button;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Templated
public class SelectorLabelItemView implements SelectorLabelItem.View, IsElement {

    @Inject
    @DataField
    Button item;

    SelectorLabelItem presenter;

    @Override
    public void init(SelectorLabelItem presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setValue(String value) {
        item.setTextContent(value);
    }

    @Override
    public void setDescription(String description) {
        item.setTitle(description);
    }

    @Override
    public void setWidth(int percentage) {
        item.getStyle().setProperty("width", percentage + "%");
    }

    @Override
    public void select() {
        item.setClassName("btn btn-primary");
    }

    @Override
    public void reset() {
        item.setClassName("btn btn-default");
    }

    @EventHandler("item")
    public void onItemClick(ClickEvent event) {
        presenter.onItemClick();
    }
}