/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.optimization;

//import history.OldAbstractPRulesOperator;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SimpleExampleSet;
import com.rapidminer.example.table.*;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.clustering.clusterer.RMAbstractClusterer;
import com.rapidminer.ispr.operator.learner.AbstractPRulesOperator;
import com.rapidminer.ispr.operator.learner.classifiers.MyKNNClassificationModel;
import com.rapidminer.ispr.operator.learner.classifiers.PredictionType;
import com.rapidminer.ispr.operator.learner.optimization.clustering.AbstractBatchModel;
import com.rapidminer.ispr.tools.math.container.DoubleDoubleContainer;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.DistanceMeasurePrecondition;
import com.rapidminer.operator.ports.metadata.GeneratePredictionModelTransformationRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.tools.Ontology;
import com.rapidminer.ispr.tools.math.container.PairContainer;
import java.util.*;

/**
 *
 * @author Marcin
 */
public abstract class AbstractPrototypeOptimizationOperator extends AbstractPRulesOperator {

    private static final long serialVersionUID = 21;
    private double numberOfInstancesBeaforeSelection = -1;
    private double numberOfInstancesAfterSelection = -1;
    private double compression = -1;
    private PredictionType predictionType;
    protected final OutputPort modelOutputPort = getOutputPorts().createPort("Model");    
    protected HashMap<Integer, String> clusterNamesMap;

    /**
     *
     * @param description
     */
    public AbstractPrototypeOptimizationOperator(OperatorDescription description, PredictionType predictionType) {
        super(description);
        this.predictionType = predictionType;
        exampleSetInputPort.addPrecondition(new DistanceMeasurePrecondition(exampleSetInputPort, this));
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
        numberOfInstancesBeaforeSelection = trainingSet.size();
        PairContainer<ExampleSet, MyKNNClassificationModel<Number>> result = optimize(trainingSet);
        ExampleSet codebooks = result.getFirst();
        MyKNNClassificationModel<Number> model = result.getSecond();

        //prototypesOutputPort.deliver(codebooks);
        modelOutputPort.deliver(model);

        numberOfInstancesAfterSelection = codebooks.size();
        compression = numberOfInstancesAfterSelection / numberOfInstancesBeaforeSelection;
        return codebooks;
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        if (predictionType == PredictionType.Clustering) {
            ParameterType type = new ParameterTypeBoolean(RMAbstractClusterer.PARAMETER_ADD_CLUSTER_ATTRIBUTE, "If enabled, a cluster id is generated as new special attribute directly in this operator, otherwise this operator does not add an id attribute. In the latter case you have to use the Apply Model operator to generate the cluster attribute.", true, false);
            types.add(type);

            type = new ParameterTypeBoolean(RMAbstractClusterer.PARAMETER_ADD_CLUSTER_ATTRIBUTE, "If enabled, a cluster id is generated as new special attribute directly in this operator, otherwise this operator does not add an id attribute. In the latter case you have to use the Apply Model operator to generate the cluster attribute.", true, false);
            types.add(type);
        }
        return types;
    }

    /**
     *
     * @param trainingSet
     * @return
     * @throws OperatorException
     */
    public abstract PairContainer<ExampleSet, MyKNNClassificationModel<Number>> optimize(ExampleSet trainingSet) throws OperatorException;

    protected ExampleSet prepareCodebooksExampleSet(Collection<Prototype> codebooks, Attributes attributes) {
        Attribute codebookLabels = AttributeFactory.createAttribute(Attributes.CLUSTER_NAME, Ontology.NOMINAL);
        NominalMapping codebookLabelsNames = new PolynominalMapping(new HashMap<Integer, String>(clusterNamesMap));
        codebookLabels.setMapping(codebookLabelsNames);
        ArrayList<Attribute> codebookAttributesCollection = new ArrayList<Attribute>(attributes.size());
        for (Attribute attribute : attributes) {
            codebookAttributesCollection.add(AttributeFactory.createAttribute(attribute));
        }
        codebookAttributesCollection.add(codebookLabels);
        ExampleTable codebooksTable = new MemoryExampleTable(codebookAttributesCollection, new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.'), codebooks.size());
        ExampleSet codebooksSet = new SimpleExampleSet(codebooksTable, codebookAttributesCollection);
        codebooksSet.getAttributes().setLabel(codebookLabels);
        Iterator<Prototype> codebookIterator = codebooks.iterator();
        Iterator<Example> codebookExampleIterator = codebooksSet.iterator();
        //Rewrite codebooks to codebooks ExampleSet
        Attributes codebookAttributes = codebooksSet.getAttributes();
        int codebookIndex = 0;
        while (codebookIterator.hasNext()) {
            Prototype codebook = codebookIterator.next();
            Example codebookExample = codebookExampleIterator.next();
            int i = 0;
            for (Attribute a : codebookAttributes) {
                codebookExample.setValue(a, codebook.getValues()[i]);
                i++;
            }
            codebookExample.setLabel(codebookIndex);
            codebookIndex++;
        }
        return codebooksSet;
    }

    protected void prepareClusterNamesMap(int c) {
        this.clusterNamesMap = new HashMap<Integer, String>(c); //Map of nominal values of Cluster attribute        
        for (int i = 0; i < c; i++) {
            String clusterName = Attributes.CLUSTER_NAME + "_" + i;
            clusterNamesMap.put(i, clusterName);
        }
    }

    protected void prepareTrainingExampleSet(ExampleSet trainingSet, AbstractBatchModel batchModel) {
        //Preparing attributes for trainingSet   
        boolean b = getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_CLUSTER_ATTRIBUTE);
        if (b) {
            int c = clusterNamesMap.size();
            ArrayList<Attribute> partitionMatrixAttributes = new ArrayList<Attribute>(c); //partition matrix + cluster attribute
            for (int i = 0; i < c; i++) {
                Attribute attribute = AttributeFactory.createAttribute(clusterNamesMap.get(i), Ontology.NUMERICAL);
                partitionMatrixAttributes.add(attribute);
            }
            Attribute traininSetLabels = AttributeFactory.createAttribute(Attributes.CLUSTER_NAME, Ontology.NOMINAL);
            NominalMapping labelsNames = new PolynominalMapping(new HashMap<Integer, String>(clusterNamesMap));
            traininSetLabels.setMapping(labelsNames);
            traininSetLabels.setDefault(Double.NaN);
            trainingSet.getExampleTable().addAttributes(partitionMatrixAttributes);
            trainingSet.getExampleTable().addAttribute(traininSetLabels);
            //TODO Uwaga tutaj dodajemy etykiety klastrów jako Label, a powinno byæ w zale¿noœci od ustawieñ prze³¹cznika                
            int i = 0;
            Attributes attributes = trainingSet.getAttributes();
            for (Attribute attribute : partitionMatrixAttributes) {
                attributes.addRegular(attribute);
                attributes.setSpecialAttribute(attribute, clusterNamesMap.get(i));
                i++;
            }
            Iterator<double[]> partitionMatrixIterator = batchModel.getPartitionMatrix().iterator();
            Iterator<Example> exampleIterator = trainingSet.iterator();
            while(exampleIterator.hasNext() && partitionMatrixIterator.hasNext()){
                Example example = exampleIterator.next();
                double[] partitionMatrix = partitionMatrixIterator.next();
                i = 0;
                for(Attribute attribute : partitionMatrixAttributes){
                    example.setValue(attribute, partitionMatrix[i]);
                    i++;
                }
            }
            if (getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_AS_LABEL)) {
                attributes.setLabel(traininSetLabels);
            } else {
                attributes.setCluster(traininSetLabels);
            }

            batchModel.apply(trainingSet, traininSetLabels);
        }
    }
}
