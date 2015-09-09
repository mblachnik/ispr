package com.rapidminer.ispr.operator.learner.selection;

import java.util.List;

import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.operator.learner.tools.PRulesUtil;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.RandomGenerator;

/**
 * 
 * @author Marcin
 */
public class RandomInstanceSelectionOperator extends AbstractInstanceSelectorOperator {

    /**
     * 
     */
    public static final String INSTANCES_NUMBER = "Number of Instances to select";
        /**
         * 
         */
        public static final String STRATIFIED = "Stratified";
	
	private boolean stratifiedSelection;
	private RandomGenerator randomGenerator;

        /**
         * 
         * @param description
         */
        public RandomInstanceSelectionOperator(OperatorDescription description) {
		super(description);
                this.setIsDistanceBasedMethod(false);
                stratifiedSelection = true;	
	}

        /**
         * 
         * @param selectedTrainingSet
         * @return
         * @throws OperatorException
         */
        @Override
	public DataIndex selectInstances(SelectedExampleSet selectedTrainingSet) throws OperatorException {
		sampleSize = getParameterAsInt(INSTANCES_NUMBER);
        this.stratifiedSelection = getParameterAsBoolean(STRATIFIED);
        this.randomGenerator = RandomGenerator.getRandomGenerator(this);
        
        int realTrainingSetSize = selectedTrainingSet.size();
        if (sampleSize > realTrainingSetSize) {
            throw new UserError(this, 110, sampleSize);
        }
        
        DataIndex index = selectedTrainingSet.getIndex();
        index.setAllFalse();
        
        if (stratifiedSelection) {
            index = PRulesUtil.stratifiedSelection(selectedTrainingSet,sampleSize,randomGenerator);
        } else {        	        	
            //int[] idx = PRulesUtil.randomSelection(realTrainingSetSize, sampleSize , randomGenerator);           
            for (int i = 0; i<sampleSize; i++){
            	index.set(i,true);            		            	            	
            }
        }
        //selectedTrainingSet.setIndex(index);
        //return selectedTrainingSet;
        return index;
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
                    return true;
            default:
                return false;
        }                            
    }    
    
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        ParameterType instancesNumber = new ParameterTypeInt(INSTANCES_NUMBER, "Number of instances to be selected", 1, Integer.MAX_VALUE, 2);
        instancesNumber.setExpert(false);
        types.add(instancesNumber);

        ParameterType stratifiedSelectionParameter = new ParameterTypeBoolean(STRATIFIED, "Stratified selection", true);
        stratifiedSelectionParameter.setExpert(false);
        types.add(stratifiedSelectionParameter);
        
        return types;
    }

}
