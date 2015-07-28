package org.dashbuilder.dataset.engine.filter;

import org.dashbuilder.dataset.filter.CoreFunctionFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * <p>Unit testing for some core functions.</p>
 * 
 * @since 0.3.0
 */
@RunWith(MockitoJUnitRunner.class)
public class CoreFunctionTest {

    @Mock
    private DataSetFilterContext filterContext;
    
    @Mock
    private CoreFunctionFilter coreFunctionFilter;
    
    private CoreFunction coreFunction;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(coreFunctionFilter.getColumnId()).thenReturn("column1");
        coreFunction = Mockito.spy(new CoreFunction(filterContext, coreFunctionFilter));
    }

    @Test
    public void testLikeTo() {
        String text = "Boston";
        doReturn("b%").when(coreFunction).getParameter(0);
        boolean isLikeTo = coreFunction.isLikeTo(text);
        assertTrue(isLikeTo);

        text = "London";
        doReturn("b%").when(coreFunction).getParameter(0);
        isLikeTo = coreFunction.isLikeTo(text);
        assertTrue(!isLikeTo);

        text = "Barcelona";
        doReturn("barce_ona").when(coreFunction).getParameter(0);
        isLikeTo = coreFunction.isLikeTo(text);
        assertTrue(isLikeTo);
        
        text = "Barcelona";
        doReturn("barce%").when(coreFunction).getParameter(0);
        isLikeTo = coreFunction.isLikeTo(text);
        assertTrue(isLikeTo);

        text = "Barcelona";
        doReturn("%rce%").when(coreFunction).getParameter(0);
        isLikeTo = coreFunction.isLikeTo(text);
        assertTrue(isLikeTo);

        text = "Barcelona";
        doReturn("%r_ce%").when(coreFunction).getParameter(0);
        isLikeTo = coreFunction.isLikeTo(text);
        assertTrue(!isLikeTo);
    }
    
}
