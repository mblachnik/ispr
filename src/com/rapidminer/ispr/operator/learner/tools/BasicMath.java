/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.tools;


/**
 *
 * @author Marcin
 */
public class BasicMath {

/*
    public static void printLabeledData(LabeledDataInterface d) {
        int anX = d.getXAttributesNumber();
        int anY = d.getYAttributesNumber();
        int dn = d.getVectorsNumber();
        for (int i = 0; i < dn; i++) {
            System.out.print("X = ");
            for (int j = 0; j < anX; j++) {
                System.out.print(d.getXElement(i, j) + " ");
            }
            System.out.print("Y = ");
            for (int j = 0; j < anY; j++) {
                System.out.print(d.getYElement(i, j) + " ");
            }
            System.out.print("\n");
        }
    }
*/
    /**
     * 
     * @param X
     * @param Y
     */
    public static void sort(double[] X, double[] Y) {
        double tmpX, tmpY;
        int size = X.length;
        for (int j = 0; j < X.length; j++) {
            for (int i = 1; i < size; i++) {
                if (X[i] < X[i - 1]) {
                    tmpX = X[i - 1];
                    X[i - 1] = X[i];
                    X[i] = tmpX;
                    tmpY = Y[i - 1];
                    Y[i - 1] = Y[i];
                    Y[i] = tmpY;
                }
            }
            size--;
        }
    }

    /**
     * Ta sama funkcja co wyżej z tą różnicą że drugi wektor jest typu int
     * @param X - dane poddawane sortowaniu
     * @param Y - dane które również ulegają sortowaniu wg. sortowania X
     */
    public static void sort(double[] X, int[] Y) {
        double tmpX;
        int tmpY;
        int size = X.length;
        for (int j = 0; j < X.length; j++) {
            for (int i = 1; i < size; i++) {
                if (X[i] < X[i - 1]) {
                    tmpX = X[i - 1];
                    X[i - 1] = X[i];
                    X[i] = tmpX;
                    tmpY = Y[i - 1];
                    Y[i - 1] = Y[i];
                    Y[i] = tmpY;
                }
            }
            size--;
        }
    }
        
    /**
     * 
     * @param X
     * @param start
     * @param end
     * @return
     */
    public static double mean(double[] X, int start, int end){
        double mean = 0;
        for (int i=start;i<=end;i++)
            mean += X[i];
        mean /= end-start+1;
        return mean;
    }
    
    /**
     * 
     * @param X
     * @return
     */
    public static double mean(double[] X){
        return mean(X,0,X.length-1);
    }

    /**
     * 
     * @param X
     * @param mean
     * @return
     */
    public static double simpleVariance(double[] X, double mean){
        double var = 0;
        for (int i=0; i<X.length; i++)
            var += (X[i] - mean)*(X[i] - mean);
        return var;
    }

    /**
     * 
     * @param X
     * @param mean
     * @param start
     * @param end
     * @return
     */
    public static double var(double[] X, double mean, int start, int end){
        double var = 0;        
        for (int i=start; i<=end; i++)
            var += (X[i] - mean)*(X[i] - mean);
        //if (start != end)
        var /= end-start+1;
        var = Math.sqrt(var);
        return var;
    }

    /**
     * 
     * @param X
     * @param start
     * @param end
     * @return
     */
    public static double var(double[] X, int start, int end){
        double m = mean(X,start,end);
        return var(X, m, start, end);
    }

    /**
     * 
     * @param X
     * @return
     */
    public static double var(double[] X){
        return var(X,0,X.length-1);
    }

    /**
     * 
     * @param b
     * @return
     */
    public static boolean[] not(boolean[] b){
        for (int i = 0; i<b.length; i++)
            b[i] = !b[i];
        return b;
    }

    /**
     * 
     * @param b
     * @return
     */
    public static int sum(boolean[] b){
        int sum = 0;
        for (int i = 0; i<b.length; i++)
            if (b[i]) sum++;
        return sum;
    }

    /**
     * 
     * @param b
     * @return
     */
    public static double sum(double[] b){
        double sum = 0;
        for (int i = 0; i<b.length; i++)
            sum += b[i];
        return sum;
    }

    /**
     * 
     * @param b
     * @return
     */
    public static double sum(float[] b){
        double sum = 0;
        for (int i = 0; i<b.length; i++)
            sum += b[i];
        return sum;
    }

    /**
     * 
     * @param b
     * @return
     */
    public static int sum(int[] b){
        int sum = 0;
        for (int i = 0; i<b.length; i++)
            sum += b[i];
        return sum;
    }

    /**
     * 
     * @param b
     * @return
     */
    public static long sum(long[] b){
        long sum = 0;
        for (int i = 0; i<b.length; i++)
            sum += b[i];
        return sum;
    }

    /**
     * 
     * @param x1
     * @param x2
     * @return
     */
    public static double[] concatenate(double[] x1, double[] x2){
        double[] y = new double[x1.length + x2.length];
        System.arraycopy(x1, 0, y, 0, x1.length);
        System.arraycopy(x2, 0, y, x1.length, x2.length);
        return y;
    }
    
    public static double log2(double x){        
        return Math.log(x)/Math.log(2);
    }
    
    
    public static double sigmoid(double x){
        return 1.0/(1 + Math.exp(-x));
    }
/*
    public static double classificationAccuracy(IndexedData y1, IndexedData y2){
        if (y1.getRowsNumber() != y2.getRowsNumber()){
            System.err.println("Incorect data sizes y1 and y2");
            return 0;
        }
        double acc = 0;
        for (int i=0; i<y1.getRowsNumber(); i++)
            acc += (Math.round(y2.getElement(i, 0)) == y1.getElement(i, 0)) ? 1 : 0;
        acc /= y1.getRowsNumber();
        return acc;
    }
 */
    public static void main(String[] args){
        for (double i = -1; i < 1; i+=0.01) {
            System.out.println(sigmoid(i));
        }
    }
}