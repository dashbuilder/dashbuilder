/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashbuilder.dataset;

import org.dashbuilder.dataset.def.DataSetPreprocessor;
import static org.dashbuilder.dataset.filter.FilterFactory.notEqualsTo;

/**
 *
 * @author salaboy
 */
public class CityFilterDataSetPreprocessor implements DataSetPreprocessor {
    
    private String filteredCity;

    public CityFilterDataSetPreprocessor(String filteredCity) {
        this.filteredCity = filteredCity;
    }
    
 
    @Override
    public void preprocess(DataSetLookup lookup) {
        lookup.getFirstFilterOp().addFilterColumn(notEqualsTo("city", filteredCity));
    }
    
}
