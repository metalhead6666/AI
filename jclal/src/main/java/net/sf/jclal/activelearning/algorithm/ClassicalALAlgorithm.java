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
package net.sf.jclal.activelearning.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jclal.activelearning.querystrategy.AbstractQueryStrategy;
import net.sf.jclal.core.IClassifier;
import net.sf.jclal.core.IConfigure;
import net.sf.jclal.core.IDataset;
import net.sf.jclal.core.IEvaluation;
import net.sf.jclal.core.IScenario;
import net.sf.jclal.core.IStopCriterion;
import net.sf.jclal.core.ISystem;
import net.sf.jclal.core.ITool;
import net.sf.jclal.util.time.TimeControl;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationRuntimeException;

/**
 * Class that represents a Classical Active Learning Algorithm.
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 */
public class ClassicalALAlgorithm extends AbstractALAlgorithm implements ITool {

	private static final long serialVersionUID = 4075956096492062508L;

	/**
	 * Max of iterations, by default it is equal to 50
	 */
	private int maxIteration = 50;

	/**
	 * Current iteration
	 */
	private int iteration;

	/**
	 * Accumulative time
	 */
	private long acumulativeTime;

	/**
	 * Control time
	 */
	private TimeControl timeControl;

	/**
	 * Active Learning scenario
	 */
	private IScenario scenario;

	/**
	 * Store the stop criterion
	 */
	private List<IStopCriterion> stopCriterionList;

	/**
	 * It stores the evaluation of the current model by a passive learning
	 * approach
	 */
	private IEvaluation passiveLearningEvaluation;

	/**
	 * Returns the evaluation of the current model by passive learning
	 *
	 * @return The evaluation
	 */
	public IEvaluation getPassiveLearningEvaluation() {

		if (passiveLearningEvaluation == null) {
			executePasiveLearning();
		}

		return passiveLearningEvaluation;
	}

	/**
	 * Establishes the passive learning evaluation
	 *
	 * @param passiveLearningEvaluation
	 *            The evaluation of the passive learning
	 */
	public void setPassiveLearningEvaluation(
			IEvaluation passiveLearningEvaluation) {
		this.passiveLearningEvaluation = passiveLearningEvaluation;
	}

	/**
	 * Get the scenario used
	 *
	 * @return The Scenario used
	 */
	@Override
	public IScenario getScenario() {
		return scenario;
	}

	/**
	 * 
	 * Set the scenario
	 *
	 * @param scenario
	 *            The scenario to be used
	 */
	@Override
	public void setScenario(IScenario scenario) {
		this.scenario = scenario;
	}

	// ///////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	// ///////////////////////////////////////////////////////////////
	/**
	 * Empty (default) constructor
	 *
	 */
	public ClassicalALAlgorithm() {

		super();

		this.stopCriterionList = new ArrayList<IStopCriterion>();
		this.timeControl = new TimeControl(5);

	}

	// ///////////////////////////////////////////////////////////////
	// -------------------------------------------- Protected methods
	// ///////////////////////////////////////////////////////////////
	// Execution methods
	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public void doInit() {

		// Do Control
		doControl();
	}

	/**
	 * Execute an AL iteration
	 */
	@Override
	protected void doIterate() {

		++iteration;

		// Do training with base classifier over labeled instances
		doTraining();

		// Do selection by scenario and query strategy
		doSelectionIntances();

		// Do evaluation with base classifier over test set
		doEvaluationTest();

		// Do label the selected instances
		doLabelInstances();

		// Do update the labeled set and unlabeled instances
		doUpdateLabelledData();

		// Do control
		doControl();

		// Do clean
		doClean();
	}

	/**
	 * Returns the AL iteration
	 * 
	 * @return The current iteration
	 */
	public int getIteration() {
		return iteration;
	}

	/**
	 * Sets the AL iteration
	 * 
	 * @param iteration
	 *            The current iteration
	 */
	public void setIteration(int iteration) {
		this.iteration = iteration;
	}

	/**
	 * Label the selected instances by query strategy
	 */
	private void doLabelInstances() {
		scenario.labelInstances();
	}

	/**
	 * Executes the evaluation of the basic classifier over the test set
	 */
	private void doEvaluationTest() {

		timeControl.mark();
		scenario.evaluationTest();
		timeControl.mark();

		timeControl.timeName("Test time", timeControl.timeLastOnes());

		timeControl.timeName(
				"Iteration time",
				timeControl.time("Training time")
						+ timeControl.time("Instances selection time"));

		acumulativeTime += timeControl.time("Iteration time");
		timeControl.timeName("Accumulative iteration time", acumulativeTime);

		scenario.getQueryStrategy().getEvaluations().get(iteration - 1)
				.setIteration(iteration);

		// add the measurements to the evaluation
		Object[][] timeMeasures = timeControl.namesAndTimes();
		for (Object[] objects : timeMeasures) {
			scenario.getQueryStrategy().getEvaluations().get(iteration - 1)
					.setMetricValue((String) objects[0], (Long) objects[1]);

		}

		// reset the time
		timeControl.reset();
	}

	/**
	 * Do update the training set and unlabeled instances
	 */
	private void doUpdateLabelledData() {
		scenario.updateLabelledData();
	}

	/**
	 * Selected instances by query strategy and Scenario
	 */
	private void doSelectionIntances() {

		timeControl.mark();
		scenario.instancesSelection();
		timeControl.mark();

		timeControl.timeName("Instances selection time",
				timeControl.timeLastOnes());
	}

	/**
	 * Executes the training phase. Train the base classifier
	 */
	private void doTraining() {

		timeControl.mark();
		scenario.training();
		timeControl.mark();

		timeControl.timeName("Training time", timeControl.timeLastOnes());
	}

	/**
	 * Check if algorithm is finished
	 *
	 * By default the implementation of this method performs the following
	 * operations:
	 * <ul>
	 * <li>If number of iterations exceeds the maximum allowed, then the
	 * algorithm is stopped</li>
	 * <li>If unlabeled set is empty, then the algorithm is stopped</li>
	 * </ul>
	 */
	protected void doControl() {

		// If maximum number of iterations is exceeded, the algorithm is
		// finished
		if (iteration >= maxIteration
				|| ((AbstractQueryStrategy) (scenario.getQueryStrategy()))
						.getUnlabelledData().isEmpty()) {
			state = FINISHED;

			return;
		}

		// the extra stop criteria are verified
		for (IStopCriterion iStopCriterion : stopCriterionList) {
			if (iStopCriterion.stop(this)) {
				state = FINISHED;
				return;
			}
		}
	}

	/**
	 * Clean the memory
	 */
	protected void doClean() {

		// Run the GC
		System.gc();

	}

	/**
	 * Get the maximum number of iterations
	 * 
	 * @return The max iteration
	 */
	public int getMaxIteration() {
		return maxIteration;
	}

	/**
	 * Set the maximum number of iterations
	 *
	 * @param maxIteration
	 *            Set the max iteration
	 */
	public void setMaxIteration(int maxIteration) {
		this.maxIteration = maxIteration;
	}

	/**
	 * Set the set used as test data
	 * 
	 * @param testDataSet
	 *            The dataset to test
	 */
	@Override
	public void setTestDataSet(IDataset testDataSet) {
		scenario.getQueryStrategy().setTestData(testDataSet);
	}

	/**
	 * Get the set used as test data
	 *
	 * @return The dataset used to test
	 */
	@Override
	public IDataset getTestDataSet() {
		return scenario.getQueryStrategy().getTestData();
	}

	/**
	 * Set the set used as labeled set
	 * 
	 * @param labeledDataSet
	 *            The labeled dataset used
	 */
	@Override
	public void setLabeledDataSet(IDataset labeledDataSet) {
		scenario.getQueryStrategy().setLabelledData(labeledDataSet);
	}

	/**
	 * Set the set used as unlabeled set
	 * 
	 * @param unlabeledDataSet
	 *            The unlabeled dataset used
	 */
	@Override
	public void setUnlabeledDataSet(IDataset unlabeledDataSet) {
		scenario.getQueryStrategy().setUnlabelledData(unlabeledDataSet);
	}

	/**
	 * Get the set used as labeled set
	 * 
	 * @return The labeled dataset used
	 */
	@Override
	public IDataset getLabeledDataSet() {
		return scenario.getQueryStrategy().getLabelledData();
	}

	/**
	 * Get the set used as unlabeled set
	 * 
	 * @return The unlabeled dataset used
	 */
	@Override
	public IDataset getUnlabeledDataSet() {
		return scenario.getQueryStrategy().getUnlabelledData();
	}

	/**
	 * @param configuration
	 *            The configuration object for the classic algorithm
	 *
	 *            The XML labels supported are:
	 *            <ul>
	 *            <li>
	 *            <p>
	 *            <b>max-iteration= int</b>
	 *            </p>
	 *            </li>
	 *            <li>
	 *            <p>
	 *            <b>scenario type= class.</b>
	 *            </p>
	 *            <p>
	 *            Package: net.sf.jclal.activelearning.scenario
	 *            </p>
	 *            <p>
	 *            Class: All
	 *            </p>
	 *            </li>
	 *            <li>
	 *            <p>
	 *            <b>stop-criterio type= class.</b>
	 *            <p>
	 *            Package: net.sf.jclal.activelearning.stopcriterion
	 *            </p>
	 *            <p>
	 *            Class: All
	 *            </p>
	 *            </li>
	 *            </ul>
	 */
	@Override
	public void configure(Configuration configuration) {

		super.configure(configuration);

		// Set max iteration
		int maxIterationT = configuration.getInt("max-iteration", maxIteration);
		setMaxIteration(maxIterationT);

		// Set the stop criterion configure
		setStopCriterionConfigure(configuration);

		// Set the scenario configuration
		setScenarioConfiguration(configuration);
	}

	/**
	 * Establishes the configuration of the scenario
	 *
	 * @param configuration
	 *            The configuration object to use
	 */
	public void setScenarioConfiguration(Configuration configuration) {

		String scenarioError = "scenario type= ";
		// scenario
		try {
			// scenario classname
			String scenarioClassname = configuration
					.getString("scenario[@type]");

			scenarioError += scenarioClassname;
			// scenario class
			Class<? extends IScenario> scenarioClass = (Class<? extends IScenario>) Class
					.forName(scenarioClassname);
			// scenario instance
			IScenario scenario = scenarioClass.newInstance();
			// Configure scenario (if necessary)
			if (scenario instanceof IConfigure) {
				((IConfigure) scenario).configure(configuration
						.subset("scenario"));
			}
			// Add this scenario to the algorithm
			setScenario(scenario);
		} catch (ClassNotFoundException e) {
			throw new ConfigurationRuntimeException(
					"\nIllegal scenario classname: " + scenarioError, e);
		} catch (InstantiationException e) {
			throw new ConfigurationRuntimeException(
					"\nIllegal scenario classname: " + scenarioError, e);
		} catch (IllegalAccessException e) {
			throw new ConfigurationRuntimeException(
					"\nIllegal scenario classname: " + scenarioError, e);
		}
	}

	/**
	 * Establishes parameters for context in the algorithm
	 *
	 * @param context
	 *            The context to use
	 */
	@Override
	public void contextualize(ISystem context) {

		super.contextualize(context);

		// Attach a random generator to this object
		if (getScenario() instanceof ITool) {
			((ITool) getScenario()).contextualize(context);
		}

		// Attach a random generator to this object
		if (getScenario().getQueryStrategy() instanceof ITool) {
			((ITool) getScenario().getQueryStrategy()).contextualize(context);
		}

	}

	/**
	 * It executes the passive learning, i.e, it trains the classifiers on the
	 * whole training set and test the model on the test set
	 *
	 */
	public void executePasiveLearning() {

		try {

			IClassifier classifier = getScenario().getQueryStrategy()
					.getClassifier().makeCopy();

			IDataset trainingDataset = getLabeledDataSet().copy();

			trainingDataset.addAll(getUnlabeledDataSet());

			TimeControl con = new TimeControl(5);

			con.mark();
			classifier.buildClassifier(trainingDataset);
			con.mark();
			con.timeName("Training time", con.timeLastOnes());
			con.timeName("Iteration time", con.time("Training time"));
			con.timeName("Accumulative iteration time",
					con.time("Training time"));

			con.timeName("Instances selection time", 0);

			con.mark();
			this.passiveLearningEvaluation = classifier
					.testModel(getTestDataSet());
			con.mark();
			con.timeName("Test time", con.timeLastOnes());

			for (Object[] objects : con.namesAndTimes()) {
				this.passiveLearningEvaluation.setMetricValue(
						(String) objects[0], (Long) objects[1]);
			}

			con.destroy();
			con = null;
			classifier = null;
			trainingDataset.delete();
			trainingDataset = null;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logger.getLogger(ClassicalALAlgorithm.class.getName()).log(
					Level.SEVERE, null, e);
		}
	}

	/**
	 * Establishes the configuration of the stop criterion
	 *
	 * @param configuration
	 *            The configuration object to use
	 */
	public void setStopCriterionConfigure(Configuration configuration) {

		// Number of defined stopCriterio
		int stopCriterioValue = configuration.getList("stop-criterion[@type]")
				.size();
		// For each listener in list
		for (int i = 0; i < stopCriterioValue; i++) {
			String header = "stop-criterion(" + i + ")";
			// stopCriterio
			String stopError = "stop-criterion type= ";
			try {
				// stopCriterio classname
				String stopCriterioClassname = configuration.getString(header
						+ "[@type]");

				stopError += stopCriterioClassname;
				// stopCriterio class
				Class<? extends IStopCriterion> stopCriterioClass = (Class<? extends IStopCriterion>) Class
						.forName(stopCriterioClassname);
				// stopCriterio instance
				IStopCriterion stopCriterio = stopCriterioClass.newInstance();
				// Configure stopCriterio (if necessary)
				if (stopCriterio instanceof IConfigure) {
					((IConfigure) stopCriterio).configure(configuration
							.subset(header));
				}
				// Add this stopCriterio to the algorithm
				addStopCriterion(stopCriterio);
			} catch (ClassNotFoundException e) {
				throw new ConfigurationRuntimeException(
						"\nIllegal stopCriterion classname: " + stopError, e);
			} catch (InstantiationException e) {
				throw new ConfigurationRuntimeException(
						"\nIllegal stopCriterion classname: " + stopError, e);
			} catch (IllegalAccessException e) {
				throw new ConfigurationRuntimeException(
						"\nIllegal stopCriterion classname: " + stopError, e);
			}

		}

	}

	/**
	 * Adds a stopping criterion
	 * 
	 * @param stopCriterion
	 *            The stop criterion to add
	 */
	public void addStopCriterion(IStopCriterion stopCriterion) {
		this.stopCriterionList.add(stopCriterion);
	}

	/**
	 * Remove a stop criterion
	 *
	 * @param stopCriterion
	 *            The stop criterion to remove
	 * @return If the criterion was successfully removed
	 */
	public boolean removeStopCriterion(IStopCriterion stopCriterion) {
		return this.stopCriterionList.remove(stopCriterion);
	}

}