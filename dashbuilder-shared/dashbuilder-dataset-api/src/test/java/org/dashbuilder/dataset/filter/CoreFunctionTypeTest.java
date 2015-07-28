package org.dashbuilder.dataset.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dashbuilder.dataset.filter.CoreFunctionType.*;

import org.dashbuilder.dataset.ColumnType;
import org.junit.Test;

public class CoreFunctionTypeTest {

    @Test
    public void getSupportedTypeTest() {
        
        CoreFunctionType[] dateFunctionTypesSupported = {IS_NULL, NOT_NULL, EQUALS_TO, NOT_EQUALS_TO, TIME_FRAME,
            GREATER_THAN, GREATER_OR_EQUALS_TO, LOWER_THAN, LOWER_OR_EQUALS_TO, BETWEEN};

        CoreFunctionType[] textLabelFunctionTypesSupported = {IS_NULL, NOT_NULL, EQUALS_TO, NOT_EQUALS_TO, LIKE_TO,
                GREATER_THAN, GREATER_OR_EQUALS_TO, LOWER_THAN, LOWER_OR_EQUALS_TO, BETWEEN};

        CoreFunctionType[] numericFunctionTypesSupported = {IS_NULL, NOT_NULL, EQUALS_TO, NOT_EQUALS_TO,
                GREATER_THAN, GREATER_OR_EQUALS_TO, LOWER_THAN, LOWER_OR_EQUALS_TO, BETWEEN};

        assertThat(CoreFunctionType.getSupportedTypes(ColumnType.DATE))
                .contains(dateFunctionTypesSupported);
        
        assertThat(CoreFunctionType.getSupportedTypes(ColumnType.LABEL))
                .containsExactly(textLabelFunctionTypesSupported);

        assertThat(CoreFunctionType.getSupportedTypes(ColumnType.NUMBER))
                .containsExactly(numericFunctionTypesSupported);

        assertThat(CoreFunctionType.getSupportedTypes(ColumnType.TEXT))
                .containsExactly(textLabelFunctionTypesSupported);
    }
}
