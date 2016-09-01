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
package org.dashbuilder.renderer.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSubType;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.AbstractRendererLibrary;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.renderer.client.metric.MetricDisplayer;
import org.dashbuilder.renderer.client.selector.SelectorDisplayer;
import org.dashbuilder.renderer.client.table.TableDisplayer;

import static org.dashbuilder.displayer.DisplayerType.*;
import static org.dashbuilder.displayer.DisplayerSubType.*;

/**
 * Default renderer
 */
@ApplicationScoped
public class DefaultRenderer extends AbstractRendererLibrary {

    public static final String UUID = "default";

    @PostConstruct
    private void init() {
        publishJsFunctions();
    }

    @Override
    public String getUUID() {
        return UUID;
    }

    @Override
    public String getName() {
        return "GWT Core";
    }

    @Override
    public boolean isDefault(DisplayerType type) {
        return TABLE.equals(type) ||
                SELECTOR.equals(type) ||
                METRIC.equals(type);
    }

    @Override
    public List<DisplayerType> getSupportedTypes() {
        return Arrays.asList(
                TABLE,
                SELECTOR,
                METRIC);
    }

    @Override
    public List<DisplayerSubType> getSupportedSubtypes(DisplayerType displayerType) {
        switch (displayerType) {
            case METRIC:
                return Arrays.asList(METRIC_CARD, METRIC_CARD2, METRIC_QUOTA, METRIC_PLAIN_TEXT);
            default:
                return Arrays.asList();
        }
    }

    @Override
    public Displayer lookupDisplayer(DisplayerSettings displayerSettings) {
        DisplayerType type = displayerSettings.getType();
        if (TABLE.equals(type)) {
            return new TableDisplayer();
        }
        if (SELECTOR.equals(type)) {
            return new SelectorDisplayer();
        }
        if (METRIC.equals(type)) {
            MetricDisplayer displayer = new MetricDisplayer();
            _metricDisplayerMap.put(displayer.getView().getUniqueId(), displayer);
            return displayer;
        }

        return null;
    }

    private native void publishJsFunctions() /*-{
        $wnd.metricDisplayerDoFilter = $entry(@org.dashbuilder.renderer.client.DefaultRenderer::metricDisplayerDoFilter(Ljava/lang/String;));
    }-*/;

    protected static Map<String,MetricDisplayer> _metricDisplayerMap = new HashMap();

    public static void metricDisplayerDoFilter(String displayerId) {
        MetricDisplayer displayer = _metricDisplayerMap.get(displayerId);
        if (displayer != null) {
            displayer.updateFilter();
        }
    }

    public static void closeDisplayer(String displayerId) {
        _metricDisplayerMap.remove(displayerId);
    }
}
