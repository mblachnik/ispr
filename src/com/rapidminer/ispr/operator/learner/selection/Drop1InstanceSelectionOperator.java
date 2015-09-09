package com.rapidminer.ispr.operator.learner.selection;

import com.rapidminer.ispr.operator.learner.selection.models.Drop1InstanceSelectionModel;

import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.ispr.operator.learner.selection.models.AbstractInstanceSelectorModel;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasures;

/**
 * 
 * @author Marcin
 */
public class Drop1InstanceSelectionOperator extends AbstractInstanceSelectorOperator {
    //private final CNNInstanceSelection cnnInstanceSelection;

    /**
     * 
     * @param description
     */
    public Drop1InstanceSelectionOperator(OperatorDescription description) {
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
        DistanceMeasure distance = measureHelper.getInitializedMeasure(exampleSet);        
        AbstractInstanceSelectorModel m = new Drop1InstanceSelectionModel(distance);
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
}
