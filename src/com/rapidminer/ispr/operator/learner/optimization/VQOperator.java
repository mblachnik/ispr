/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.optimization;

import java.util.List;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.ispr.operator.learner.classifiers.MyKNNClassificationModel;
import com.rapidminer.ispr.operator.learner.classifiers.PredictionType;
import com.rapidminer.ispr.operator.learner.classifiers.VotingType;
import com.rapidminer.ispr.operator.learner.optimization.clustering.AbstractVQModel;
import com.rapidminer.ispr.operator.learner.optimization.clustering.VQModel;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;

/**
 *
 * @author Marcin
 */
public class VQOperator extends AbstractPrototypeOptimizationChain{

    /**
     * 
     */
    public static final String PARAMETER_ITERATION_NUMBER = "Iterations";
    /**
     * 
     */
    public static final String PARAMETER_UPDATE_RATE = "Alpha";
    private DistanceMeasureHelper measureHelper;
    private int numberOfIteration;
    private double updateRate;

    /**
     * 
     * @param description
     */
    public VQOperator(OperatorDescription description) {
        super(description,PredictionType.Clustering);
        numberOfIteration = 50;
        updateRate = 0.02;
        measureHelper = new DistanceMeasureHelper(this);
    }

    /**
     * 
     * @param trainingSet
     * @param codebooks
     * @throws OperatorException
     */
    @Override
    public MyKNNClassificationModel<Number> optimize(ExampleSet trainingSet, ExampleSet codebooks) throws OperatorException {
        this.numberOfIteration = getParameterAsInt(PARAMETER_ITERATION_NUMBER);
        DistanceMeasure distance = measureHelper.getInitializedMeasure(codebooks);                 
        this.updateRate = getParameterAsDouble(PARAMETER_UPDATE_RATE);
        AbstractVQModel vqModel = new VQModel(codebooks, numberOfIteration, distance, updateRate);          
        vqModel.run(trainingSet);   
        ISPRGeometricDataCollection<Number> knn = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH,codebooks, distance);
        MyKNNClassificationModel<Number> model = new MyKNNClassificationModel<Number>(codebooks, knn, 1, VotingType.MAJORITY, false);
        return model;
    }
         

    @Override
    protected MDInteger getSampledSize(ExampleSetMetaData exampleSetMD) throws UndefinedParameterError {
       ExampleSetMetaData prototypesMetaData = (ExampleSetMetaData) this.initialPrototypesInnerSourcePort.getMetaData();
        if (prototypesMetaData != null){
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

        //ParameterType lvqTypeParameter =  new 
        ParameterType type = new ParameterTypeInt(PARAMETER_ITERATION_NUMBER, "Number of iteration loop", 1, Integer.MAX_VALUE, this.numberOfIteration);
        type.setExpert(false);        
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_UPDATE_RATE, "Value of update rate", 0, Double.MAX_VALUE, 0.3);
        type.setExpert(false);
        types.add(type);
        
        types.addAll(DistanceMeasures.getParameterTypes(this));
        return types;
    }
}
