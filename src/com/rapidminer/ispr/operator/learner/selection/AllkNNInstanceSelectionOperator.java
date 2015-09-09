package com.rapidminer.ispr.operator.learner.selection;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.loss.ILossFunction;
import com.rapidminer.ispr.operator.learner.loss.LossFunctionHelper;
import com.rapidminer.ispr.operator.learner.selection.models.AllKNNInstanceSelectionGeneralModel;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import java.util.List;

/**
 *
 * @author Marcin
 */
public class AllkNNInstanceSelectionOperator extends AbstractInstanceSelectorOperator {

    /**
     * The parameter name for &quot;The used number of nearest neighbors.&quot;
     */
    public static final String PARAMETER_ADD_WEIGHTS = "k_start";
    /**
     *
     */
    public static final String PARAMETER_K_STOP = "k_stop";

    /**
     *
     * @param description
     */
    public AllkNNInstanceSelectionOperator(OperatorDescription description) {
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
        int k1 = getParameterAsInt(PARAMETER_ADD_WEIGHTS);
        int k2 = getParameterAsInt(PARAMETER_K_STOP);
        if (k1 > k2) {
            int tmp = k1;
            k1 = k2;
            k2 = tmp;
        }        
        ILossFunction loss = null;        
        Attribute labelAttribute = exampleSet.getAttributes().getLabel();
        loss = LossFunctionHelper.getConfiguredLoss(this);
        AllKNNInstanceSelectionGeneralModel m = new AllKNNInstanceSelectionGeneralModel(measure, k1, k2, loss);
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

        ParameterType typeK1 = new ParameterTypeInt(PARAMETER_ADD_WEIGHTS, "The lower number of nearest neighbors.", 1, Integer.MAX_VALUE, 3);
        typeK1.setExpert(false);
        types.add(typeK1);

        ParameterType typeK2 = new ParameterTypeInt(PARAMETER_K_STOP, "The higher number of nearest neighbors.", 1, Integer.MAX_VALUE, 5);
        typeK2.setExpert(false);
        types.add(typeK2);        
        
        types.addAll(LossFunctionHelper.getLossParameters(this));
                       
        return types;
    }
}
