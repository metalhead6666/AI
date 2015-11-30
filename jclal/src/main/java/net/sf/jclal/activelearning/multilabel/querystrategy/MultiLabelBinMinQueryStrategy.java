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
package net.sf.jclal.activelearning.multilabel.querystrategy;

import java.util.logging.Level;
import java.util.logging.Logger;
import mulan.transformations.BinaryRelevanceTransformation;
import net.sf.jclal.classifier.SMO;
import net.sf.jclal.classifier.MulanClassifier;
import net.sf.jclal.classifier.ParallelBinaryRelevance;
import weka.classifiers.Classifier;
import weka.core.Instance;

/**
 * Implementation of BinMin active strategy. See for more information Brinker,
 * K. (2006). On active learning in Multi-label Classification.
 *
 * @author Oscar Gabriel Reyes Pupo
 *
 */
public class MultiLabelBinMinQueryStrategy extends
        AbstractMultiLabelQueryStrategy {

    /**
     * Empty (default) constructor
     */
    public MultiLabelBinMinQueryStrategy() {

        super();

        setMaximal(false);
    }

    private static final long serialVersionUID = 1L;
    
    /**
     * Returns the utility of the instance.
     * 
     * @param instance The instance to test
     * @return The utility of the instance
     */
    @Override
    public double utilityInstance(Instance instance) {

        if (!(((MulanClassifier) getClassifier()).getInternalClassifier() instanceof ParallelBinaryRelevance)) {
            System.err
                    .println("The BinMin strategy must be configured with the Paralle Binary Relevance algorithm");
        }

        ParallelBinaryRelevance learner = (ParallelBinaryRelevance) ((MulanClassifier) getClassifier()).getInternalClassifier();

        // One SVM classiier for each label
        Classifier[] smos = learner.getEnsemble();

        if (!(smos[0] instanceof SMO)) {
            System.err
                    .println("The base classifiers of the Binary Relevance algorithm on the BinMin strategy must be SVM");
        }

        BinaryRelevanceTransformation brt = learner.getBrt();

        double min = Double.MAX_VALUE;

        for (int l = 0; l < getNumLabels(); l++) {

            double result;

            try {

                Instance transformedInstance = brt.transformInstance(instance,
                        l);

                result = Math.abs(((SMO) smos[l]).SVMOutput(transformedInstance));

                if (result < min) {
                    min = result;
                }

            } catch (Exception e) {

                Logger.getLogger(MultiLabelBinMinQueryStrategy.class.getName()).log(
                        Level.SEVERE, null, e);
            }
        }

        return min;

    }

}
