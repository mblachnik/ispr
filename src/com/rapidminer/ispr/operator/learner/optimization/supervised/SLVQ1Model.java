package com.rapidminer.ispr.operator.learner.optimization.supervised;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 * 
 * @author Marcin
 */
public class SLVQ1Model extends AbstractLVQModel {

    private DistanceMeasure measure;
    private int currentIteration, iterations;
    private double alphaPositive, alphaNegative;
    private double initialAlphaPositive, initialAlphaNegative;

    /**
     * 
     * @param prototypes
     * @param iterations
     * @param measure
     * @param alphaPositive
     * @param alphaNegative
     * @throws OperatorException
     */
    public SLVQ1Model(ExampleSet prototypes, int iterations, DistanceMeasure measure, double alphaPositive, double alphaNegative) throws OperatorException {
        super(prototypes);
        this.iterations = iterations;
        this.currentIteration = 0;
        this.alphaNegative = alphaNegative;
        this.alphaPositive = alphaPositive;
        this.initialAlphaNegative = alphaNegative;
        this.initialAlphaPositive = alphaPositive;
        this.measure = measure;
        this.measure.init(prototypes);
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
