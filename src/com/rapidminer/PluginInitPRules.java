/*
 *  RapidMiner PRules Extension
 *
 *  Copyright (C) 2001-2011 by Marcin Blachnik and the contributors
 *
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
package com.rapidminer;

import java.io.InputStream;
import java.util.Properties;

import com.rapidminer.gui.MainFrame;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import com.rapidminer.ispr.tools.math.similarity.numerical.NormalizedManhattanDistance;

/**
 * This class provides hooks for initialization
 *
 * @author Sebastian Land
 */
public class PluginInitPRules {

    /**
     *
     * @param mainframe
     */
    public static void initGui(MainFrame mainframe) {
    }

    /**
     *
     * @param loader
     * @return
     */
    public static InputStream getOperatorStream(ClassLoader loader) {
        return null;
    }

    /**
     *
     */
    public static void initPluginManager() {
        DistanceMeasures.registerMeasure(DistanceMeasures.NUMERICAL_MEASURES_TYPE, "Normalized Manhattan Distance", NormalizedManhattanDistance.class);	        
    }

    /**
     *
     */
    public static void initFinalChecks() {
    }

    /**
     *
     */
    public static void initSplashTexts() {
    }

    /**
     *
     * @param aboutBoxProperties
     */
    public static void initAboutTexts(Properties aboutBoxProperties) {
    }

    /**
     *
     * @return
     */
    public static Boolean showAboutBox() {
        return true;
    }
}
