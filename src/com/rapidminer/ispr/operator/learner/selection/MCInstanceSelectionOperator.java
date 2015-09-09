/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection;

import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.loss.ILossFunction;
import com.rapidminer.ispr.operator.learner.loss.LossFunctionHelper;
import com.rapidminer.ispr.operator.learner.selection.models.AbstractInstanceSelectorModel;
import com.rapidminer.ispr.operator.learner.selection.models.MCInstanceSelectionModel;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.ispr.operator.learner.tools.genetic.RMRandomGenerator;
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
public class MCInstanceSelectionOperator extends AbstractInstanceSelectorOperator {

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
    public MCInstanceSelectionOperator(OperatorDescription description) {
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
        int p = getParameterAsInt(PARAMETER_PROTOTYPES_NUMBER);
        int s = getParameterAsInt(PARAMETER_ITERATION_NUMBER);
        RandomGenerator randomGenerator = RandomGenerator.getRandomGenerator(this);
        ILossFunction loss = LossFunctionHelper.getConfiguredLoss(this);
        AbstractInstanceSelectorModel m = new MCInstanceSelectionModel(measure, p, s, new RMRandomGenerator(randomGenerator), loss);
        DataIndex index = m.selectInstances(exampleSet);
        sampleSize = index.getLength();
        return index;
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
        return false;
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
