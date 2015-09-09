package com.rapidminer.ispr.operator.learner.optimization.supervised;

import java.util.Map;

/**
 * 
 * @author Marcin
 */
public enum LVQTypes {

    LVQ1, LVQ2, LVQ21, LVQ3, OLVQ, WLVQ, SLVQ, 
    //MyLVQ2, MyLVQ21, MyLVQ3, 
    LVQ21SGD, WTM_LVQ, GLVQ;
            
    public static final String PARAMETER_LVQ_TYPE = "LVQ Type";
    
    public static String[] lvqTypes() {
        LVQTypes[] fields = LVQTypes.values();
        String[] names = new String[fields.length];
        int i = 0;
        for (LVQTypes value : fields) {
            names[i] = value.name();
            i++;
        }
        return names;
    }
}
