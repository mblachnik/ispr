/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.loss;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.operator.OperatorCapability;

/**
 *
 * @author Marcin
 */
public interface ILossFunction extends Cloneable{
    public void init(ExampleSet exampleSet);
    public void init(ISPRGeometricDataCollection<Number> samples);
    public double getLoss(double real, double predicted, double[] values);
    public double getLoss(double real, double predicted, Example values);
    public String getLossName();
    public String getLossDescription();
    public void setThreshold(double threshold);
    //public HashMap<String,>
    public boolean supportedLabelTypes(OperatorCapability capabilities);
}
