/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.operator.learner.loss.ILossFunction;
import com.rapidminer.ispr.operator.learner.tools.PRulesUtil;
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
public class ENNInstanceSelectionModel extends AbstractInstanceSelectorModel {

    private final DistanceMeasure measure;
    private final int k;
    private boolean storeConfidence = false;
    private double[] confidence;
    private final GeometricCollectionTypes knnType = GeometricCollectionTypes.LINEAR_SEARCH;
    private final ILossFunction loss;
    private double[] classWeight;

    /**
     *
     * @param measure
     * @param k
     * @param loss
     */
    public ENNInstanceSelectionModel(DistanceMeasure measure, int k, ILossFunction loss) {
        this.measure = measure;
        this.k = k;
        this.loss = loss;
        this.classWeight = null;
    }

    /**
     *
     * @param measure
     * @param k
     * @param loss
     * @param classWeight
     */
    public ENNInstanceSelectionModel(DistanceMeasure measure, int k, ILossFunction loss, double[] classWeight) {
        this.measure = measure;
        this.k = k;
        this.loss = loss;
        this.classWeight = classWeight;
    }

    /**
     *
     * @param exampleSet
     * @return
     */
    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) {
        Attributes attributes = exampleSet.getAttributes();
        DataIndex index = exampleSet.getIndex();
        Attribute label = attributes.getLabel();

        int sampleSize = exampleSet.size();
        //DATA STRUCTURE PREPARATION        
        ISPRGeometricDataCollection<Number> samples;
        samples = KNNTools.initializeKNearestNeighbourFactory(knnType, exampleSet, measure);
        loss.init(samples);
        if (storeConfidence) {
            confidence = new double[sampleSize];
        }
        //ENN EDITTING
        double[] values;
        double realLabel;
        double predictedLabel = 0;

        int instanceIndex = 0;
        Iterator<double[]> sampleIterator = samples.samplesIterator();
        Iterator<Number> labelIterator = samples.storedValueIterator();
        if (label.isNominal()) {
            if (this.classWeight == null) {
                this.classWeight = new double[label.getMapping().size()];
                for (int i = 0; i < classWeight.length; i++) {
                    classWeight[i] = 1.0d;
                }
            }
            int numberOfClasses = label.getMapping().size();
            double[] counter = new double[numberOfClasses];
            while (sampleIterator.hasNext() && labelIterator.hasNext()) {
                Arrays.fill(counter, 0);
                Collection<Number> res;
                values = sampleIterator.next();
                realLabel = labelIterator.next().doubleValue();
                res = samples.getNearestValues(k + 1, values);
                double sum = 0;
                for (Number i : res) {
                    counter[i.intValue()] += classWeight[i.intValue()];
                    sum += classWeight[i.intValue()];
                }
                counter[(int) realLabel] -= classWeight[(int) realLabel]; //here we have to subtract distanceRate because we took k+1 neighbours 					            
                sum -= classWeight[(int) realLabel]; //here we have to subtract because nearest neighbors includ itself, see line above
                predictedLabel = PRulesUtil.findMostFrequentValue(counter);
                if (storeConfidence) {
                    confidence[instanceIndex] = counter[(int) predictedLabel] / sum;
                }                           
                if (loss.getLoss(realLabel,predictedLabel,values)> 0) {
                    index.set(instanceIndex, false);
                }
                instanceIndex++;
            }
        } else if (label.isNumerical()) {
            while (sampleIterator.hasNext() && labelIterator.hasNext()) {
                predictedLabel = 0;
                Collection<Number> res;
                values = sampleIterator.next();
                realLabel = labelIterator.next().doubleValue();
                res = samples.getNearestValues(k + 1, values);
                double sum = 0;
                for (Number i : res) {
                    predictedLabel += i.doubleValue();
                    sum++;
                }
                predictedLabel -= realLabel;  //here we have to subtract distanceRate because we took k+1 neighbours 					            
                sum--; //here we have to subtract because nearest neighbors includ itself, see line above                
                predictedLabel /= sum;
                if (loss.getLoss(realLabel,predictedLabel,values)> 0) {
                    index.set(instanceIndex, false);
                }
                instanceIndex++;
            }
        }
        return index;

    }

    public double[] getConfidence() {
        return confidence;
    }

    public boolean isStoreConfidence() {
        return storeConfidence;
    }

    public void setStoreConfidence(boolean storeConfidence) {
        this.storeConfidence = storeConfidence;
    }
}
