/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models;

import com.rapidminer.ispr.operator.learner.PRulesModel;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.loss.ILossFunction;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 *
 * @author Marcin
 */
public class GEInstanceSelectionModel extends AbstractInstanceSelectorModel implements EditedDistanceGraphCriteria {

    private AbstractInstanceSelectorModel model;

    /**
     * 
     * @param distance
     */
    public GEInstanceSelectionModel(DistanceMeasure distance, ILossFunction loss) {
        this.model = new EditedDistanceGraphModel(distance,this, loss);
    }

    /**
     * 
     * @param inputExampleSet
     * @return
     */
    @Override
    public DataIndex selectInstances(SelectedExampleSet inputExampleSet) {
        return model.selectInstances(inputExampleSet);        
    }

    @Override
    public boolean evaluate(double a, double b, double c) {        
        a *= a;
        b *= b;
        c *= c;
        return a > b + c;
    }
  
}
