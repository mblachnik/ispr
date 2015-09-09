package com.rapidminer.ispr.operator.learner.selection;

import com.rapidminer.example.Attribute;
import com.rapidminer.ispr.operator.learner.selection.models.ENNInstanceSelectionModel;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.loss.ILossFunction;
import com.rapidminer.ispr.operator.learner.loss.LossFunctionHelper;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Marcin
 */
public class ENNInstanceSelectionOperator extends AbstractInstanceSelectorOperator {

    /**
     * The parameter name for &quot;The used number of nearest neighbors.&quot;
     */
    public static final String PARAMETER_K = "k";
    private DistanceMeasureHelper measureHelper;
    /**
     * The parameter name for &quot;The weights w for all classes (first column:
     * class name, second column: weight), i.e. set the parameters C of each
     * class w * C (empty: using 1 for all classes where the weight was not
     * defined).&quot;
     */
    public static final String PARAMETER_CLASS_WEIGHTS = "class_weights";

    /**
     *
     * @param description
     */
    public ENNInstanceSelectionOperator(OperatorDescription description) {
        super(description);        
        measureHelper = new DistanceMeasureHelper(this);
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
        Attribute labelAttribute = exampleSet.getAttributes().getLabel();
        double[] classWeight = null;
        ILossFunction loss = LossFunctionHelper.getConfiguredLoss(this);
        if (labelAttribute.isNominal()) {
            classWeight = new double[labelAttribute.getMapping().size()];
            for (int i = 0; i < classWeight.length; i++) {
                classWeight[i] = 1.0d;
            }
            if (isParameterSet(PARAMETER_CLASS_WEIGHTS)) {
                List<String[]> classWeights = getParameterList(PARAMETER_CLASS_WEIGHTS);
                Iterator<String[]> i = classWeights.iterator();
                while (i.hasNext()) {
                    String[] classWeightArray = i.next();
                    String className = classWeightArray[0];
                    double classWeightValue = Double.valueOf(classWeightArray[1]);
                    int index = labelAttribute.getMapping().getIndex(className);
                    if ((index >= 0) && (index < classWeight.length)) {
                        classWeight[index] = classWeightValue;
                    }
                }
            }
        }
        ENNInstanceSelectionModel m = new ENNInstanceSelectionModel(measure, k, loss, classWeight);
        DataIndex output = m.selectInstances(exampleSet);
        sampleSize = output.getLength();
        return output;
    }

    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        int measureType = DistanceMeasures.MIXED_MEASURES_TYPE;
        ILossFunction loss = null;
        try {
            measureType = measureHelper.getSelectedMeasureType();
            loss = LossFunctionHelper.getConfiguredLoss(this); //This is in this kind of block in case it wont be possible to access LoossFunctionHelper           
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
                if (loss != null) {
                    if (!loss.supportedLabelTypes(capability)) {
                        return false;
                    }
                }
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

        ParameterType type = new ParameterTypeInt(PARAMETER_K, "The used number of nearest neighbors.", 3, Integer.MAX_VALUE, 3);
        type.setExpert(false);
        types.add(type);

        types.addAll(LossFunctionHelper.getLossParameters(this));

        types.add(new ParameterTypeList(PARAMETER_CLASS_WEIGHTS, "The weights w for all classes (first column: class name, second column: weight), i.e. set the parameters C of each class w * C (empty: using 1 for all classes where the weight was not defined).", new ParameterTypeString("class_name", "The class name."), new ParameterTypeDouble("weight",
                "The weight for this class.", 0.0d, Double.POSITIVE_INFINITY, 1.0d)));
        return types;
    }
}
