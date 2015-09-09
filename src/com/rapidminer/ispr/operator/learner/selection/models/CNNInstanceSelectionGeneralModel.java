/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models;

import com.rapidminer.example.Example;
import com.rapidminer.example.set.EditedExampleSet;
import com.rapidminer.example.set.ISPRExample;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.operator.learner.loss.ILossFunction;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Collection;

/**
 *
 * @author Marcin
 */
public class CNNInstanceSelectionGeneralModel extends AbstractInstanceSelectorModel {

    private final DistanceMeasure distance;
    private final ILossFunction loss;    

    /**
     *
     * @param distance
     * @param loss
     * @param randomGenerator
     */
    public CNNInstanceSelectionGeneralModel(DistanceMeasure distance, ILossFunction loss) {
        this.distance = distance;
        this.loss = loss;        
    }
    
    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) {
        loss.init(exampleSet);
        EditedExampleSet selectedSet = new EditedExampleSet(exampleSet);
        EditedExampleSet trainingSet = new EditedExampleSet(exampleSet);

        DataIndex trainingIndex = trainingSet.getIndex();
        DataIndex selectedIndex = selectedSet.getIndex();
        selectedIndex.setAllFalse();
        int i = 0;
        selectedIndex.set(i, true);
        trainingIndex.set(i, false);        
        ISPRGeometricDataCollection<Number> nn = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, selectedSet, distance);
        boolean isModiffied = true;
        int attributeSize = exampleSet.getAttributes().size();
        double[] firstValues = new double[attributeSize];

        while (isModiffied) {
            isModiffied = false;
            for (Example firstInstance : trainingSet) {
                KNNTools.extractExampleValues(firstInstance, firstValues);
                Collection<Number> result = nn.getNearestValues(1, firstValues);
                double realLabel = firstInstance.getLabel();
                double predictedLabel = result.iterator().next().doubleValue();                
                if (loss.getLoss(realLabel,predictedLabel,firstValues)> 0) {
                    i = ((ISPRExample) firstInstance).getIndex();
                    selectedIndex.set(i, true);
                    trainingIndex.set(i, false);
                    nn.add(firstValues.clone(), realLabel);
                    isModiffied = true;
                }
            }
        }
        return selectedIndex;
    }
}
