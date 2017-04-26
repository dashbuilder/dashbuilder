package org.dashbuilder.dataprovider.backend.elasticsearch;

import org.dashbuilder.dataset.DataSetFilterTest;
import org.dashbuilder.dataset.DataSetGroupTest;
import org.dashbuilder.dataset.DataSetNestedGroupTest;
import org.dashbuilder.dataset.DataSetTrimTest;
import org.junit.Before;
import org.junit.Test;

/**
 * This test case delegates to the common tests from data set core module.
 */
public class ElasticSearchCommonTests extends ElasticSearchDataSetTestBase {

    protected static final String EL_EXAMPLE_DATASET_DEF = "org/dashbuilder/dataprovider/backend/elasticsearch/expensereports.dset";
    
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Register the data set definition for expense reports index.
        _registerDataSet(EL_EXAMPLE_DATASET_DEF);
    }
    
    @Test
    public void testTrim() throws Exception {
        DataSetTrimTest subTest = new DataSetTrimTest();
        subTest.testTrim();
        subTest.testTrimGroup();
        subTest.testDuplicatedColumns();
    }

    @Test
    public void testDataSetGroup() throws Exception {
        DataSetGroupTest subTest = new DataSetGroupTest();
        subTest.testDataSetFunctions();
        // Not supported - subTest.testDateMinMaxFunctions();
        subTest.testNumberMinMaxFunctions();
        subTest.testGroupByLabelDynamic();
        subTest.testGroupByYearDynamic();
        subTest.testGroupByMonthDynamic();
        subTest.testGroupByMonthDynamicNonEmpty();
        subTest.testGroupByDayOfWeekDynamic();
        subTest.testGroupByDayOfWeekFixed();
        subTest.testGroupByMonthReverse();
        subTest.testGroupByMonthFixed();
        subTest.testGroupByMonthFirstMonth();
        subTest.testGroupByMonthFirstMonthReverse();
        subTest.testGroupByQuarter();
        subTest.testGroupByDateOneRow();
        subTest.testGroupByDateOneDay();
        subTest.testGroupAndCountSameColumn();
        // Not supported - subTest.testGroupNumberAsLabel();
    }

    @Test
    public void testDataSetNestedGroup() throws Exception {
        DataSetNestedGroupTest subTest = new DataSetNestedGroupTest();
        subTest.testGroupSelectionFilter();
        // Not supported - subTest.testNestedGroupFromMultipleSelection();
        // Not supported - subTest.testNestedGroupRequiresSelection();
        // Not supported - subTest.testThreeNestedLevels();
        // Not supported - subTest.testNoResultsSelection();
    }

    @Test
    public void testDataSetFilter() throws Exception {
        DataSetFilterTest subTest = new DataSetFilterTest();
        subTest.testColumnTypes();
        subTest.testFilterByString();
        subTest.testFilterByDate();
        subTest.testFilterByNumber();
        subTest.testFilterMultiple();
        subTest.testFilterUntilToday();
        subTest.testANDExpression();
        subTest.testNOTExpression();
        subTest.testORExpression();
        subTest.testORExpressionMultilple();
        subTest.testLogicalExprNonEmpty();
        subTest.testCombinedExpression();
        subTest.testCombinedExpression2();
        subTest.testCombinedExpression3();
        subTest.testInOperator();
        subTest.testNotInOperator();
        // Like TO operations are tested in ElasticSearchDataSetTest, as the results depend on the core type and string analyzer used for that column.
    }
}
