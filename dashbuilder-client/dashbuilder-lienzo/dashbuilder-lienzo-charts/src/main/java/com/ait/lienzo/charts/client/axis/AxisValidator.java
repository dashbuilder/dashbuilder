package com.ait.lienzo.charts.client.axis;

import com.ait.lienzo.client.core.shape.json.validators.NumberValidator;
import com.ait.lienzo.client.core.shape.json.validators.ObjectValidator;
import com.ait.lienzo.client.core.shape.json.validators.StringValidator;

/**
 * <p>Common validations for a generic chart axis.</p> 
 */
public class AxisValidator extends ObjectValidator {

    public static final AxisValidator INSTANCE = new AxisValidator();

    public AxisValidator()
    {
        super("chartAxis");

        addAttribute("title", StringValidator.INSTANCE, true);
        addAttribute("format", StringValidator.INSTANCE, false);
        addAttribute("size", NumberValidator.INSTANCE, false);
        // TODO
    }
}
