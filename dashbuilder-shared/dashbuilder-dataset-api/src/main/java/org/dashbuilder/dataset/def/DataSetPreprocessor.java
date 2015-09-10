/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dashbuilder.dataset.def;

import org.dashbuilder.dataset.DataSetLookup;

/**
 * A generic interface to declare extra pre processing on the backend for DataSets 
 * 
 */
public interface DataSetPreprocessor {
    public void preprocess(DataSetLookup lookup);
}
