package com.rapidminer.ispr.operator.learner.optimization;

import java.util.List;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.ispr.operator.learner.classifiers.MyKNNClassificationModel;
import com.rapidminer.ispr.operator.learner.classifiers.PredictionType;
import com.rapidminer.ispr.operator.learner.classifiers.VotingType;
import com.rapidminer.ispr.operator.learner.optimization.clustering.AbstractBatchModel;
import com.rapidminer.ispr.operator.learner.optimization.clustering.FCMModel;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.ispr.tools.math.container.PairContainer;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import java.util.*;

/**
 *
 * @author Marcin
 */
public class FCMOperator extends AbstractPrototypeOptimizationOperator {

    /**
     *
     */
    public static final String PARAMETER_ITERATION_NUMBER = "Iterations";
    /**
     *
     */
    public static final String PARAMETER_FUZZYNES = "Fuzzynes";
    /**
     *
     */
    public static final String PARAMETER_MIN_GAIN = "MinGain";
    /**
     *
     */
    public static final String PARAMETER_NUMBER_OF_CLUSTERS = "Clusters";
    int c; //Number of clusters
    double m; //Fuzzynes values
    DistanceMeasureHelper measureHelper;
    int numberOfIteration;
    double minGain; //Minimum improvement of optimization process
    double costFunctionValue;

    /**
     *
     * @param description
     */
    public FCMOperator(OperatorDescription description) {
        super(description, PredictionType.Clustering);
        addValue(new ValueDouble("CostFunctionValue", "Cost Function Value") {

            @Override
            public double getDoubleValue() {
                return costFunctionValue;
            }
        });
        //getTransformer().addPassThroughRule(exampleSetInputPort, exampleSetOutputPort);		
        //getTransformer().addPassThroughRule(exampleSetInputPort,originalExampleSetOutputPort);
        c = 3; //Number of clusters
        m = 2; //Fuzzynes values
        numberOfIteration = 50;
        minGain = 0.0001;
        measureHelper = new DistanceMeasureHelper(this);

    }

    /**
     *
     * @param trainingSet
     * @return
     * @throws OperatorException
     */
    @Override
    public PairContainer<ExampleSet, MyKNNClassificationModel<Number>> optimize(ExampleSet trainingSet) throws OperatorException {
        //Creating attributes related with partition Matrix
        this.c = getParameterAsInt(PARAMETER_NUMBER_OF_CLUSTERS);
        if (c > trainingSet.size()) {
            throw new UserError(this, "Number of clusters greater then number of samples");
        }
        m = getParameterAsDouble(PARAMETER_FUZZYNES);
        minGain = getParameterAsDouble(PARAMETER_MIN_GAIN);
        numberOfIteration = getParameterAsInt(PARAMETER_ITERATION_NUMBER);       

        DistanceMeasure distance = measureHelper.getInitializedMeasure(trainingSet);
        AbstractBatchModel batchModel = new FCMModel(distance, m, numberOfIteration, minGain, RandomGenerator.getRandomGenerator(this), c, trainingSet);
        Collection<Prototype> codebooks = batchModel.train(trainingSet);

        prepareClusterNamesMap(c);
                
        prepareTrainingExampleSet(trainingSet, batchModel);
       
        ExampleSet codebooksSet = prepareCodebooksExampleSet(codebooks, trainingSet.getAttributes());                        
        
        costFunctionValue = batchModel.getCostFunctionValue();
        ISPRGeometricDataCollection<Number> knn = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH,codebooksSet, distance);
        MyKNNClassificationModel<Number> model = new MyKNNClassificationModel<Number>(codebooksSet, knn, 1, VotingType.MAJORITY, false);
        return new PairContainer<ExampleSet, MyKNNClassificationModel<Number>>(codebooksSet, model);
    }

    @Override
    protected MDInteger getSampledSize(ExampleSetMetaData emd) throws UndefinedParameterError {
        return c < 0 ? new MDInteger() : new MDInteger(c);
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
            default:
                return false;
        }
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type;

        type = new ParameterTypeInt(PARAMETER_NUMBER_OF_CLUSTERS, "Number of clusters", 1, Integer.MAX_VALUE, c);
        type.setExpert(false);
        types.add(type);

        type = new ParameterTypeInt(PARAMETER_ITERATION_NUMBER, "Number of iteration loop", 1, Integer.MAX_VALUE, numberOfIteration);
        type.setExpert(false);
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_FUZZYNES, "Fuzzynes", 2, Double.MAX_VALUE, m);
        type.setExpert(true);
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_MIN_GAIN, "Minimum gain during optimization", 0, Double.MAX_VALUE, minGain);
        type.setExpert(true);
        types.add(type);

        types.addAll(DistanceMeasures.getParameterTypes(this));

        types.addAll(RandomGenerator.getRandomGeneratorParameters(this));

        return types;
    }
}
