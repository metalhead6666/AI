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
package net.sf.jclal.util.learningcurve;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jclal.evaluation.measure.AbstractEvaluation;
import net.sf.jclal.util.dataset.LoadDataFromReporterFile;
import net.sf.jclal.util.file.FileUtil;
import net.sf.jclal.util.sort.Container;
import weka.core.Utils;

/**
 * Utility class to handle the learning curve produced by an AL process.
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 */
public class LearningCurveUtility {

	/**
	 * @param args the command line arguments
	 * @throws IOException The exception to launch
	 *
	 */
	//Case 1: -csv If is selected then -report, -measure and
	//[optional]-destination will be defined
	public static void main(String[] args) throws IOException {

		if (args == null || args.length == 0) {
			System.out
					.println("\nCase 1: -csv If is selected then -report=[String], "
							+ "-measure=[String] and\n"
							+ "[optional]-destination=[String] will be defined");
		}

		for (String method : args) {

			if (method.equalsIgnoreCase("-csv")) {
				String reportDirectory = "";
				String measure = "";
				String destination = null;

				for (String param : args) {
					if (param.startsWith("-report=")) {
						reportDirectory = extractValue(param);
					} else if (param.startsWith("-measure=")) {
						measure = extractValue(param);
					} else if (param.startsWith("-destination=")) {
						destination = extractValue(param);
					}
				}

				System.out.println("Starts CSV file reports process...");
				File r = csvFileReports(reportDirectory, measure, destination);
				System.out
						.println("End CSV. FilePath-> " + r.getAbsolutePath());
			}

		}
	}

	private static String extractValue(String param) {
		return param.substring(param.indexOf("=") + 1);
	}

	/**
	 * Calculates the area under the learning curve (ALC).
	 *
	 * @param tcurve
	 *            a list of evaluations
	 * @param measureName
	 *            The measure to use.
	 * @return the area under learning curve
	 */
	public static double getArea(List<AbstractEvaluation> tcurve,
			String measureName) {

		final int n = tcurve.size();

		if (n == 0) {
			return Double.NaN;
		}

		// The x-axis represents the number of labeled instances
		final int[] xVals = new int[tcurve.size()];

		// The y-axis represents the values of the metric name
		final double[] yVals = new double[tcurve.size()];

		// fill the xvals and yvals
		for (int i = 0; i < xVals.length; i++) {

			AbstractEvaluation eval = tcurve.get(i);

			xVals[i] = eval.getLabeledSetSize();
			yVals[i] = eval.getMetricValue(measureName);

		}

		double area = 0;
		double xlast = xVals[n - 1];

		double total = 0;

		for (int i = n - 2; i >= 0; i--) {
			double xDelta = Math.abs(xVals[i] - xlast);
			total += xDelta;
			area += (yVals[i] * xDelta);

			xlast = xVals[i];
		}

		if (area == 0) {
			return Utils.missingValue();
		}

		return area / total;
	}

	/**
	 * Calculates the area under the learning curve (ALC).
	 *
	 * @param reportFileCurve
	 *            The active learning reports
	 * @param measureName
	 *            The measure to use.
	 * @return the area under learning curve
	 */
	public static double getArea(File reportFileCurve, String measureName) {
		LoadDataFromReporterFile fileInput = new LoadDataFromReporterFile(
				reportFileCurve);

		return getArea(fileInput.getEvaluations(), measureName);
	}

	/**
	 * To construct a file in CSV format from the obtained experimental results
	 * over several datasets. It needs a directory, in the directory the folders
	 * containing the results must be organized by each dataset. If the destination is
	 * not specified then a file CSV is created automatically with the name of
	 * the directory of reports in its same origin path.
	 *
	 * @param reportDirectory The directory which contains the results
	 * @param measure The measure to take into account
	 * @param destinationName If null, then a file CSV is created automatically with the
	 *            name of the directory of reports in its same origin path.
	 * @return The csv file
	 * @throws IOException The exception to launch
	 */
	public static File csvFileReports(String reportDirectory,
			final String measure, String destinationName) throws IOException {

		File source = new File(reportDirectory);

		File exit;
		if (destinationName == null || destinationName.isEmpty()) {

			String name = source.getName();
			exit = new File(source.getParentFile(), name + ".csv");

			int c = 1;
			while (exit.exists()) {
				exit = new File(source.getParentFile(), name + c++ + ".csv");
			}

			name = null;
		} else {
			exit = new File(destinationName);
		}

		exit.createNewFile();

		File[] files = source.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});

		List<File> listDataset = Arrays.asList(files);
		FileUtil.orderFilesByPathName(listDataset, true);

		HashMap<String, Integer> columns = new HashMap<String, Integer>();

		int pos = 0;
		List<File>[] arrListReports = new List[listDataset.size()];
		// each dataset
		for (File dataset : listDataset) {

			List<File> listReports = Arrays.asList(dataset
					.listFiles(new FileFilter() {

						@Override
						public boolean accept(File pathname) {
							return pathname.isFile()
									&& isReport(pathname, measure);
						}
					}));
			FileUtil.orderFilesByPathName(listReports, true);

			// each report
			for (File report : listReports) {

				String nameReport = nameReport(report);
				if (!columns.containsKey(nameReport)) {
					columns.put(nameReport, columns.size());
				}
				// clean
				nameReport = null;

			}

			arrListReports[pos++] = listReports;
		}

		double[][] values = new double[listDataset.size()][columns.size()];
		// each dataset
		for (int i = 0; i < listDataset.size(); i++) {

			// each report
			for (int j = 0; j < arrListReports[i].size(); j++) {
				File report = arrListReports[i].get(j);

				String nameReport = nameReport(report);

				int column = columns.get(nameReport);
				values[i][column] = LearningCurveUtility.getArea(report,
						measure);

				// clean
				nameReport = null;
			}

		}

		Container<String>[] colNames = new Container[columns.size()];
		pos = 0;
		for (Map.Entry<String, Integer> entry : columns.entrySet()) {
			colNames[pos++] = new Container<String>(entry.getValue(),
					entry.getKey());
		}
		Arrays.sort(colNames);

		BufferedWriter writer = Files.newBufferedWriter(exit.toPath(),
				Charset.defaultCharset());
		writer.append("Datasets");
		for (int i = 0; i < colNames.length; i++) {
			writer.append(";").append(colNames[i].getValue());
		}
		writer.append("\n");

		String v;
		for (int i = 0; i < listDataset.size(); i++) {
			File dataset = listDataset.get(i);

			writer.append(dataset.getName());
			for (int j = 0; j < values[0].length; j++) {
				v = String.valueOf(values[i][j]).replace(',', '.');
				writer.append(";").append(v);
			}
			writer.append("\n");
		}
		writer.flush();
		writer.close();

		// clean
		files = null;
		listDataset = null;
		columns.clear();
		columns = null;
		for (int i = 0; i < colNames.length; i++) {
			colNames[i] = null;
		}
		colNames = null;
		for (int i = 0; i < arrListReports.length; i++) {
			arrListReports[i] = null;
		}
		arrListReports = null;
		v = null;
		writer = null;

		return exit;
	}

	public static String nameReport(File report) {
		String nameReport = report.getName().substring(
				report.getName().indexOf('-') + 1);
		return nameReport.split(" ")[0];
	}

	public static boolean isReport(File x, String measure) {
		try {
			LearningCurveUtility.getArea(x, measure);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}