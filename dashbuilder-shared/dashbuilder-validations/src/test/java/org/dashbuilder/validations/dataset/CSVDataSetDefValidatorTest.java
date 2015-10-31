package org.dashbuilder.validations.dataset;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.dashbuilder.dataset.def.CSVDataSetDef;
import org.dashbuilder.dataset.validation.groups.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class CSVDataSetDefValidatorTest extends AbstractValidationTest {

    @Mock
    CSVDataSetDef csvDataSetDef;
    private CSVDataSetDefValidator tested;

     
    @Before
    public void setup() {
        super.setup();
        tested = spy(new CSVDataSetDefValidator());
        doReturn(validator).when(tested).getDashbuilderValidator();
    }
    
    @Test
    public void testValidateAttributesUsingFilePath() {
        final boolean isUsingFilePath = true;
        tested.validateAttributes(csvDataSetDef, isUsingFilePath);
        verify(validator, times(1)).validate(csvDataSetDef, CSVDataSetDefValidation.class, CSVDataSetDefFilePathValidation.class);
    }

    @Test
    public void testValidateAttributesUsingFileUrl() {
        final boolean isUsingFilePath = false;
        tested.validateAttributes(csvDataSetDef, isUsingFilePath);
        verify(validator, times(1)).validate(csvDataSetDef, CSVDataSetDefValidation.class, CSVDataSetDefFileURLValidation.class);
    }
    
    @Test
    public void testValidateUsingFilePath() {
        final boolean isCacheEnabled = true;
        final boolean isPushEnabled = true;
        final boolean isRefreshEnabled = true;
        final boolean isUsingFilePath = true;
        tested.validate(csvDataSetDef, isCacheEnabled, isPushEnabled, isRefreshEnabled, isUsingFilePath);
        verify(validator, times(1)).validate(csvDataSetDef, 
                DataSetDefBasicAttributesGroup.class,
                DataSetDefProviderTypeGroup.class,
                DataSetDefCacheRowsValidation.class,
                DataSetDefPushSizeValidation.class,
                DataSetDefRefreshIntervalValidation.class,
                CSVDataSetDefValidation.class,
                CSVDataSetDefFilePathValidation.class);
    }

    @Test
    public void testValidateUsingFileUrl() {
        final boolean isCacheEnabled = true;
        final boolean isPushEnabled = true;
        final boolean isRefreshEnabled = true;
        final boolean isUsingFilePath = false;
        tested.validate(csvDataSetDef, isCacheEnabled, isPushEnabled, isRefreshEnabled, isUsingFilePath);
        verify(validator, times(1)).validate(csvDataSetDef,
                DataSetDefBasicAttributesGroup.class,
                DataSetDefProviderTypeGroup.class,
                DataSetDefCacheRowsValidation.class,
                DataSetDefPushSizeValidation.class,
                DataSetDefRefreshIntervalValidation.class,
                CSVDataSetDefValidation.class,
                CSVDataSetDefFileURLValidation.class);
    }

    @Test
    public void testValidateNoCache() {
        final boolean isCacheEnabled = false;
        final boolean isPushEnabled = true;
        final boolean isRefreshEnabled = true;
        final boolean isUsingFilePath = false;
        tested.validate(csvDataSetDef, isCacheEnabled, isPushEnabled, isRefreshEnabled, isUsingFilePath);
        verify(validator, times(1)).validate(csvDataSetDef,
                DataSetDefBasicAttributesGroup.class,
                DataSetDefProviderTypeGroup.class,
                DataSetDefPushSizeValidation.class,
                DataSetDefRefreshIntervalValidation.class,
                CSVDataSetDefValidation.class,
                CSVDataSetDefFileURLValidation.class);
    }

    @Test
    public void testValidateNoPush() {
        final boolean isCacheEnabled = true;
        final boolean isPushEnabled = false;
        final boolean isRefreshEnabled = true;
        final boolean isUsingFilePath = false;
        tested.validate(csvDataSetDef, isCacheEnabled, isPushEnabled, isRefreshEnabled, isUsingFilePath);
        verify(validator, times(1)).validate(csvDataSetDef,
                DataSetDefBasicAttributesGroup.class,
                DataSetDefProviderTypeGroup.class,
                DataSetDefCacheRowsValidation.class,
                DataSetDefRefreshIntervalValidation.class,
                CSVDataSetDefValidation.class,
                CSVDataSetDefFileURLValidation.class);
    }

    @Test
    public void testValidateNoRefresh() {
        final boolean isCacheEnabled = true;
        final boolean isPushEnabled = true;
        final boolean isRefreshEnabled = false;
        final boolean isUsingFilePath = false;
        tested.validate(csvDataSetDef, isCacheEnabled, isPushEnabled, isRefreshEnabled, isUsingFilePath);
        verify(validator, times(1)).validate(csvDataSetDef,
                DataSetDefBasicAttributesGroup.class,
                DataSetDefProviderTypeGroup.class,
                DataSetDefCacheRowsValidation.class,
                DataSetDefPushSizeValidation.class,
                CSVDataSetDefValidation.class,
                CSVDataSetDefFileURLValidation.class);
    }
    
}
