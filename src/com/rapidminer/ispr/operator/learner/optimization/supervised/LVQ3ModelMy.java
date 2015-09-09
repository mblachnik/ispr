package com.rapidminer.ispr.operator.learner.optimization.supervised;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 *
 * @author Marcin
 */
public class LVQ3ModelMy extends AbstractLVQModel {

    private final DistanceMeasure measure;
    private final int iterations;
    private int currentIteration;
    private double alphaPositive, alphaNegative;
    private final double initialAlphaPositive, initialAlphaNegative;
    private final double window;
    private final double epsilon;

    /**
     *
     * @param prototypes
     * @param iterations
     * @param measure
     * @param alphaPositive
     * @param alphaNegative
     * @param window
     * @param epsilon
     * @throws OperatorException
     */
    public LVQ3ModelMy(ExampleSet prototypes, int iterations,
            DistanceMeasure measure, double alphaPositive,
            double alphaNegative, double window, double epsilon) throws OperatorException {
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
        this.epsilon = epsilon;
    }

    /**
     *
     */
    @Override
    public void update() {
        double dist, minDistCorrect = Double.MAX_VALUE, minDistOther = Double.MAX_VALUE;
        int selectedPrototypeCorrect = 0;
        int selectedPrototypeOther = 0;
        double labelOther = -1;
        int i = 0;
        for (double[] prototype : prototypesValues) {
            dist = measure.calculateDistance(prototype, exampleValues);
            double protoLabel = prototypeLabels[i];
            if (dist < minDistCorrect && exampleLabel == protoLabel) {
                minDistCorrect = dist;
                selectedPrototypeCorrect = i;
            } else if (dist < minDistOther) {
                minDistOther = dist;
                selectedPrototypeOther = i;
                labelOther = prototypeLabels[selectedPrototypeOther];
            }
            i++;
        }

        if (labelOther != exampleLabel) {
            double threshold = Math.min(minDistCorrect / minDistOther, minDistOther / minDistCorrect);
            if (threshold > window) {
                for (i = 0; i < getAttributesSize(); i++) {
                    double trainValue = exampleValues[i];
                    double value1 = prototypesValues[selectedPrototypeCorrect][i];
                    double value2 = prototypesValues[selectedPrototypeOther][i];
                    value1 += alphaPositive * (trainValue - value1);
                    value2 -= alphaNegative * (trainValue - value2);
                    prototypesValues[selectedPrototypeCorrect][i] = value1;
                    prototypesValues[selectedPrototypeOther][i] = value2;
                }
            }
        } else {
            for (i = 0; i < getAttributesSize(); i++) {
                double trainValue = exampleValues[i];
                double value1 = prototypesValues[selectedPrototypeCorrect][i];
                double value2 = prototypesValues[selectedPrototypeOther][i];
                value1 += epsilon * alphaPositive * (trainValue - value1);
                value2 += epsilon * alphaPositive * (trainValue - value2);
                prototypesValues[selectedPrototypeCorrect][i] = value1;
                prototypesValues[selectedPrototypeOther][i] = value2;
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
