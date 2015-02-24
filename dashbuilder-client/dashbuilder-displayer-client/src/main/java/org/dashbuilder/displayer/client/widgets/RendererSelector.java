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
package org.dashbuilder.displayer.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.RendererLibLocator;
import java.util.Set;

public class RendererSelector extends Composite {

    private static final String RENDERER_KEY = "renderer";
    
    interface RendererSelectorBinder extends UiBinder<Widget, RendererSelector>{}
    private static final RendererSelectorBinder uiBinder = GWT.create(RendererSelectorBinder.class);
    
    @UiField
    FlowPanel mainPanel;

    @UiField
    FlowPanel listPanel;

    @UiField
    HorizontalPanel radioButtonsPanel;
    
    @UiField
    ListBox listBox;
    
    public String getTitle() {
        return "Renderer selector";
    }

    public RendererSelector(DisplayerType displayerType, String currentValue, SelectorType selectorType, final RendererSelectorEventHandler handler) {
        
        // Init the widget with the view binding.
        initWidget(uiBinder.createAndBindUi(this));

        // Add the event handler.
        if (handler != null) {
            addHandler(handler, RendererSelectorEvent.TYPE);
        }

        RendererLibLocator rendererLibLocator = RendererLibLocator.get();
        Set<String> renderers = rendererLibLocator.getAvailableRenderers(displayerType);
        if (renderers != null && renderers.size() > 1) {

            if (currentValue == null) currentValue = rendererLibLocator.getDefaultRenderer(displayerType);
            
            // Build the selector.
            switch (selectorType) {
                case LIST:
                    buildListType(renderers, currentValue);
                    break;
                case RADIO:
                    buildRadioButtonsType(renderers, currentValue);
                    break;
            }
            
        } 
        // If there is only one renderer in the list, do not show the selector.
        else {
            listPanel.setVisible(false);
            radioButtonsPanel.setVisible(false);
            mainPanel.setVisible(false);
        }
    }
    
    private void buildListType(Set<String> renderers, String selectedValue) {
        
        listBox.clear();
        listPanel.setVisible(true);
        radioButtonsPanel.setVisible(false);
        
        // Add listbox contents.
        int index = 0;
        int selectedIndex = 0;
        for (String renderer : renderers) {
            if (renderer.equalsIgnoreCase(selectedValue)) selectedIndex = index; 
            listBox.addItem(renderer);
            index++;
        }
        listBox.setSelectedIndex(selectedIndex);
        
        // The click event handler.
        listBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int index = listBox.getSelectedIndex();
                String value = listBox.getValue(index);
                fireEvent(new RendererSelectorEvent(value));
            }
        });
        
    }

    private void buildRadioButtonsType(Set<String> renderers, String selectedValue) {

        radioButtonsPanel.setVisible(true);
        listPanel.setVisible(false);

        // Add listbox contents.
        for (String renderer : renderers) {
            
            final com.github.gwtbootstrap.client.ui.RadioButton rb = new com.github.gwtbootstrap.client.ui.RadioButton(RENDERER_KEY, renderer);
            rb.setValue(renderer.equalsIgnoreCase(selectedValue));
            
            rb.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    String renderer = rb.getText();
                    fireEvent(new RendererSelectorEvent(renderer));
                }
            });
            
            radioButtonsPanel.add(rb);
        }
    }

    @Override
    public void setWidth(String width) {
        super.setWidth(width);
        mainPanel.setWidth(width);
        listPanel.setWidth(width);
        radioButtonsPanel.setWidth(width);
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        mainPanel.setHeight(height);
        listPanel.setHeight(height);
        radioButtonsPanel.setHeight(height);
    }

    /**
     * The callback then a renderer is selected. 
     */
    public static class RendererSelectorEvent extends GwtEvent<RendererSelectorEventHandler> {

        public static Type<RendererSelectorEventHandler> TYPE = new Type<RendererSelectorEventHandler>();
        
        private String renderer;

        public RendererSelectorEvent(String renderer) {
            this.renderer = renderer;
        }

        @Override
        public Type<RendererSelectorEventHandler> getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(RendererSelectorEventHandler handler) {
            handler.onRendererSelected(this);
        }

        public String getRenderer() {
            return renderer;
        }
    }

    public interface RendererSelectorEventHandler extends EventHandler
    {
        void onRendererSelected(RendererSelectorEvent event);
    }
    
    public enum SelectorType {
        LIST, RADIO;
    }
}