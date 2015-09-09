/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.loss;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.operator.OperatorCapability;

/**
 *
 * @author Marcin
 */
public class ThresholdLinearLoss implements ILossFunction {
    
    private double threshold = 0;    

    public ThresholdLinearLoss() {
    }

    @Override
    public void init(ExampleSet exampleSet) {
    }

    @Override
    public void init(ISPRGeometricDataCollection<Number> samples){                
    }
            
    @Override
    public double getLoss(double real, double predicted, double[] values) {
        double value = Math.abs(real - predicted) > threshold ? 1 : 0;
        return value;
    }
    
    @Override
    public double getLoss(double real, double predicted, Example example){
        double[] values = new double[example.getAttributes().size()];
        KNNTools.extractExampleValues(example, values);
        return getLoss(real, predicted, values);
    }
    
    @Override
    public void setThreshold(double threshold){
        this.threshold = threshold;
    }

    @Override
    public String getLossName() {
        return "ThresholdLinearLoss";
    }

    @Override
    public String getLossDescription() {
        return "Y=(R-P) > Thres";
    }
    
    @Override
    public boolean supportedLabelTypes(OperatorCapability capabilities){
        switch (capabilities){
            case POLYNOMINAL_LABEL:
            case BINOMINAL_LABEL:
            case NUMERICAL_LABEL:
                return true;
        }
        return false;
    }
}
