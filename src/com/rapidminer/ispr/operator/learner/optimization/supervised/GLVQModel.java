package com.rapidminer.ispr.operator.learner.optimization.supervised;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.operator.learner.tools.BasicMath;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 *
 * @author Marcin
 */
public class GLVQModel extends AbstractLVQModel {

    private final DistanceMeasure measure; //Distance measure
    private final int iterations; //TOtal number of iterations
    private int currentIteration; //Iteration id
    private double alphaPositive; //Learning rate
    private double alphaNegative; //Learning rate
    private double time; //Time used in sigmoidal cost function evaluation
    private final double initialAlphaPositive; // Initial value of alpha, here alpha is not changing    
    private final double initialAlphaNegative; // Initial value of alpha, here alpha is not changing    
           
    /**
     *
     * @param prototypes
     * @param iterations
     * @param measure
     * @param alphaPositive     
     * @throws OperatorException
     */
    public GLVQModel(ExampleSet prototypes, int iterations,
            DistanceMeasure measure, double alphaPositive, double alphaNegative) throws OperatorException {
        super(prototypes);        
        this.iterations = iterations;
        this.currentIteration = 0;        
        this.alphaPositive = alphaPositive;     
        this.alphaNegative = alphaNegative;     
        this.time = 1;
        this.initialAlphaPositive = alphaPositive;
        this.initialAlphaNegative = alphaNegative;
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

        double denominator =  minDistCorrect + minDistIncorrect;        
        double mu   = (minDistCorrect - minDistIncorrect) / denominator;        
        double costFunVal = BasicMath.sigmoid(mu * time); 
        double dif_df_mu = costFunVal * (1 - costFunVal);        
        double factorCorrect   = minDistIncorrect / (denominator * denominator);        
        double factorIncorrect = minDistCorrect   / (denominator * denominator);
                 
        for (i = 0; i < getAttributesSize(); i++) {
            double trainValue = exampleValues[i];
            double valueCorrect = prototypesValues[selectedPrototypeCorrect][i];
            double valueIncorrect = prototypesValues[selectedPrototypeIncorrect][i];
            valueCorrect   += alphaPositive * dif_df_mu * factorCorrect   * (trainValue - valueCorrect);
            valueIncorrect -= alphaNegative * dif_df_mu * factorIncorrect * (trainValue - valueIncorrect);
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
        time *= 1.1;
        //alphaPositive = learingRateUpdateRule(alphaPositive, currentIteration, iterations, initialAlphaPositive);                
        //alphaNegative = learingRateUpdateRule(alphaNegative, currentIteration, iterations, initialAlphaNegative);                
        return currentIteration < iterations;
    }
}
