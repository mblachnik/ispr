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
public class ClassLoss implements ILossFunction {
    double threshold = 0;

    @Override
    public void init(ExampleSet exampleSet) {
        
    }
    
    @Override
    public void init(ISPRGeometricDataCollection<Number> samples){
        
    }
    
    @Override
    public void setThreshold(double threshold){
        this.threshold = threshold;
    }

    @Override
    public double getLoss(double real, double predicted, double[] values){
        return  real == predicted ? 0 : 1;
    }
    
    @Override
    public double getLoss(double real, double predicted, Example example){    
        double[] values = null;
        return getLoss(real, predicted, values);
    }
    
    @Override
    public String getLossName() {
        return "Class loss";
    }

    @Override
    public String getLossDescription() {
        return "R == P ? 0 : 1";
    }
    
    @Override
    public boolean supportedLabelTypes(OperatorCapability capabilities){
        switch (capabilities){
            case POLYNOMINAL_LABEL:
            case BINOMINAL_LABEL:
                return true;
        }
        return false;
    }
}
