/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models.old;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.PRulesModel;
import com.rapidminer.ispr.operator.learner.loss.ClassLoss;
import com.rapidminer.ispr.operator.learner.loss.ILossFunction;
import com.rapidminer.ispr.operator.learner.selection.models.ENNInstanceSelectionModel;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.operator.learner.tools.PRulesUtil;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import static com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes.CACHED_LINEAR_SEARCH;
import static com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes.LINEAR_SEARCH;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.ispr.tools.math.container.SimpleNNCachedLineraList;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Marcin
 */
public class RENNInstanceSelectionModel_OLD implements PRulesModel<ExampleSet> {

    private int k;
    private DistanceMeasure measure;

    /**
     *
     * @param measure
     * @param k
     */
    public RENNInstanceSelectionModel_OLD(DistanceMeasure measure, int k) {
        this.k = k;
        this.measure = measure;
    }

    /**
     *
     * @param exampleSet
     * @return
     */
    @Override
    public ExampleSet run(ExampleSet exampleSet) {
        int initSize, tInitSize = exampleSet.size();        
         ILossFunction loss = new ClassLoss();
         loss.setThreshold(0);
         ENNInstanceSelectionModel m = new ENNInstanceSelectionModel(measure, k, loss);                
         do {
         initSize = tInitSize;
         exampleSet = m.run(exampleSet);
         } while ((tInitSize = exampleSet.size()) != initSize);
         return exampleSet;
    }
}
