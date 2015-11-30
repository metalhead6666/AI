/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sf.jclal.activelearning.singlelabel.querystrategy;

import weka.core.Instance;
import weka.core.Utils;

/**
 * Implementation of Margin Sampling Strategy (Uncertainty Sampling) query
 * strategy.
 *
 * Margin sampling is a variant of uncertainty sampling that take into
 * consideration the information about of the first and second most probable
 * class under the model.
 *
 * Burr Settles. Active Learning Literature Survey. Computer Sciences Technical
 * Report 1648, University ofWisconsin–Madison. 2009.
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Maria del Carmen Rodriguez Hernandez
 * @author Eduardo Perez Perdomo
 *
 */
public class MarginSamplingQueryStrategy extends UncertaintySamplingQueryStrategy {

    private static final long serialVersionUID = 5715639689715848049L;

    /**
     * Manufacturer for defect.
     */
    public MarginSamplingQueryStrategy() {

        setMaximal(false);
    }

    /*
     * The least confident strategy calculate for each instance Px - Py, being
     * Px and Py the maximals probabilities.
     */
    @Override
    public double utilityInstance(Instance instance) {

        double[] probs = distributionForInstance(instance);

        //determine the class with the highest probability
        int ind1 = Utils.maxIndex(probs);
        double max1 = probs[ind1];
        probs[ind1] = 0;

        //determine the second class with the highest probability
        int ind2 = Utils.maxIndex(probs);
        double max2 = probs[ind2];

        return max1 - max2;
    }
}
