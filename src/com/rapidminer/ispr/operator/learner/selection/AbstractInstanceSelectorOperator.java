package com.rapidminer.ispr.operator.learner.selection;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.example.set.SortedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.ispr.operator.learner.AbstractPRulesOperator;
import com.rapidminer.ispr.operator.learner.classifiers.MyKNNClassificationModel;
import com.rapidminer.ispr.operator.learner.classifiers.VotingType;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.GeneratePredictionModelTransformationRule;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Marcin
 */
public abstract class AbstractInstanceSelectorOperator extends AbstractPRulesOperator {

    public static final String PARAMETER_RANDOMIZE_EXAMPLES = "randomize_examples";
    public static final String PARAMETER_ADD_WEIGHTS = "add weight attribute";
    int sampleSize = -1;
    private double numberOfInstancesBeaforeSelection = -1;
    private double numberOfInstancesAfterSelection = -1;
    private double compression = -1;
    protected DistanceMeasureHelper measureHelper;
    protected final OutputPort modelOutputPort = getOutputPorts().createPort("Model");
    private boolean isDistanceBasedMethod;
    
    /**
     *
     * @param description
     */
    public AbstractInstanceSelectorOperator(OperatorDescription description) {
        super(description);        
        init();
    }

    private void init() {
        isDistanceBasedMethod = true;
        measureHelper = new DistanceMeasureHelper(this);
        //getTransformer().addRule(new GenerateModelTransformationRule(exampleSetInputPort, modelOutputPort, MyKNNClassificationModel.class));
        getTransformer().addRule(new GeneratePredictionModelTransformationRule(exampleSetInputPort, modelOutputPort, MyKNNClassificationModel.class));
        addValue(new ValueDouble("Instances_beafore_selection", "Number Of Examples in the training set") {
            @Override
            public double getDoubleValue() {
                return numberOfInstancesBeaforeSelection;
            }
        });
        addValue(new ValueDouble("Instances_after_selection", "Number Of Examples after selection") {
            @Override
            public double getDoubleValue() {
                return numberOfInstancesAfterSelection;
            }
        });
        addValue(new ValueDouble("Compression", "Compressin = #Instances_after_selection/#Instances_beafore_selection") {
            @Override
            public double getDoubleValue() {
                return compression;
            }
        });
    }

    /**
     *
     * @param trainingSet
     * @return
     * @throws OperatorException
     */
    @Override
    public ExampleSet processExamples(ExampleSet trainingSet) throws OperatorException {
        /*
         * Attribute weightsAttribute = trainingSet.getAttributes().getWeight(); if ( weightsAttribute == null ){ weightsAttribute
         * = AttributeFactory.createAttribute (PRulesUtil.INSTANCES_WEIGHTS_NAME,Ontology.NUMERICAL);
         * trainingSet.getExampleTable().addAttribute(weightsAttribute); trainingSet.getAttributes().setWeight(weightsAttribute);
         * for (Example example : trainingSet){ example.setWeight(1); } }
         */
        if (getSampleRandomizeOption()) {
            boolean shufleExamples = getParameterAsBoolean(PARAMETER_RANDOMIZE_EXAMPLES);
            if (shufleExamples) { //We can shuffle examples ony if we don't use initial geometricCollection. Order of examples in both in GemoetricCollection and ExampleSet must be equal            
                ArrayList<Integer> indicesCollection = new ArrayList<Integer>(trainingSet.size());
                for (int i = 0; i < trainingSet.size(); i++) {
                    indicesCollection.add(i);
                }

                Collections.shuffle(indicesCollection, RandomGenerator.getRandomGenerator(this));

                int[] indices = new int[trainingSet.size()];
                for (int i = 0; i < trainingSet.size(); i++) {
                    indices[i] = indicesCollection.get(i);
                }

                trainingSet = new SortedExampleSet(trainingSet, indices);
            }
        }
        SelectedExampleSet instanceSelectionInput;
        SelectedExampleSet output;
        if (trainingSet instanceof SelectedExampleSet) {
            output = (SelectedExampleSet) trainingSet;
            instanceSelectionInput = (SelectedExampleSet) trainingSet.clone();

        } else {
            output = new SelectedExampleSet(trainingSet);
            instanceSelectionInput = (SelectedExampleSet) output.clone();
        }
        numberOfInstancesBeaforeSelection = trainingSet.size();
        DataIndex index = selectInstances(instanceSelectionInput);
        output.setIndex(index);
        numberOfInstancesAfterSelection = output.size();
        compression = numberOfInstancesAfterSelection / numberOfInstancesBeaforeSelection;
        if (modelOutputPort.isConnected()) {
            ISPRGeometricDataCollection<Number> samples;
            DistanceMeasure distance = measureHelper.getInitializedMeasure(output);
            samples = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, output, distance);
            MyKNNClassificationModel<Number> model = new MyKNNClassificationModel<Number>(output, samples, 1, VotingType.MAJORITY, false);
            modelOutputPort.deliver(model);
        }
        boolean addWeights = getParameterAsBoolean(PARAMETER_ADD_WEIGHTS);
        if (addWeights) {
            //ExampleSet tmpOutput = output.getParentExampleSet();
            //DataIndex fullIndex = output.getFullIndex();
            ExampleSet tmpTraining = (ExampleSet) trainingSet.clone();
            Attribute weights = AttributeFactory.createAttribute(Attributes.WEIGHT_NAME, Ontology.NUMERICAL);
            Attributes attributes = tmpTraining.getAttributes();
            tmpTraining.getExampleTable().addAttribute(weights);
            attributes.setWeight(weights);

            ExampleSet sortedTrainingSet = new SortedExampleSet(tmpTraining, attributes.getId(), SortedExampleSet.INCREASING);
            sortedTrainingSet.getAttributes().setWeight(weights);
            ExampleSet sortedPrototypesSet = new SortedExampleSet(output, attributes.getId(), SortedExampleSet.INCREASING);
            Iterator<Example> trainingIterator = sortedTrainingSet.iterator();
            Iterator<Example> prototypeIterator = sortedPrototypesSet.iterator();
            while (prototypeIterator.hasNext()) {
                Example prototypeExample = prototypeIterator.next();
                while (trainingIterator.hasNext()) {
                    Example trainingExample = trainingIterator.next();
                    if (prototypeExample.getId() == trainingExample.getId()) {
                        trainingExample.setWeight(1);
                        break;
                    }
                }
            }
            return tmpTraining;
        }
        return output;
    }

     /**
     * This method may be override if an algorithm doesn't want to allow sample randomization. This may be used for ENN algorithm because the order of samples doesn't influence the result. This cannot be solved using class field because in the constructor DistanceMeasureHelper executes the geParametersType method
     * @return
     */
    boolean getSampleRandomizeOption() {
        return true;
    }

    @Override
    protected MDInteger getSampledSize(ExampleSetMetaData exampleSetMD) throws UndefinedParameterError {
        if (sampleSize == -1) {
            return new MDInteger();
        } else {
            return new MDInteger(sampleSize);
        }
    }

    /**
     *
     * @param trainingSet
     * @return
     * @throws OperatorException
     */
    public abstract DataIndex selectInstances(SelectedExampleSet trainingSet) throws OperatorException;

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        if (getSampleRandomizeOption()) {
            ParameterType type = new ParameterTypeBoolean(PARAMETER_RANDOMIZE_EXAMPLES, "Randomize examples", true);
            type.setExpert(false);
            types.add(type);
            types.addAll(RandomGenerator.getRandomGeneratorParameters(this));
        }

        ParameterType type = new ParameterTypeBoolean(PARAMETER_ADD_WEIGHTS, "Add weight attribute", false);
        type.setExpert(true);
        types.add(type);

        if (isDistanceBasedMethod)
            types.addAll(DistanceMeasures.getParameterTypes(this));
        return types;
    }
    
    public boolean isIsDistanceBasedMethod() {
        return isDistanceBasedMethod;
    }

    public void setIsDistanceBasedMethod(boolean isDistanceBasedMethod) {
        this.isDistanceBasedMethod = isDistanceBasedMethod;
    }
}
