/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection;

import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.loss.ILossFunction;
import com.rapidminer.ispr.operator.learner.loss.LossFunctionHelper;
import com.rapidminer.ispr.operator.learner.selection.models.RMHCNaiveInstanceSelectionGeneralModel;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import java.util.List;

/**
 *
 * @author Marcin
 */
public class RMHCInstanceSelectionOperator extends AbstractInstanceSelectorOperator {

    /**
     * The parameter name for &quot;The used number of nearest neighbors.&quot;
     */
    public static final String PARAMETER_PROTOTYPES_NUMBER = "Number of prototypes";
    /**
     * 
     */
    public static final String PARAMETER_ITERATION_NUMBER = "Number of iterations";    

    /**
     * 
     * @param description
     */
    public RMHCInstanceSelectionOperator(OperatorDescription description) {
        super(description);        
    }

    /**
     * 
     * @param exampleSet
     * @return
     * @throws OperatorException
     */
    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) throws OperatorException {
        //INITIALIZATION
        DistanceMeasure measure = measureHelper.getInitializedMeasure(exampleSet);
        int numberOfPrototypes = getParameterAsInt(PARAMETER_PROTOTYPES_NUMBER);
        int numberOfIterations = getParameterAsInt(PARAMETER_ITERATION_NUMBER);
        RandomGenerator randomGenerator = RandomGenerator.getRandomGenerator(this);
        ILossFunction loss = LossFunctionHelper.getConfiguredLoss(this);
        RMHCNaiveInstanceSelectionGeneralModel m = new RMHCNaiveInstanceSelectionGeneralModel(measure, numberOfPrototypes, numberOfIterations, randomGenerator,loss);
        //com.rapidminer.ispr.operator.learner.tools.genetic.RandomGenerator rg = new com.rapidminer.ispr.operator.learner.tools.genetic.RMRandomGenerator(randomGenerator);
        //RMHCInstanceSelectionModel m = new RMHCInstanceSelectionModel(measure, numberOfPrototypes, numberOfIterations, rg,loss);
        DataIndex output = m.selectInstances(exampleSet);
        sampleSize = output.getLength();
        return output;
    }

    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        int measureType = DistanceMeasures.MIXED_MEASURES_TYPE;
        try {
            measureType = measureHelper.getSelectedMeasureType();
        } catch (Exception e) {
        }
        switch (capability) {
            case BINOMINAL_ATTRIBUTES:
            case POLYNOMINAL_ATTRIBUTES:
                return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                        || (measureType == DistanceMeasures.NOMINAL_MEASURES_TYPE);
            case NUMERICAL_ATTRIBUTES:
                return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                        || (measureType == DistanceMeasures.DIVERGENCES_TYPE)
                        || (measureType == DistanceMeasures.NUMERICAL_MEASURES_TYPE);
            case POLYNOMINAL_LABEL:
            case BINOMINAL_LABEL:
            case MISSING_VALUES:
                return true;
            default:
                return false;
        }
    }
    
    @Override
    boolean getSampleRandomizeOption() {
        return true;
    }
    
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        ParameterType type = new ParameterTypeInt(PARAMETER_PROTOTYPES_NUMBER, "Size of the population", 3, Integer.MAX_VALUE, 3);
        type.setExpert(false);
        types.add(type);
        type = new ParameterTypeInt(PARAMETER_ITERATION_NUMBER, "The number of iterations", 1, Integer.MAX_VALUE, 100);
        type.setExpert(false);
        types.add(type);        
        types.addAll(LossFunctionHelper.getLossParameters(this));
        return types;
    }
}
