package org.dashbuilder.dataset.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dashbuilder.dataset.filter.CoreFunctionType.BETWEEN;
import static org.dashbuilder.dataset.filter.CoreFunctionType.EQUALS_TO;
import static org.dashbuilder.dataset.filter.CoreFunctionType.GREATER_OR_EQUALS_TO;
import static org.dashbuilder.dataset.filter.CoreFunctionType.GREATER_THAN;
import static org.dashbuilder.dataset.filter.CoreFunctionType.IS_NULL;
import static org.dashbuilder.dataset.filter.CoreFunctionType.LOWER_OR_EQUALS_TO;
import static org.dashbuilder.dataset.filter.CoreFunctionType.LOWER_THAN;
import static org.dashbuilder.dataset.filter.CoreFunctionType.NOT_EQUALS_TO;
import static org.dashbuilder.dataset.filter.CoreFunctionType.NOT_NULL;

import org.dashbuilder.dataset.ColumnType;
import org.junit.Test;

public class CoreFunctionTypeTest {

    @Test
    public void getSupportedTypeTest() {
        assertThat(CoreFunctionType.getSupportedTypes(ColumnType.DATE))
                .contains(CoreFunctionType.values());

        CoreFunctionType[] allExceptTimeFrame = {IS_NULL, NOT_NULL, EQUALS_TO, NOT_EQUALS_TO,
            GREATER_THAN, GREATER_OR_EQUALS_TO, LOWER_THAN, LOWER_OR_EQUALS_TO, BETWEEN};

        assertThat(CoreFunctionType.getSupportedTypes(ColumnType.LABEL))
                .containsExactly(allExceptTimeFrame);

        assertThat(CoreFunctionType.getSupportedTypes(ColumnType.NUMBER))
                .containsExactly(allExceptTimeFrame);

        assertThat(CoreFunctionType.getSupportedTypes(ColumnType.TEXT))
                .containsExactly(allExceptTimeFrame);
    }
}
