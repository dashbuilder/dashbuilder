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
package org.dashbuilder.client.google;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.visualizations.Table;
import com.google.gwt.visualization.client.visualizations.Table.Options;
import org.dashbuilder.model.displayer.TableDisplayer;

public class GoogleTableViewer extends GoogleDisplayerViewer<TableDisplayer> {

    protected int pageSize = 20;
    protected int currentPage = 1;
    protected int numberOfRows = 0;
    protected int numberOfPages = 1;

    @Override
    public String getPackage() {
        return Table.PACKAGE;
    }

    @Override
    public Widget createChart() {
        pageSize = dataDisplayer.getPageSize();
        numberOfRows = dataSetHandler.getDataSetMetadata().getNumberOfRows();
        numberOfPages = ((numberOfRows-1) / pageSize) + 1;
        if (currentPage > numberOfPages) currentPage = 1;

        Table table = new Table(createTable(), createOptions());

        HTML titleHtml = new HTML();
        if (dataDisplayer.isTitleVisible()) {
            titleHtml.setText(dataDisplayer.getTitle());
        }

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(titleHtml);
        verticalPanel.add(table);
        return verticalPanel;
    }

    private Options createOptions() {
        Options options = Options.create();
        options.setPageSize(dataDisplayer.getPageSize());
        options.setShowRowNumber(true);
        return options;
    }

    @Override
    public void draw() {
        // Draw only the data subset corresponding to the current page.
        int pageSize = dataDisplayer.getPageSize();
        int offset = (currentPage - 1) * pageSize;
        dataSetHandler.trimDataSet(offset, pageSize);

        super.draw();
    }

    private void gotoNextPage() {
        currentPage++;
        this.draw();
    }
}
