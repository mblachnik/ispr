/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.loss.ILossFunction;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.operator.learner.tools.PRulesUtil;
import com.rapidminer.ispr.tools.math.container.DoubleObjectContainer;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Marcin
 */
public class AllKNNInstanceSelectionGeneralModel extends AbstractInstanceSelectorModel {

    private DistanceMeasure measure;
    private int k1, k2;
    private ILossFunction loss;
    private double threshold;

    /**
     *
     * @param measure
     * @param k1
     * @param k2
     */
    public AllKNNInstanceSelectionGeneralModel(DistanceMeasure measure, int k1, int k2, ILossFunction loss) {
        this.measure = measure;
        this.k1 = k1;
        this.k2 = k2;
        this.threshold = threshold;
        this.loss = loss;
    }

    /**
     *
     * @param inputExampleSet
     * @return
     */
    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) {
        Attributes attributes = exampleSet.getAttributes();
        DataIndex index = exampleSet.getIndex();
        Attribute label = attributes.getLabel();

        //DATA STRUCTURE PREPARATION
        ISPRGeometricDataCollection<Number> samples;
        samples = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, exampleSet, measure);
        //All - kNN EDITTING
        loss.init(exampleSet);
        
        if (label.isNominal()) {
            int[] counter;
            int instanceIndex = 0;
            double predictedLabel;
            counter = new int[label.getMapping().size()];
            Iterator<double[]> samplesIterator = samples.samplesIterator();
            Iterator<Number> labelsIterator = samples.storedValueIterator();
            while (samplesIterator.hasNext() && labelsIterator.hasNext()) {
                double realLabel = labelsIterator.next().doubleValue();
                double[] values = samplesIterator.next();
                Arrays.fill(counter, 0);
                Collection<DoubleObjectContainer<Number>> res = samples.getNearestValueDistances(k2, values);
                int k = 0;
                for (DoubleObjectContainer<Number> it : res) {
                    int i = it.getSecond().intValue();
                    counter[i]++;
                    if (k > k1) {
                        predictedLabel = PRulesUtil.findMostFrequentValue(counter);                        
                        if (loss.getLoss(realLabel,predictedLabel,values) > 0) {
                            index.set(instanceIndex, false);
                        }
                    }
                    k++;
                }
                instanceIndex++;
            }
        } else if (label.isNumerical()) {
            int instanceIndex = 0;
            double predictedLabel;
            double sum;
            Iterator<double[]> samplesIterator = samples.samplesIterator();
            Iterator<Number> labelsIterator = samples.storedValueIterator();
            while (samplesIterator.hasNext() && labelsIterator.hasNext()) {
                double realLabel = labelsIterator.next().doubleValue();
                double[] values = samplesIterator.next();
                sum = 0;
                Collection<DoubleObjectContainer<Number>> res = samples.getNearestValueDistances(k2, values);
                int k = 0;
                for (DoubleObjectContainer<Number> it : res) {
                    sum += it.getSecond().doubleValue();
                    if (k > k1) {
                        predictedLabel = sum / k;                        
                        if (loss.getLoss(realLabel,predictedLabel,values) > 0) {
                            index.set(instanceIndex, false);
                        }
                    }
                    k++;
                }
                instanceIndex++;
            }
        }
        return index;
    }
}
