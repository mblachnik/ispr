package com.rapidminer.ispr.operator.learner.optimization.supervised;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 *
 * @author Marcin
 */
public class LVQ2ModelMy extends AbstractLVQModel {

    private final DistanceMeasure measure;
    private final int iterations;
    private int currentIteration;
    private double alphaPositive, alphaNegative;
    private final double initialAlphaPositive, initialAlphaNegative;

    /**
     *
     * @param prototypes
     * @param iterations
     * @param measure
     * @param alphaPositive
     * @param alphaNegative
     * @throws OperatorException
     */
    public LVQ2ModelMy(ExampleSet prototypes, int iterations,
            DistanceMeasure measure, double alphaPositive,
            double alphaNegative) throws OperatorException {
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
        double dist, minDistCorrect = Double.MAX_VALUE, minDistIncorrect = Double.MAX_VALUE;
        int selectedPrototypeCorrect = 0;
        int selectedPrototypeIncorrect = 0;
        int i = 0;

        for (double[] prototype : prototypesValues) {
            dist = measure.calculateDistance(prototype, exampleValues);
            double protoLabel = prototypeLabels[i];
            if (dist < minDistCorrect && exampleLabel == protoLabel) {
                minDistCorrect = dist;
                selectedPrototypeCorrect = i;
            }
            if (dist < minDistIncorrect && exampleLabel != protoLabel) {
                minDistIncorrect = dist;
                selectedPrototypeIncorrect = i;
            }
            i++;
        }

        for (i = 0; i < getAttributesSize(); i++) {
            double trainValue = exampleValues[i];
            double valueCorrect = prototypesValues[selectedPrototypeCorrect][i];
            double valueIncorrect = prototypesValues[selectedPrototypeIncorrect][i];
            valueCorrect += alphaPositive * (trainValue - valueCorrect);
            valueIncorrect -= alphaNegative * (trainValue - valueIncorrect);
            prototypesValues[selectedPrototypeCorrect][i] = valueCorrect;
            prototypesValues[selectedPrototypeIncorrect][i] = valueIncorrect;
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
