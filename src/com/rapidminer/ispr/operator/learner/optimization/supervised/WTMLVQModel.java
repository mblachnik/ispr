/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.optimization.supervised;

import com.rapidminer.example.ExampleSet;
import static com.rapidminer.ispr.operator.learner.optimization.supervised.AbstractLVQModel.learingRateUpdateRule;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.HashMap;

/**
 *
 * @author Marcin
 */
public class WTMLVQModel extends AbstractLVQModel {

    private DistanceMeasure measure;
    private int currentIteration, iterations;
    private double alphaPositive, alphaNegative;
    private double initialAlphaPositive, initialAlphaNegative;
    private HashMap<Integer,int[]> prototypeNeighborMap;

    /**
     * 
     * @param prototypes
     * @param iterations
     * @param measure
     * @param alphaPositive
     * @param alphaNegative
     * @throws OperatorException
     */
    public WTMLVQModel(ExampleSet prototypes, int iterations, DistanceMeasure measure, double alphaPositive, double alphaNegative) throws OperatorException {
        super(prototypes);
        this.iterations = iterations;
        this.currentIteration = 0;
        this.alphaNegative = alphaNegative;
        this.alphaPositive = alphaPositive;
        this.initialAlphaNegative = alphaNegative;
        this.initialAlphaPositive = alphaPositive;
        this.measure = measure;
        this.measure.init(prototypes);
        this.prototypeNeighborMap = new HashMap<Integer,int[]>();         
    }

    /**
     * 
     */
    @Override
    public void update() {
        double dist, minDist = Double.MAX_VALUE;
        int selectedPrototype = 0;
        int i = 0;
        for (double[] prototype : prototypesValues) {
            dist = measure.calculateDistance(prototype, exampleValues);
            if (dist < minDist) {
                minDist = dist;
                selectedPrototype = i;
            }
            i++;
        }

        if ((prototypeLabels[selectedPrototype] == exampleLabel) || (Double.isNaN(prototypeLabels[selectedPrototype]))) {
            for (i = 0; i < getAttributesSize(); i++) {
                double value = prototypesValues[selectedPrototype][i];
                value += alphaPositive * (exampleValues[i] - value);
                prototypesValues[selectedPrototype][i] = value;
            }
        } else {
            for (i = 0; i < getAttributesSize(); i++) {
                double value = prototypesValues[selectedPrototype][i];
                value -= alphaNegative * (exampleValues[i] - value);
                prototypesValues[selectedPrototype][i] = value;
            }
        }        
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean nextIteration() {
        currentIteration++;
        alphaPositive = learingRateUpdateRule(alphaPositive, currentIteration, iterations, initialAlphaPositive);
        alphaNegative = learingRateUpdateRule(alphaNegative, currentIteration, iterations, initialAlphaNegative);
        return currentIteration < iterations;
    }
}

