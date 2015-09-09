/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.loss;

import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.EqualTypeCondition;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marcin
 */
public class LossFunctionHelper {

    static final ILossFunction[] lossClassList = {new ClassLoss(), new ThresholdLinearLoss(), new ThresholdRelativeLinearLoss(), new LocalThresholdLinearLoss(), new LocalThresholdRelativeLinearLoss()};
    static final String[] lossStringList = new String[lossClassList.length];
    public static final String PARAMETER_THRESHOLD = "Threshold";
    public static final String PARAMETER_LOSS = "Loss type";

    static {
        for (int i = 0; i < lossClassList.length; i++) {
            lossStringList[i] = lossClassList[i].getLossName();
        }
    }

    public static ILossFunction[] getLossList() {
        return lossClassList;
    }

    public static String[] getLossNameList() {
        return lossStringList;
    }

    public static List<ParameterType> getLossParameters(Operator operator) {
        List<ParameterType> types = new ArrayList<ParameterType>();
        ParameterType type;
        type = new ParameterTypeCategory(PARAMETER_LOSS, "Loss function", LossFunctionHelper.getLossNameList(), 0);        
        types.add(type);
        
        type = new ParameterTypeDouble(PARAMETER_THRESHOLD, "The loss function threshold acceptance value", 0, Double.MAX_VALUE, 0.1);
        type.setExpert(false);
        String[] s = LossFunctionHelper.getLossNameList();
        type.registerDependencyCondition(new EqualTypeCondition(operator, PARAMETER_LOSS, s, false, 1,2));
        types.add(type);

        return types;
    }

    public static ILossFunction getConfiguredLoss(Operator operator) {
        int lossId;
        ILossFunction loss = null;
        try {
            lossId = operator.getParameterAsInt(PARAMETER_LOSS);
            loss = LossFunctionHelper.getLossList()[lossId];
            loss = loss.getClass().newInstance();            
            double threshold = 0;
            if (loss instanceof ClassLoss ){
                threshold = 0;
            } else {
                threshold = operator.getParameterAsDouble(PARAMETER_THRESHOLD);
                operator.getParameterAsDouble(PARAMETER_THRESHOLD);
            }
            loss.setThreshold(threshold);
        } catch (UndefinedParameterError e) {
            throw new RuntimeException("Error in loss function: UndefinedParameterError");
        } catch (InstantiationException e) {
            throw new RuntimeException("Error in loss function: InstantiationException");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error in loss function: IllegalAccessException");
        }
        return loss;
    }
}
