/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.weka;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.selection.AbstractInstanceSelectorOperator;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.conditions.EqualTypeCondition;
import com.rapidminer.tools.WekaInstancesAdaptor;
import com.rapidminer.tools.WekaTools;
import java.util.List;
import java.util.Vector;
import main.core.algorithm.Algorithm;
import main.core.algorithm.BSEAlgorithm;
import main.core.algorithm.CNNAlgorithm;
import main.core.algorithm.CNNRegAlgorithm;
import main.core.algorithm.DROP1Algorithm;
import main.core.algorithm.DROP2Algorithm;
import main.core.algorithm.DROP3Algorithm;
import main.core.algorithm.DROP4Algorithm;
import main.core.algorithm.DROP5Algorithm;
import main.core.algorithm.ENNAlgorithm;
import main.core.algorithm.ENNRegAlgorithm;
import main.core.algorithm.HMNEAlgorithm;
import main.core.algorithm.ICFAlgorithm;
import main.core.algorithm.ICFRegAlgorithm;
import main.core.algorithm.MIAlgorithm;
import main.core.algorithm.MSSAlgorithm;
import main.core.algorithm.MSSRegAlgorithm;
import main.core.algorithm.RNNAlgorithm;
import main.core.exception.NotEnoughInstancesException;
import weka.core.Instances;

/**
 *
 * @author Marcin
 */
public class WekaISOperator extends AbstractInstanceSelectorOperator {

    public static final String PARAMETER_NEAREST_NEIGHBORS = "k";
    public static final String PARAMETER_THRESHOLD = "Threshold";

    public WekaISOperator(OperatorDescription description) {
        super(description);
    }

    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) throws OperatorException {
        try {
            Instances instances = WekaTools.toWekaInstances(exampleSet, "LearningInstances", WekaInstancesAdaptor.LEARNING);
            int intType = getParameterAsInt(WekaISAlgorithms.PARAMETER_IS_ALGORITHM);
            Algorithm isAlgorithm = null;
            WekaISAlgorithms type = WekaISAlgorithms.valueOf(WekaISAlgorithms.IS_ALGORITHM_TYPES()[intType]);
            int k = 1;
            double threshold = 0.1;
            Attribute labelAttribute = exampleSet.getAttributes().getLabel();
            if (labelAttribute.isNominal()) {
                switch (type) {
                    case ENN:
                        isAlgorithm = new ENNAlgorithm(instances);
                        k = getParameterAsInt(PARAMETER_NEAREST_NEIGHBORS);
                        ((ENNAlgorithm) isAlgorithm).setNumOfNearestNeighbour(k);
                        break;
                    case BSE:
                        isAlgorithm = new BSEAlgorithm(instances);
                        k = getParameterAsInt(PARAMETER_NEAREST_NEIGHBORS);
                        ((BSEAlgorithm) isAlgorithm).setNumOfNearestNeighbour(k);
                        break;
                    case ICF:
                        isAlgorithm = new ICFAlgorithm(instances);
                        k = getParameterAsInt(PARAMETER_NEAREST_NEIGHBORS);
                        ((ICFAlgorithm) isAlgorithm).setNumOfNearestNeighbour(k);
                        break;
                    case DROP1:
                        isAlgorithm = new DROP1Algorithm(instances);
                        k = getParameterAsInt(PARAMETER_NEAREST_NEIGHBORS);
                        ((DROP1Algorithm) isAlgorithm).setNumOfNearestNeighbour(k);
                        break;
                    case DROP2:
                        isAlgorithm = new DROP2Algorithm(instances);
                        k = getParameterAsInt(PARAMETER_NEAREST_NEIGHBORS);
                        ((DROP2Algorithm) isAlgorithm).setNumOfNearestNeighbour(k);
                        break;
                    case DROP3:
                        isAlgorithm = new DROP3Algorithm(instances);
                        k = getParameterAsInt(PARAMETER_NEAREST_NEIGHBORS);
                        ((DROP3Algorithm) isAlgorithm).setNumOfNearestNeighbour(k);
                        break;
                    case DROP4:
                        isAlgorithm = new DROP4Algorithm(instances);
                        k = getParameterAsInt(PARAMETER_NEAREST_NEIGHBORS);
                        ((DROP4Algorithm) isAlgorithm).setNumOfNearestNeighbour(k);
                        break;
                    case DROP5:
                        isAlgorithm = new DROP5Algorithm(instances);
                        k = getParameterAsInt(PARAMETER_NEAREST_NEIGHBORS);
                        ((DROP5Algorithm) isAlgorithm).setNumOfNearestNeighbour(k);
                        break;
                    case CNN:
                        isAlgorithm = new CNNAlgorithm(instances);
                        break;
                    case HMNE:
                        isAlgorithm = new HMNEAlgorithm(instances);
                        break;
                    case MI:
                        isAlgorithm = new MIAlgorithm(instances);
                        break;
                    case MSS:
                        isAlgorithm = new MSSAlgorithm(instances);
                        break;
                    case RNN:
                        isAlgorithm = new RNNAlgorithm(instances);
                        break;
                    default:
                        throw new UserError(this, "error.weka.nominalLabel");

                }
            } else if (labelAttribute.isNumerical()) {
                switch (type) {
                    case ENN_REG:
                        ENNRegAlgorithm ennRegIsAlgorithm = new ENNRegAlgorithm(instances);
                        k = getParameterAsInt(PARAMETER_NEAREST_NEIGHBORS);
                        ennRegIsAlgorithm.setNumOfNearestNeighbour(k);
                        threshold = getParameterAsDouble(PARAMETER_THRESHOLD);
                        ennRegIsAlgorithm.setAlpha(threshold);
                        isAlgorithm = ennRegIsAlgorithm;
                        break;
                    case CNN_REG:
                        CNNRegAlgorithm cnnRegIsAlgorithm = new CNNRegAlgorithm(instances);
                        threshold = getParameterAsDouble(PARAMETER_THRESHOLD);
                        cnnRegIsAlgorithm.setAlpha(threshold);
                        isAlgorithm = cnnRegIsAlgorithm;
                        break;
                    case ICF_REG:
                        ICFRegAlgorithm icfRegIsAlgorithm = new ICFRegAlgorithm(instances);
                        k = getParameterAsInt(PARAMETER_NEAREST_NEIGHBORS);
                        icfRegIsAlgorithm.setNumOfNearestNeighbour(k);
                        threshold = getParameterAsDouble(PARAMETER_THRESHOLD);
                        icfRegIsAlgorithm.setAlpha(threshold);
                        isAlgorithm = icfRegIsAlgorithm;
                        break;
                    case MSS_REG:
                        MSSRegAlgorithm mssRegIsAlgorithm = new MSSRegAlgorithm(instances);
                        threshold = getParameterAsDouble(PARAMETER_THRESHOLD);
                        mssRegIsAlgorithm.setAlpha(threshold);
                        isAlgorithm = mssRegIsAlgorithm;
                        break;
                    default:
                        throw new UserError(this, "error.weka.numericLabel");
                }
            } else {
                throw new UserError(this, "error.label");
            }
            isAlgorithm.allSteps();
            Vector<Integer> vIndex = isAlgorithm.getOutputDatasetIndex();
            DataIndex index = exampleSet.getIndex();
            index.setAllFalse();
            for (int i : vIndex) {
                index.set(i, true);
            }
            return index;

        } catch (NotEnoughInstancesException ex) {
            throw new UserError(this, "Not enought instances");
        } catch (UserError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UserError(this, "An exception occure, most likely in WekaInstanceSelection module");
        }
    }

    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        switch (capability) {
            case BINOMINAL_ATTRIBUTES:
            case POLYNOMINAL_ATTRIBUTES:
            case NUMERICAL_ATTRIBUTES:
            case POLYNOMINAL_LABEL:
            case BINOMINAL_LABEL:
            case NUMERICAL_LABEL:
            case MISSING_VALUES:
                return true;
            default:
                return false;
        }
    }

    /*
     public boolean supportsCapability(OperatorCapability capability) {
     Classifier classifier;
     try {
     classifier = getWekaClassifier(WekaTools.getWekaParametersFromTypes(this, wekaParameters));
     } catch (OperatorException e) {
     return true;
     }
     if (classifier != null) {
     try {
     return WekaLearnerCapabilities.supportsCapability(classifier, capability);
     } catch (Throwable t) {
     return true;
     }
     }
     return true;
     }
     */
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        ParameterType type = new ParameterTypeCategory(WekaISAlgorithms.PARAMETER_IS_ALGORITHM, "Name of instance selection algorithm", WekaISAlgorithms.IS_ALGORITHM_TYPES(), 0);
        type.setExpert(false);
        types.add(type);

        type = new ParameterTypeInt(PARAMETER_NEAREST_NEIGHBORS, "Number of nearest neighbors", 1, Integer.MAX_VALUE, 3);
        type.setExpert(false);
        type.registerDependencyCondition(new EqualTypeCondition(this, WekaISAlgorithms.PARAMETER_IS_ALGORITHM, WekaISAlgorithms.IS_ALGORITHM_TYPES(), false,
                WekaISAlgorithms.ENN.ordinal(),
                WekaISAlgorithms.ICF.ordinal(),
                WekaISAlgorithms.BSE.ordinal(),
                WekaISAlgorithms.DROP1.ordinal(),
                WekaISAlgorithms.DROP2.ordinal(),
                WekaISAlgorithms.DROP3.ordinal(),
                WekaISAlgorithms.DROP4.ordinal(),
                WekaISAlgorithms.DROP5.ordinal(),
                WekaISAlgorithms.ENN_REG.ordinal(),
                WekaISAlgorithms.ICF_REG.ordinal()));

        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_THRESHOLD, "Threshold for regression", 0, Double.MAX_VALUE, 0.1);
        type.setExpert(false);
        type.registerDependencyCondition(new EqualTypeCondition(this, WekaISAlgorithms.PARAMETER_IS_ALGORITHM, WekaISAlgorithms.IS_ALGORITHM_TYPES(), false,
                WekaISAlgorithms.ENN_REG.ordinal(),
                WekaISAlgorithms.ICF_REG.ordinal(),
                WekaISAlgorithms.CNN_REG.ordinal(),
                WekaISAlgorithms.MSS_REG.ordinal()));
        types.add(type);

        return types;
    }
}
