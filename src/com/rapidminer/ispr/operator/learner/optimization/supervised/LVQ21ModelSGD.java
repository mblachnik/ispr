package com.rapidminer.ispr.operator.learner.optimization.supervised;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 *
 * @author Marcin
 */
public class LVQ21ModelSGD extends AbstractLVQModel {

    private final DistanceMeasure measure;
    private final int iterations;
    private int currentIteration;
    private double alphaPositive, alphaNegative;
    private final double initialAlphaPositive, initialAlphaNegative;
    private final double window;

    /**
     *
     * @param prototypes
     * @param iterations
     * @param measure
     * @param alphaPositive
     * @param alphaNegative
     * @param window
     * @throws OperatorException
     */
    public LVQ21ModelSGD(ExampleSet prototypes, int iterations,
            DistanceMeasure measure, double alphaPositive,
            double alphaNegative, double window) throws OperatorException {
        super(prototypes);
        this.iterations = iterations;
        this.currentIteration = 0;
        this.alphaNegative = alphaNegative;
        this.alphaPositive = alphaPositive;
        this.initialAlphaNegative = alphaNegative;
        this.initialAlphaPositive = alphaPositive;
        this.measure = measure;
        this.measure.init(prototypes);
        this.window = (1 - window) / (1 + window);
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
        
        double threshold = Math.min(minDistCorrect / minDistIncorrect, minDistIncorrect / minDistCorrect);
        
        if ( threshold > window) {
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
