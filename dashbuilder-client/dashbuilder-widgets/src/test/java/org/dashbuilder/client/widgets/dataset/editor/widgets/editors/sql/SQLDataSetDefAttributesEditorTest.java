/*
 * Copyright 2015 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.client.widgets.dataset.editor.widgets.editors.sql;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.dashbuilder.dataset.def.SQLDataSetDef;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

@RunWith(GwtMockitoTestRunner.class)
public class SQLDataSetDefAttributesEditorTest {

    // Thing under test
    SQLDataSetDefAttributesEditor editor;

    @Mock
    SQLDataSetDef tableDSDef;
    @Mock
    SQLDataSetDef queryDSDef;

    @Before
    public void setup() {
        editor = Mockito.spy(new SQLDataSetDefAttributesEditor());
    }

    @Test
    public void setTableBasedDSDef() {
        when(tableDSDef.getDbTable()).thenReturn("mytable");
        when(tableDSDef.getDbSQL()).thenReturn(null);

        editor.set(tableDSDef);

        verify(editor).enableTable();
        verify(editor, never()).enableQuery();
    }

    @Test
    public void setQueryBasedDSDef() {
        when(queryDSDef.getDbTable()).thenReturn(null); //query-based recognized by table being null
        when(queryDSDef.getDbSQL()).thenReturn("select * from person;");

        editor.set(queryDSDef);

        verify(editor).enableQuery();
        verify(editor, never()).enableTable();
    }
}
