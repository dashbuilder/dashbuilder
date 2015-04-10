package org.dashbuilder.dataset;

import org.apache.commons.io.IOUtils;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.backend.DataSetDefJSONMarshaller;
import org.dashbuilder.dataset.def.BeanDataSetDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.dashbuilder.test.ShrinkWrapHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

@RunWith(Arquillian.class)
public class DataSetDefJSONMarshallerTest {

    private static final String BEAN_JSON_PATH = "beanJSONMarshalling.dset";
    private static final String UTF_8 = "UTF-8";

    @Deployment
    public static Archive<?> createTestArchive()  {
        return ShrinkWrapHelper.createJavaArchive()
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    DataSetManager dataSetManager;

    @Inject
    DataSetFormatter dataSetFormatter;

    @Inject
    DataSetDefRegistry dataSetDefRegistry;
    
    @Inject
    DataSetDefJSONMarshaller dataSetDefJSONMarshaller;
    
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testBean() throws Exception {
        final BeanDataSetDef dataSetDef = new BeanDataSetDef();
        dataSetDef.setName("data set name");
        dataSetDef.setUUID("test-uuid");
        dataSetDef.setProvider(DataSetProviderType.BEAN);
        dataSetDef.setCacheEnabled(false);
        dataSetDef.setCacheMaxRows(100);
        dataSetDef.setPublic(true);
        dataSetDef.setPushEnabled(false);
        dataSetDef.setPushMaxSize(10);
        dataSetDef.setRefreshAlways(false);
        dataSetDef.setRefreshTime("1second");
        // TODO: dataSetDef.setDataSetFilter();
        dataSetDef.setGeneratorClass("org.dashbuilder.DataSetGenerator");
        final Map<String, String> parameterMap = new LinkedHashMap<String, String>();
        parameterMap.put("p1", "v1");
        parameterMap.put("p2", "v2");
        dataSetDef.setParamaterMap(parameterMap);
        // TODO: Columns.
        
        String json = dataSetDefJSONMarshaller.toJsonString(dataSetDef);
        String beanJSONContent = getFileAsString(BEAN_JSON_PATH);

        assertDataSetDef(json, beanJSONContent);
        System.out.println(json);
        System.out.println(beanJSONContent);
    }
    
    // TODO: Tests for CSV, SQL and EL.
    
    private void assertDataSetDef(final String def1, final String def2) throws Exception {
        if (def1 == null && def2 != null) Assert.assertTrue("JSON string for Def1 is null and for Def2 is not null", false);
        if (def1 != null && def2 == null) Assert.assertTrue("JSON string for Def1 is not null and for Def2 is null", false);
        if (def1 == null) Assert.assertTrue("JSON string for both definitions is null", false);

        DataSetDef def1Object = dataSetDefJSONMarshaller.fromJson(def1);
        DataSetDef def2Object = dataSetDefJSONMarshaller.fromJson(def2);
        
        Assert.assertEquals(def1Object, def2Object);
    }

    protected static String getFileAsString(String file) throws Exception {
        InputStream mappingsFileUrl = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
        StringWriter writer = null;
        String fileContent = null;

        try {
            writer = new StringWriter();
            IOUtils.copy(mappingsFileUrl, writer, UTF_8);
            fileContent = writer.toString();
        } finally {
            if (writer != null) writer.close();
        }

        // Ensure newline characters meet the HTTP specification formatting requirements.
        return fileContent.replaceAll("\n","\r\n");
    }
}
