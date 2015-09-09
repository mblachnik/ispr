/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.PRulesModel;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;

/**
 *
 * @author Marcin
 */
public abstract class AbstractInstanceSelectorModel implements PRulesModel<ExampleSet> {

    /**
     *
     * @param inputExampleSet
     * @return
     */
    @Override
    public ExampleSet run(ExampleSet inputExampleSet) {
        SelectedExampleSet exampleSet;
        if (inputExampleSet instanceof SelectedExampleSet) {
            exampleSet = (SelectedExampleSet) inputExampleSet.clone();
        } else {
            exampleSet = new SelectedExampleSet(inputExampleSet);
        }
        DataIndex index = selectInstances(exampleSet);
        //UWAGA tak trzeba zrobiæ bo mo¿e siê zdarzyæ ¿e któryœ z algorytmów selekcji sam wybierze wektory i wówczas nie bêdzie mo¿na zrobiæ setIndex bo nie bêd¹ siê zgadza³y indeksy
        if (inputExampleSet instanceof SelectedExampleSet) {
            exampleSet = (SelectedExampleSet) inputExampleSet.clone();
        } else {
            exampleSet = new SelectedExampleSet(inputExampleSet);
        }
        exampleSet.setIndex(index);
        return exampleSet;
    }

    public abstract DataIndex selectInstances(SelectedExampleSet exampleSet);
}
