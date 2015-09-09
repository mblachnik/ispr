package com.rapidminer.ispr.operator.learner.selection;

import com.rapidminer.ispr.operator.learner.selection.models.RENNInstanceSelectionModel;
import java.util.List;

import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.loss.ILossFunction;
import com.rapidminer.ispr.operator.learner.loss.LossFunctionHelper;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.operator.OperatorCapability;
import static com.rapidminer.operator.OperatorCapability.NUMERICAL_LABEL;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasures;

/**
 * 
 * @author Marcin
 */
public class RENNInstanceSelectionOperator extends AbstractInstanceSelectorOperator {

    /**
     * The parameter name for &quot;The used number of nearest neighbors.&quot;
     */
    public static final String PARAMETER_K = "k";    

    /**
     * 
     * @param description
     */
    public RENNInstanceSelectionOperator(OperatorDescription description) {
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
        int k = getParameterAsInt(PARAMETER_K);
        ILossFunction loss = LossFunctionHelper.getConfiguredLoss(this);
        RENNInstanceSelectionModel m = new RENNInstanceSelectionModel(measure, k, loss);
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
            case NUMERICAL_LABEL:
            case MISSING_VALUES:
                return true;
            default:
                return false;
        }
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        ParameterType type = new ParameterTypeInt(PARAMETER_K, "The used number of nearest neighbors.", 3, Integer.MAX_VALUE, 3);
        type.setExpert(false);
        types.add(type);
        
        types.addAll(LossFunctionHelper.getLossParameters(this));
        
        return types;
    }
}
