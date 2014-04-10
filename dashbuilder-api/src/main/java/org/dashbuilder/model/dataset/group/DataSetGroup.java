/**
 * Copyright (C) 2012 JBoss Inc
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
package org.dashbuilder.model.dataset.group;

import java.util.List;
import java.util.ArrayList;

import org.dashbuilder.model.dataset.DataSetOp;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * A data set group operation.
 */
@Portable
public class DataSetGroup implements DataSetOp {

    protected List<Domain> domainList = new ArrayList<Domain>();
    protected List<Range> rangeList = new ArrayList<Range>();

    public void addDomains(Domain... domains) {
        for (Domain domain : domains) {
            domainList.add(domain);
        }
    }

    public void addRanges(Range... ranges) {
        for (Range range : ranges) {
            rangeList.add(range);
        }
    }

    public List<Domain> getDomainList() {
        return domainList;
    }

    public List<Range> getRangeList() {
        return rangeList;
    }

    public boolean equals(Object other) {
        // TODO
        return false;
    }
}
