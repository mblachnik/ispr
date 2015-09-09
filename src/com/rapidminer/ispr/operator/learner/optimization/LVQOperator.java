/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.optimization;

import com.rapidminer.ispr.operator.learner.optimization.supervised.SLVQ1Model;
import com.rapidminer.ispr.operator.learner.optimization.supervised.LVQ1Model;
import com.rapidminer.ispr.operator.learner.optimization.supervised.OLVQModel;
import com.rapidminer.ispr.operator.learner.optimization.supervised.WLVQModel;
import java.util.List;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.ispr.operator.learner.PRulesModel;
import com.rapidminer.ispr.operator.learner.classifiers.MyKNNClassificationModel;
import com.rapidminer.ispr.operator.learner.classifiers.PredictionType;
import com.rapidminer.ispr.operator.learner.classifiers.VotingType;
import com.rapidminer.ispr.operator.learner.optimization.supervised.GLVQModel;
import com.rapidminer.ispr.operator.learner.optimization.supervised.LVQ21ModelMy;
import com.rapidminer.ispr.operator.learner.optimization.supervised.LVQ2ModelMy;
import com.rapidminer.ispr.operator.learner.optimization.supervised.LVQ3ModelMy;
import com.rapidminer.ispr.operator.learner.optimization.supervised.LVQTypes;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.EqualTypeCondition;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;

/**
 * GLVQ algorithm - based on paper: Atsushi Sato, Keiji Yamada  Generalized Learning Vector Quantization, 1996
 * @author Marcin
 */
public class LVQOperator extends AbstractPrototypeOptimizationChain {

    /**
     *
     */
    public static final String PARAMETER_ITERATION_NUMBER = "Iterations";
    /**
     *
     */
    public static final String PARAMETER_UPDATE_RATE = "Alpha";
    /**
     *
     */
    public static final String PARAMETER_UPDATE_RATE_POSITIVE = "Alpha_positive";
    /**
     *
     */
    public static final String PARAMETER_UPDATE_RATE_NEGATIVE = "Alpha_negative";
    /**
     *
     */
    public static final String PARAMETER_LVQ_TYPE = "LVQ_type";
    /**
     *
     */
    public static final String PARAMETER_WINDOW = "window";
    /**
     *
     */
    public static final String PARAMETER_EPSILON = "epsilon";
    
    public static final String PARAMETER_CALCDIFF = "Calc dF(u)/du";

    private DistanceMeasureHelper measureHelper;
    private int numberOfIteration;
    private double updateRatePositive;
    private double updateRateNegative;
    private LVQTypes lvqType;
    private double window;
    private double epsilon;

    /**
     *
     * @param description
     */
    public LVQOperator(OperatorDescription description) {
        super(description, PredictionType.Classification);
        numberOfIteration = 50;
        updateRatePositive = 0.02;
        updateRateNegative = 0.02;
        lvqType = LVQTypes.LVQ1;
        window = 0.2;
        epsilon = 0.2;
        measureHelper = new DistanceMeasureHelper(this);
    }

    /**
     *
     * @param trainingSet
     * @param codebooks
     * @return 
     * @throws OperatorException
     */
    @Override
    public MyKNNClassificationModel<Number> optimize(ExampleSet trainingSet, ExampleSet codebooks) throws OperatorException {
        this.numberOfIteration = getParameterAsInt(PARAMETER_ITERATION_NUMBER);
        DistanceMeasure distance = measureHelper.getInitializedMeasure(trainingSet);
        distance.init(codebooks.getAttributes(), trainingSet.getAttributes());
        PRulesModel<ExampleSet> lvqModel = null;
        int idLvqType = getParameterAsInt(PARAMETER_LVQ_TYPE);
        lvqType = LVQTypes.values()[idLvqType];
        switch (lvqType) {
            case LVQ1:
                this.updateRatePositive = getParameterAsDouble(PARAMETER_UPDATE_RATE_POSITIVE);
                this.updateRateNegative = getParameterAsDouble(PARAMETER_UPDATE_RATE_NEGATIVE);
                lvqModel = new LVQ1Model(codebooks, numberOfIteration, distance, updateRatePositive, updateRateNegative);
                break;
            case SLVQ:
                this.updateRatePositive = getParameterAsDouble(PARAMETER_UPDATE_RATE_POSITIVE);
                this.updateRateNegative = getParameterAsDouble(PARAMETER_UPDATE_RATE_NEGATIVE);
                lvqModel = new SLVQ1Model(codebooks, numberOfIteration, distance, updateRatePositive, updateRateNegative);
                break;
            case LVQ2:
                this.updateRatePositive = getParameterAsDouble(PARAMETER_UPDATE_RATE_POSITIVE);
                this.updateRateNegative = getParameterAsDouble(PARAMETER_UPDATE_RATE_NEGATIVE);
                lvqModel = new LVQ2ModelMy(codebooks, numberOfIteration, distance, updateRatePositive, updateRateNegative);
                break;
            case LVQ21:
                this.updateRatePositive = getParameterAsDouble(PARAMETER_UPDATE_RATE_POSITIVE);
                this.updateRateNegative = getParameterAsDouble(PARAMETER_UPDATE_RATE_NEGATIVE);
                window = getParameterAsDouble(PARAMETER_WINDOW);
                lvqModel = new LVQ21ModelMy(codebooks, numberOfIteration, distance, updateRatePositive, updateRateNegative, window);
                break;
            case LVQ3:
                this.updateRatePositive = getParameterAsDouble(PARAMETER_UPDATE_RATE_POSITIVE);
                this.updateRateNegative = getParameterAsDouble(PARAMETER_UPDATE_RATE_NEGATIVE);
                window = getParameterAsDouble(PARAMETER_WINDOW);
                epsilon = getParameterAsDouble(PARAMETER_EPSILON);
                lvqModel = new LVQ3ModelMy(codebooks, numberOfIteration, distance, updateRatePositive, updateRateNegative, window, epsilon);
                break;
            case WLVQ:
                this.updateRatePositive = getParameterAsDouble(PARAMETER_UPDATE_RATE_POSITIVE);
                this.updateRateNegative = getParameterAsDouble(PARAMETER_UPDATE_RATE_NEGATIVE);
                if (trainingSet.getAttributes().getWeight() == null) {
                    throw new UserError(this, "WLVQ algorithm requires instances weight attribute.");
                }
                lvqModel = new WLVQModel(codebooks, numberOfIteration, distance, updateRatePositive, updateRateNegative);
                break;
            case OLVQ:
                double updateRate = getParameterAsDouble(PARAMETER_UPDATE_RATE_POSITIVE);
                lvqModel = new OLVQModel(codebooks, numberOfIteration, distance, updateRate);
                break;
            case GLVQ:                                
                this.updateRatePositive = getParameterAsDouble(PARAMETER_UPDATE_RATE_POSITIVE);                
                this.updateRatePositive = getParameterAsDouble(PARAMETER_UPDATE_RATE_NEGATIVE);                
                lvqModel = new GLVQModel(codebooks, numberOfIteration, distance, updateRatePositive, updateRateNegative);
                break;                                
             //case LVQ21SGD
            default:
                throw new UserError(this, "Unknown LVQ type");

        }
        lvqModel.run(trainingSet);
        ISPRGeometricDataCollection<Number> knn = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH,codebooks, distance);
        MyKNNClassificationModel<Number> model = new MyKNNClassificationModel<Number>(codebooks, knn, 1, VotingType.MAJORITY, false);
        return model;
    }

    @Override
    protected MDInteger getSampledSize(ExampleSetMetaData exampleSetMD) throws UndefinedParameterError {
        ExampleSetMetaData prototypesMetaData = (ExampleSetMetaData) this.initialPrototypesInnerSourcePort.getMetaData();
        if (prototypesMetaData != null) {
            int absoluteNumber = prototypesMetaData.getNumberOfExamples().getNumber();
            return new MDInteger(absoluteNumber);
        }
        return new MDInteger();
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
                return true;
            default:
                return false;
        }
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        //ParameterType lvqTypeParameter =  new 
        ParameterType type = new ParameterTypeCategory(PARAMETER_LVQ_TYPE, "Defines on type of lvq algorithm.", LVQTypes.lvqTypes(), 0);
        type.setExpert(false);
        types.add(type);

        type = new ParameterTypeInt(PARAMETER_ITERATION_NUMBER, "Number of iteration loop", 1, Integer.MAX_VALUE, this.numberOfIteration);
        type.setExpert(false);
        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_LVQ_TYPE, LVQTypes.lvqTypes(), false, 
                LVQTypes.LVQ1.ordinal(), LVQTypes.LVQ2.ordinal(), LVQTypes.LVQ21.ordinal(), LVQTypes.LVQ3.ordinal(), 
                LVQTypes.OLVQ.ordinal(), LVQTypes.WLVQ.ordinal(), LVQTypes.SLVQ.ordinal(), LVQTypes.GLVQ.ordinal()));
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_UPDATE_RATE, "Value of update rate", 0, Double.MAX_VALUE, 0.3);
        type.setExpert(false);
        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_LVQ_TYPE, LVQTypes.lvqTypes(), false, LVQTypes.OLVQ.ordinal()));
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_UPDATE_RATE_POSITIVE, "Value of update rate", 0, Double.MAX_VALUE, this.updateRatePositive);
        type.setExpert(false);
        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_LVQ_TYPE, LVQTypes.lvqTypes(), false, 
                LVQTypes.LVQ1.ordinal(), LVQTypes.LVQ2.ordinal(), LVQTypes.LVQ21.ordinal(), LVQTypes.LVQ3.ordinal(), 
                LVQTypes.WLVQ.ordinal(), LVQTypes.SLVQ.ordinal(), LVQTypes.GLVQ.ordinal()));
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_UPDATE_RATE_NEGATIVE, "Value of update rate", 0, Double.MAX_VALUE, this.updateRateNegative);
        type.setExpert(false);
        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_LVQ_TYPE, LVQTypes.lvqTypes(), false, 
                LVQTypes.LVQ1.ordinal(), LVQTypes.LVQ2.ordinal(), LVQTypes.LVQ21.ordinal(), LVQTypes.LVQ3.ordinal(), 
                LVQTypes.WLVQ.ordinal(), LVQTypes.SLVQ.ordinal(), LVQTypes.GLVQ.ordinal()));
        types.add(type);


        type = new ParameterTypeDouble(PARAMETER_WINDOW, "Defines the relative window width", 0, 1, window);
        type.setExpert(false);
        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_LVQ_TYPE, LVQTypes.lvqTypes(), false, 
                LVQTypes.LVQ21.ordinal(), LVQTypes.LVQ3.ordinal()));
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_EPSILON, "Defines the epsilon of LVQ3", 0, 1, epsilon);
        type.setExpert(false);
        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_LVQ_TYPE, LVQTypes.lvqTypes(), false, 
                LVQTypes.LVQ3.ordinal()));
        types.add(type);        
        
        types.addAll(DistanceMeasures.getParameterTypes(this));
        return types;
    }
}
