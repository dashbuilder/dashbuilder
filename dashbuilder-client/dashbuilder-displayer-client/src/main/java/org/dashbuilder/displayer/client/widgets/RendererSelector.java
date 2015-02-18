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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.RendererLibLocator;

import javax.swing.plaf.basic.BasicMenuUI;
import java.util.Set;

public class RendererSelector extends Composite {

    interface RendererSelectorBinder extends UiBinder<Widget, RendererSelector>{}
    private static final RendererSelectorBinder uiBinder = GWT.create(RendererSelectorBinder.class);
    
    @UiField
    FlowPanel mainPanel;
    
    @UiField
    ListBox listBox;
    
    public String getTitle() {
        return "Renderer selector";
    }

    public RendererSelector(DisplayerType displayerType, final RendererSelectorEvent rendererSelectorEvent) {
        
        // Init the widget with the view binding.
        initWidget(uiBinder.createAndBindUi(this));

        RendererLibLocator rendererLibLocator = RendererLibLocator.get();
        Set<String> renderers = rendererLibLocator.getAvailableRenderers(displayerType);
        if (renderers != null && renderers.size() > 1) {
            // Add listbox contents.
            listBox.clear();
            if (renderers != null && !renderers.isEmpty()) {
                for (String renderer : renderers) {
                    listBox.addItem(renderer);
                }
            }

            // The click event handler.
            listBox.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    if (rendererSelectorEvent != null) rendererSelectorEvent.onRendererChanged(getRendererSelected());
                }
            });
        } 
        // If there is only one renderer in the list, do not show the selector.
        else {
            listBox.setVisible(false);
        }
    }
    
    public String getRendererSelected() {
        int index = listBox.getSelectedIndex();
        return listBox.getValue(index);
    }

    /**
     * The callback then a renderer is selected. 
     */
    public interface RendererSelectorEvent {
        
        public void onRendererChanged(String renderer);
        
    }
}