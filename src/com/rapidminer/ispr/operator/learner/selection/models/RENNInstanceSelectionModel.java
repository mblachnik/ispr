/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.loss.ILossFunction;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.operator.learner.tools.PRulesUtil;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Marcin
 */
public class RENNInstanceSelectionModel extends AbstractInstanceSelectorModel {

    private int k;
    private DistanceMeasure measure;
    private ILossFunction loss;

    /**
     *
     * @param measure
     * @param k
     */
    public RENNInstanceSelectionModel(DistanceMeasure measure, int k, ILossFunction loss) {
        this.k = k;
        this.measure = measure;
        this.loss = loss;
    }

    /**
     *
     * @param exampleSet
     * @return
     */
    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) {
        loss.init(exampleSet);
        Attributes attributes = exampleSet.getAttributes();
        Attribute label = attributes.getLabel();
        //DATA STRUCTURE PREPARATION        
        ISPRGeometricDataCollection<Number> samples = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, exampleSet, measure);
        int numberOfClasses = label.getMapping().size();
        //ENN EDITTING
        double[] values = new double[attributes.size()];
        int[] counter = new int[numberOfClasses];
        DataIndex mainIndex = exampleSet.getIndex();
        while (true) {
            int instanceIndex = 0;
            DataIndex index = exampleSet.getIndex();
            for (Example example : exampleSet) {
                Arrays.fill(counter, 0);
                Collection<Number> res;
                KNNTools.extractExampleValues(example, values); //Reads attribute values from Example example to double array                    
                res = samples.getNearestValues(k + 1, values);
                double sum = 0;
                for (Number i : res) {
                    counter[i.intValue()]++;
                    sum++;
                }
                counter[(int) example.getLabel()] -= 1; //here we have to subtract distanceRate because we took k+1 neighbours 					            
                sum--; //here we have to subtract because nearest neighbors includ itself, see line above
                int mostFrequent = PRulesUtil.findMostFrequentValue(counter);
                if (example.getLabel() != mostFrequent) {
                    index.set(instanceIndex, false);
                }
                instanceIndex++;
            }            
            if (index.getLength()==index.getFullLength()){
                break;
            }            
            exampleSet.setIndex(index);   
            mainIndex.setIndex(index);
            for(int i=index.getFullLength()-1; i>-1; i--){
                if (!index.get(i)){
                    samples.remove(i);
                }
            }
        }
        return mainIndex;
    }
}
