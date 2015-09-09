/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2012 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.ispr.tools.math.container;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.RandomAccess;
import com.rapidminer.ispr.operator.learner.tools.SymetricDoubleMatrix;
import com.rapidminer.tools.container.Tupel;
import com.rapidminer.tools.math.container.BoundedPriorityQueue;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 * This class is an implementation of the GeometricDataCollection interface,
 * which searches all datapoints linearly for the next k neighbours. Hence O(n)
 * computations are required for this operation. With extra methods it allows to
 * use internal cache
 *
 * @author Sebastian Land
 *
 * @param <T> This is the type of value with is stored with the points and
 * retrieved on nearest neighbour search
 */
public class SimpleNNCachedLineraList<T extends Serializable> implements ISPRCachedGeometricDataCollection<T>, RandomAccess {

    private static final long serialVersionUID = -746048910140779285L;
    DistanceMeasure distance;
    ArrayList<double[]> samples;
    ArrayList<T> storedValues;
    SymetricDoubleMatrix distanceCache;
    int index = -1;

    public SimpleNNCachedLineraList(DistanceMeasure distance, int n) {
        this.distance = distance;
        samples = new ArrayList<double[]>(n);
        storedValues = new ArrayList<T>(n);
        //int cacheSize  = (n*n + n)/2;                
        distanceCache = new SymetricDoubleMatrix(n);
    }

    @Override
    public void add(double[] values, T storeValue) {
        index++;
        this.samples.add(values);
        this.storedValues.add(storeValue);
        int i = 0;
        for (double[] sample : samples) {
            double dist = distance.calculateDistance(sample, values);
            distanceCache.set(i, index, dist);
            i++;
        }
    }

    @Override
    public Collection<T> getNearestValues(int k, double[] values) {
        Collection<T> result = new ArrayList<T>(k);
        if (k > 1) {
            BoundedPriorityQueue<Tupel<Double, T>> queue = new BoundedPriorityQueue<Tupel<Double, T>>(k);
            int i = 0;
            for (double[] sample : this.samples) {
                double dist = distance.calculateDistance(sample, values);
                queue.add(new Tupel<Double, T>(dist, storedValues.get(i)));
                i++;
            }
            for (Tupel<Double, T> tupel : queue) {
                result.add(tupel.getSecond());
            }
        } else {
            int i = 0;
            double minDist = Double.MAX_VALUE;
            T subResult = null;
            for (double[] sample : this.samples) {
                double dist = distance.calculateDistance(sample, values);
                if (dist < minDist) {
                    minDist = dist;
                    subResult = storedValues.get(i);
                }
                i++;
            }
            result.add(subResult);
        }
        return result;
    }

    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(int k, double[] values) {
        BoundedPriorityQueue<DoubleObjectContainer<T>> queue = new BoundedPriorityQueue<DoubleObjectContainer<T>>(k);
        int i = 0;
        for (double[] sample : this.samples) {
            double dist = distance.calculateDistance(sample, values);
            queue.add(new DoubleObjectContainer<T>(dist, storedValues.get(i)));
            i++;
        }
        return queue;
    }

    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, double[] values) {
        ArrayList<DoubleObjectContainer<T>> queue = new ArrayList<DoubleObjectContainer<T>>();
        int i = 0;
        for (double[] sample : this.samples) {
            double currentDistance = distance.calculateDistance(sample, values);
            if (currentDistance <= withinDistance) {
                queue.add(new DoubleObjectContainer<T>(currentDistance, storedValues.get(i)));
            }
            i++;
        }
        return queue;
    }

    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, int butAtLeastK, double[] values) {
        Collection<DoubleObjectContainer<T>> result = getNearestValueDistances(withinDistance, values);
        if (result.size() < butAtLeastK) {
            return getNearestValueDistances(butAtLeastK, values);
        }
        return result;
    }

    @Override
    public Collection<T> getNearestValues(int k, int index) {
        Collection<T> result = new ArrayList<T>(k);
        if (k > 1) {
            BoundedPriorityQueue<DoubleObjectContainer<T>> queue = new BoundedPriorityQueue<DoubleObjectContainer<T>>(k);
            int i = 0;
            for (double[] sample : this.samples) {
                double dist = distanceCache.get(index, i);
                queue.add(new DoubleObjectContainer<T>(dist, storedValues.get(i)));
                i++;
            }
            for (DoubleObjectContainer<T> tupel : queue) {
                result.add(tupel.getSecond());
            }
        } else {
            int i = 0;
            double minDist = Double.MAX_VALUE;
            T subResult = null;
            for (double[] sample : this.samples) {
                double dist = distanceCache.get(index, i);
                if (dist < minDist) {
                    minDist = dist;
                    subResult = storedValues.get(i);
                }
                i++;
            }
            result.add(subResult);
        }
        return result;
    }

    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(int k, int index) {
        BoundedPriorityQueue<DoubleObjectContainer<T>> queue = new BoundedPriorityQueue<DoubleObjectContainer<T>>(k);
        int i = 0;
        for (double[] sample : this.samples) {
            double dist = distanceCache.get(index, i);
            queue.add(new DoubleObjectContainer<T>(dist, storedValues.get(i)));
            i++;
        }
        return queue;
    }

    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, int index) {
        ArrayList<DoubleObjectContainer<T>> queue = new ArrayList<DoubleObjectContainer<T>>();
        int i = 0;
        for (double[] sample : this.samples) {
            double currentDistance = distanceCache.get(index, i);
            if (currentDistance <= withinDistance) {
                queue.add(new DoubleObjectContainer<T>(currentDistance, storedValues.get(i)));
            }
            i++;
        }
        return queue;
    }

    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, int butAtLeastK, int index) {
        Collection<DoubleObjectContainer<T>> result = getNearestValueDistances(withinDistance, index);
        if (result.size() < butAtLeastK) {
            return getNearestValueDistances(butAtLeastK, index);
        }
        return result;
    }

    @Override
    public int size() {
        return samples.size();
    }

    @Override
    public T getStoredValue(int index) {
        return storedValues.get(index);
    }

    @Override
    public double[] getSample(int index) {
        return samples.get(index);
    }

    @Override
    public void remove(int n) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Iterator<T> storedValueIterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<double[]> samplesIterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setSample(int index, double[] sample, T storedValue) {
        samples.set(index, sample);
        storedValues.set(index, storedValue);
        int i=0;
        for (double[] values : samples) {
            double dist = distance.calculateDistance(values, sample);
            distanceCache.set(i, index, dist);
            i++;
        }
    }
}
