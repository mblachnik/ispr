/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models;

import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.loss.ILossFunction;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 *
 * @author Marcin
 */
public class EditedDistanceGraphModel extends AbstractInstanceSelectorModel {

    private final DistanceMeasure distance;
    private final EditedDistanceGraphCriteria criteria;
    private final ILossFunction loss;

    /**
     *
     * @param distance
     * @param criteria
     * @param loss
     */
    public EditedDistanceGraphModel(DistanceMeasure distance, EditedDistanceGraphCriteria criteria, ILossFunction loss) {
        this.distance = distance;
        this.criteria = criteria;
        this.loss = loss;
    }

    /**
     *
     * @param exampleSet
     * @return
     */
    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) {
        int size = exampleSet.size();
        ISPRGeometricDataCollection<Number> samples;
        samples = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, exampleSet, distance);
//        ArrayList<double[]> samples = new ArrayList<double[]>(exampleSet.size());
//        ArrayList<Number> labels = new ArrayList<Number>(exampleSet.size());
        int numberOfAttrbutes = exampleSet.getAttributes().size();
        loss.init(exampleSet);
//        for (Example ex : exampleSet) {
//            double[] values = new double[numberOfAttrbutes];
//            KNNTools.extractExampleValues(ex, values);
//            samples.add(values);
//            labels.add(ex.getLabel());
//        }        
        DataIndex indexA = new DataIndex(size);
        indexA.setAllFalse();
        for (int iA = 0; iA < size; iA++) {
            for (int iB = 0; iB < size; iB++) {
                if (iB == iA) continue;
                double labelA = samples.getStoredValue(iA).doubleValue();
                double labelB = samples.getStoredValue(iB).doubleValue();
                if (loss.getLoss(labelA,labelB,samples.getSample(iB)) > 0) {
                    boolean chk = true;
                    double dAB = distance.calculateDistance(samples.getSample(iA), samples.getSample(iB));
                    for (int iC = 0; iC < size; iC++) {
                        if (iC == iA || iC == iB) continue;
                        double dAC = distance.calculateDistance(samples.getSample(iA), samples.getSample(iC));
                        double dBC = distance.calculateDistance(samples.getSample(iB), samples.getSample(iC));
                        if (criteria.evaluate(dAB, dAC, dBC)) {
                            chk = false;
                            break;
                        }
                    }
                    if (chk) {
                        indexA.set(iA, true);
                        indexA.set(iB, true);
                    }                    
                }
            }
        }                
        return indexA;


/*
        EditedExampleSet exampleSetB = new EditedExampleSet(exampleSet);
        EditedExampleSet exampleSetC = new EditedExampleSet(exampleSet);
        DataIndex indexA = new DataIndex(size);
        indexA.setAllFalse();
        DataIndex indexB = exampleSetB.getIndex();
        DataIndex indexC = exampleSetC.getIndex();
        int counterA = 0;
        int counterB;
        for (Example exampleA : exampleSet) {
            double labelA = exampleA.getLabel();
            ISPRExample exampleAA = (ISPRExample) exampleA;
            int exampleAId = exampleAA.getIndex();
            indexB.set(exampleAId, false);
            indexC.set(exampleAId, false);
            counterB = counterA + 1;
            for (Example exampleB : exampleSetB) {
                if (exampleB.getLabel() != labelA) {
                    ISPRExample exampleBB = (ISPRExample) exampleB;
                    indexC.set(exampleBB.getIndex(), false);
                    boolean chk = true;
                    double dAB = distance.calculateDistance(exampleA, exampleB);
                    for (Example exampleC : exampleSetC) {
                        double dAC = distance.calculateDistance(exampleA, exampleC);
                        double dBC = distance.calculateDistance(exampleB, exampleC);
                        if (criteria.evaluate(dAB, dAC, dBC)) {
                            chk = false;
                            break;
                        }
                    }
                    if (chk) {
                        indexA.set(counterA, true);
                        indexA.set(counterB, true);
                    }
                    indexC.set(exampleBB.getIndex(), true);
                }
                counterB++;
            }
            indexC.set(exampleAId, true);
            counterA++;
        }
        */
    }
}
