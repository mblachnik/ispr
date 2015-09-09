package com.rapidminer.ispr.operator.learner.optimization.supervised;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 *
 * @author Marcin
 */
public class LVQ2Model extends AbstractLVQModel {

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
    public LVQ2Model(ExampleSet prototypes, int iterations,
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
        double dist, minDist1 = Double.MAX_VALUE, minDist2 = Double.MAX_VALUE;
        int selectedPrototypeNr1 = 0;
        int selectedPrototypeNr2 = 0;
        int i = 0;
        for (double[] prototype : prototypesValues) {
            dist = measure.calculateDistance(prototype, exampleValues);
            if (dist < minDist1) {
                minDist2 = minDist1;
                minDist1 = dist;
                selectedPrototypeNr2 = selectedPrototypeNr1;
                selectedPrototypeNr1 = i;
            } else if (dist < minDist2) {
                minDist2 = dist;
                selectedPrototypeNr2 = i;
            }
            i++;
        }

        double labelExa = exampleLabel;
        double labelProto1 = prototypeLabels[selectedPrototypeNr1];
        double labelProto2 = prototypeLabels[selectedPrototypeNr2];

        if (labelProto1 != labelProto2) {
            if ((labelProto1 == labelExa) || (labelProto2 == labelExa)) {
                if (labelProto2 == labelExa) {
                    int prototype = selectedPrototypeNr1;
                    selectedPrototypeNr1 = selectedPrototypeNr2;
                    selectedPrototypeNr2 = prototype;
                }
                for (i = 0; i < getAttributesSize(); i++) {
                    double trainValue = exampleValues[i];
                    double value1 = prototypesValues[selectedPrototypeNr1][i];
                    double value2 = prototypesValues[selectedPrototypeNr2][i];
                    value1 += alphaPositive * (trainValue - value1);
                    value2 -= alphaNegative * (trainValue - value2);
                    prototypesValues[selectedPrototypeNr1][i] = value1;
                    prototypesValues[selectedPrototypeNr2][i] = value2;
                }
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
