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
        //UWAGA tak trzeba zrobi� bo mo�e si� zdarzy� �e kt�ry� z algorytm�w selekcji sam wybierze wektory i w�wczas nie b�dzie mo�na zrobi� setIndex bo nie b�d� si� zgadza�y indeksy
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
