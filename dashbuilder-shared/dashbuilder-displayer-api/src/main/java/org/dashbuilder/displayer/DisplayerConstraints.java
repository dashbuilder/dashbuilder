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
package org.dashbuilder.displayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dashbuilder.dataset.DataSetLookupConstraints;

/**
 * Every Displayer implementation can used this class to specify what are the supported DisplayerSettings attributes.
 */
public class DisplayerConstraints {

    public static final int ERROR_DATASET_LOOKUP_CONSTRAINTS_NOT_FOUND = 301;

    protected DataSetLookupConstraints dataSetLookupConstraints;
    protected Set<DisplayerAttributeDef> supportedEditorAttributes;

    public DisplayerConstraints(DataSetLookupConstraints dataSetLookupConstraints) {
        this.dataSetLookupConstraints = dataSetLookupConstraints;
        supportedEditorAttributes = new HashSet<DisplayerAttributeDef>();
    }

    public DisplayerConstraints supportsAttribute( DisplayerAttributeDef attributeDef ) {

        // Support the attribute and all its ancestors.
        DisplayerAttributeDef _attr = attributeDef;
        while (_attr != null) {
            supportedEditorAttributes.add(_attr);
            _attr = _attr.getParent();
        }
        // ... and all its descendants as well.
        if (attributeDef instanceof DisplayerAttributeGroupDef) {
            for (DisplayerAttributeDef member : ((DisplayerAttributeGroupDef) attributeDef).getChildren()) {
                supportsAttribute(member);
            }
        }
        return this;
    }

    public Set<DisplayerAttributeDef> getSupportedAttributes() {
        return supportedEditorAttributes;
    }

    public DataSetLookupConstraints getDataSetLookupConstraints() {
        return dataSetLookupConstraints;
    }

    public DisplayerConstraints setDataSetLookupConstraints(DataSetLookupConstraints dataSetLookupConstraints) {
        this.dataSetLookupConstraints = dataSetLookupConstraints;
        return this;
    }

    public ValidationError check(DisplayerSettings settings) {
        if (dataSetLookupConstraints == null) {
            return new ValidationError(ERROR_DATASET_LOOKUP_CONSTRAINTS_NOT_FOUND);
        }
        if (settings.getDataSet() != null) {
            DataSetLookupConstraints.ValidationError error = dataSetLookupConstraints.check(settings.getDataSet());
            if (error != null) return new ValidationError(error.getCode(), error.getParameters());
        }
        else if (settings.getDataSetLookup() != null) {
            DataSetLookupConstraints.ValidationError error = dataSetLookupConstraints.check(settings.getDataSetLookup());
            if (error != null) return new ValidationError(error.getCode(), error.getParameters());
        }
        return null;
    }

    public class ValidationError {
        int code = -1;
        List parameters = new ArrayList();

        public ValidationError(int code, Object... params) {
            this.code = code;
            this.parameters.add(params);
        }

        public int getCode() {
            return code;
        }

        public List getParameters() {
            return parameters;
        }
    }
}