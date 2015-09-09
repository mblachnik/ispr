/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.optimization;

//import history.OldAbstractPRulesOperator;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.clustering.clusterer.RMAbstractClusterer;
import com.rapidminer.operator.learner.CapabilityProvider;
import com.rapidminer.ispr.operator.learner.AbstractPRulesOperatorChain;
import com.rapidminer.ispr.operator.learner.classifiers.MyKNNClassificationModel;
import com.rapidminer.ispr.operator.learner.classifiers.PredictionType;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetPrecondition;
import com.rapidminer.ispr.operator.learner.tools.PRulesUtil;
import com.rapidminer.operator.ports.metadata.CapabilityPrecondition;
import com.rapidminer.operator.ports.metadata.DistanceMeasurePrecondition;
import com.rapidminer.operator.ports.metadata.GenerateModelTransformationRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import java.util.List;

/**
 *
 * @author Marcin
 */
public abstract class AbstractPrototypeOptimizationChain extends AbstractPRulesOperatorChain {

    private static final long serialVersionUID = 21;
    /**
     * 
     */
    protected final OutputPort exampleInnerSourcePort = getSubprocess(0).getInnerSources().createPort("Example Set");
    /**
     * 
     */
    protected final InputPort initialPrototypesInnerSourcePort = getSubprocess(0).getInnerSinks().createPort("Codebooks");    
    /**
     * 
     */
    //protected final OutputPort initialPrototyoesOutputPort = getOutputPorts().createPassThroughPort("Initial Prototpes");    
    
    protected final OutputPort modelOutputPort = getOutputPorts().createPort("Model");
    
    private double numberOfInstancesBeaforeSelection = -1;
    private double numberOfInstancesAfterSelection = -1;
    private double compression = -1;
    private PredictionType predictionType;
    

    /**
     * 
     * @param description
     * @param predictionType
     */
    public AbstractPrototypeOptimizationChain(OperatorDescription description, PredictionType predictionType) {
        super(description,"Initialization");//
        this.predictionType = predictionType;
        getTransformer().addRule(new GenerateModelTransformationRule(exampleSetInputPort, modelOutputPort, MyKNNClassificationModel.class));
        exampleSetInputPort.addPrecondition(new CapabilityPrecondition(new CapabilityProvider() {
            @Override
            public boolean supportsCapability(OperatorCapability capability) {
                switch (capability) {
                    case BINOMINAL_ATTRIBUTES:
                    case POLYNOMINAL_ATTRIBUTES:
                    case NUMERICAL_ATTRIBUTES:
                    case POLYNOMINAL_LABEL:
                    case BINOMINAL_LABEL:
                    case MISSING_VALUES:
                        return true;
                    default:
                        return false;
                }
            }
            }, exampleSetInputPort));
        
        initialPrototypesInnerSourcePort.addPrecondition(new CapabilityPrecondition(new CapabilityProvider() {
            @Override
            public boolean supportsCapability(OperatorCapability capability) {
                switch (capability) {
                    case BINOMINAL_ATTRIBUTES:
                    case POLYNOMINAL_ATTRIBUTES:
                    case NUMERICAL_ATTRIBUTES:
                    case POLYNOMINAL_LABEL:
                    case BINOMINAL_LABEL:
                    case MISSING_VALUES:
                        return true;
                    default:
                        return false;
                }
            }
            }, initialPrototypesInnerSourcePort));
        
        initialPrototypesInnerSourcePort.addPrecondition(new ExampleSetPrecondition(exampleSetInputPort));
        initialPrototypesInnerSourcePort.addPrecondition(new DistanceMeasurePrecondition(initialPrototypesInnerSourcePort, this));
        
        getTransformer().addPassThroughRule(initialPrototypesInnerSourcePort, prototypesOutputPort);
        //getTransformer().addPassThroughRule(initialPrototypesInnerSourcePort, initialPrototyoesOutputPort);                
        getTransformer().addPassThroughRule(exampleSetInputPort, exampleInnerSourcePort);
        
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
        exampleInnerSourcePort.deliver(trainingSet);
        getSubprocess(0).execute();
        inApplyLoop();
        ExampleSet codebooksInitialization = initialPrototypesInnerSourcePort.getDataOrNull(ExampleSet.class);            
        if (codebooksInitialization == null) {
            throw new UserError(this, "Need initial prototypes");
        }        
        //initialPrototyoesOutputPort.deliver(codebooksInitialization);                        
        ExampleSet codebooks = PRulesUtil.duplicateExampleSet(codebooksInitialization);        
        MyKNNClassificationModel<Number> model = optimize(trainingSet, codebooks);        
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
            /*
            type = new ParameterTypeBoolean(RMAbstractClusterer.PARAMETER_ADD_CLUSTER_ATTRIBUTE, "If enabled, a cluster id is generated as new special attribute directly in this operator, otherwise this operator does not add an id attribute. In the latter case you have to use the Apply Model operator to generate the cluster attribute.", true, false);
            types.add(type);
            */
        }
        return types;
    }

    /**
     * 
     * @param trainingSet
     * @param codebooks
     * @return 
     * @throws OperatorException
     */
    public abstract MyKNNClassificationModel<Number> optimize(ExampleSet trainingSet, ExampleSet codebooks) throws OperatorException;
}
