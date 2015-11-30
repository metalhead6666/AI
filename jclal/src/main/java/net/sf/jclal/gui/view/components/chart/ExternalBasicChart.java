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
package net.sf.jclal.gui.view.components.chart;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import net.sf.jclal.core.IEvaluation;
import net.sf.jclal.evaluation.measure.AbstractEvaluation;
import net.sf.jclal.util.dataset.LoadDataFromReporterFile;
import net.sf.jclal.util.learningcurve.LearningCurveUtility;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;

/**
 * Visual Component to visualize the learning curve from an active
 * learning experiment
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 */
public class ExternalBasicChart extends JFrame {

    private static final long serialVersionUID = 7079334419440144035L;

    private XYSeriesCollection series;
    private ChartPanel chartPanel;
    private JFreeChart chart;
    private JPanel content;
    private JComboBox<String> comboBox;
    private String windowsTitle;
    private List<List<AbstractEvaluation>> evaluationsCollection;
    private String chartTitle;
    private String xTitle;
    private ArrayList<String> queryNames;
    private JMenuBar menubar;
    private JSlider slider;
    private int reportFrecuency;
    private int max;
    //*************************
    private LearningCurvesVisualTable curveOptions;
    private HashMap<String, Color> controlCurveColor;
    private Set<Integer> set;
    private Set<String> setSeries;
    private Object[][] data;
    private ArrayList<Color> colors;
    //****************************

    private boolean viewPointsForm = false;
    private boolean viewWithOutColor = false;
    private boolean viewWhiteBackground = false;

    // It stores the evaluation of passive learning. It is represented as
    // another curve in the chart
    private IEvaluation passiveEvaluation;

    /**
     *
     * @param windowsTitleParam The title of the window.
     * @param chartTitleParam The title of the chart panel.
     * @param xTitleParam The X-axis label.
     */
    public ExternalBasicChart(String windowsTitleParam, String chartTitleParam,
            String xTitleParam) {

        reportFrecuency = 1;

        menubar = new JMenuBar();
        menubar.add(createMenu());

        max = -1;

        windowsTitle = windowsTitleParam;
        chartTitle = chartTitleParam;
        xTitle = xTitleParam;

        queryNames = new ArrayList<String>();
        evaluationsCollection = new ArrayList<List<AbstractEvaluation>>();
        controlCurveColor = new HashMap<String, Color>();
        set = new HashSet<Integer>();
        setSeries = new HashSet<String>();
        colors = new ArrayList<Color>();

        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(640, 480));
        chartPanel.setFillZoomRectangle(true);
        chartPanel.setMouseWheelEnabled(true);

        slider = new JSlider(JSlider.HORIZONTAL);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setToolTipText("Changes the report frecuency");
        slider.setSnapToTicks(true);
        slider.setMinimum(1);
        slider.setMinorTickSpacing(1);

        slider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {

                reportFrecuency = ((JSlider) evt.getSource()).getValue();

                slider.setToolTipText(String.valueOf(reportFrecuency));

                jComboBoxItemStateChanged();
            }
        });

        comboBox = new JComboBox<String>();

        comboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent evt) {

                jComboBoxItemStateChanged();
            }
        });

        content = new JPanel(new BorderLayout());
        content.add(comboBox, BorderLayout.NORTH);
        content.add(chartPanel, BorderLayout.CENTER);
        content.add(slider, BorderLayout.SOUTH);

        setJMenuBar(menubar);
        setTitle(windowsTitle);
        setContentPane(this.content);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }

    protected void jComboBoxItemStateChanged() {

        try {

            XYDataset dataset;
            String yTitle;

            if (comboBox.getItemCount() != 0) {

                dataset = createDataset(comboBox.getSelectedItem()
                        .toString());
                yTitle = comboBox
                        .getSelectedItem().toString();
            } else {
                dataset = null;
                yTitle = new String();
            }

            createChart(dataset, chartTitle, xTitle, yTitle);

            chartPanel.removeAll();

            chartPanel.setChart(chart);

            chartPanel.repaint();

        } catch (Exception e) {
        }
    }

    private JFreeChart createChart(XYDataset dataset, String title,
            String xTitle, String yTitle) {

        colors.add(Color.BLACK);
        colors.add(1, Color.BLUE);
        colors.add(1, Color.RED);
        colors.add(1, Color.GREEN);
        colors.add(1, Color.YELLOW);
        colors.add(1, Color.CYAN);
        colors.add(1, Color.MAGENTA);
        colors.add(1, new Color(111, 83, 64));
        colors.add(1, new Color(153, 51, 255));
        colors.add(1, new Color(102, 204, 255));
        colors.add(1, new Color(85, 80, 126));
        colors.add(1, new Color(168, 80, 126));

        chart = ChartFactory.createXYLineChart(title, xTitle, yTitle, dataset,
                PlotOrientation.VERTICAL, true, true, false);

        chart.setBackgroundPaint(Color.white);

        chart.getXYPlot().setBackgroundPaint(Color.white);
        chart.getXYPlot().setDomainGridlinePaint(Color.white);
        chart.getXYPlot().setRangeGridlinePaint(Color.white);

        int numSeries = series.getSeriesCount();

        XYLineAndShapeRenderer renderer = ((XYLineAndShapeRenderer) chart.
                getXYPlot().getRenderer());

        renderer.setDrawSeriesLineAsPath(true);

        renderer.setSeriesStroke(0, new BasicStroke(2.0F));

        renderer.setSeriesStroke(
                1, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f, new float[]{2}, 0)
        );

        renderer.setSeriesStroke(
                2, new BasicStroke(
                        2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                        1.0f, new float[]{6.0f, 2.0f, 6.0f, 2.0f}, 0.0f
                )
        );

        renderer.setSeriesStroke(
                3, new BasicStroke(
                        2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                        1.0f, new float[]{12.0f, 2.0f, 2.0f, 2.0f}, 0.0f
                )
        );

        renderer.setSeriesStroke(
                4, new BasicStroke(
                        2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                        1.0f, new float[]{12.0f, 2.0f, 2.0f, 2.0f, 2.0f, 2.0f},
                        0.0f
                )
        );

        renderer.setSeriesStroke(
                5, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f, new float[]{12, 2, 12, 2, 2, 2, 2,
                    2, 2, 2, 2, 2}, 0)
        );

        renderer.setSeriesStroke(
                6, new BasicStroke(
                        2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                        1.0f, new float[]{6.0f, 2.0f, 6.0f, 2.0f, 2.0f, 2.0f},
                        0.0f
                )
        );

        renderer.setSeriesStroke(
                7, new BasicStroke(
                        2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                        1.0f, new float[]{6.0f, 2.0f, 6.0f, 2.0f, 6.0f, 2.0f,
                            2.0f, 2.0f, 2.0f, 2.0f, 2.0f, 2.0f}, 0.0f
                )
        );

        for (int i = 0; i < numSeries; i++) {

            if (i == colors.size()) {

                colors = addColors(colors, numSeries);

            }

            if (viewWithOutColor) {
                renderer.setSeriesPaint(i, Color.BLACK);
            } else {
                String name = series.getSeries(i).getKey().toString();

                if (!controlCurveColor.containsKey(name)) {
                    renderer.setSeriesPaint(i, colors.get(i));
                    controlCurveColor.put(name, colors.get(i));

                } else {

                    renderer.setSeriesPaint(i, controlCurveColor.get(name));

                }

                renderer.setSeriesShapesVisible(i, viewPointsForm);

                if (viewWhiteBackground) {
                    chart.getXYPlot().setBackgroundPaint(Color.WHITE);
                }
            }
        }
        chart.getXYPlot().setRenderer(renderer);

        return chart;
    }

    /**
     * @param one
     * @param two
     * @param three
     * @return
     * <p>
     * return new color RBG
     */
    private Color colorGenerator(Color one, Color two, Color three) {
        int red = (int) (one.getRed() + one.getBlue() + one.getGreen() / 3);
        int blue = (int) (two.getBlue() + two.getRed() + two.getGreen() / 3);
        int green = (int) (three.getGreen() + three.getRed() + three.getBlue() / 3);

        return new Color(red, blue, green);
    }

    /**
     *
     * @param colors
     * @param length
     * @return Add colors to the ArryList
     */
    private ArrayList<Color> addColors(ArrayList<Color> colors, int length) {

        ArrayList<Color> newColors = colors;

        for (int i = 0; i < length; i++) {

            newColors.add(colorGenerator(
                    newColors.get((int) (Math.random() * newColors.size() - 1)),
                    newColors.get((int) (Math.random() * newColors.size() - 1)),
                    newColors.get((int) (Math.random() * newColors.size() - 1)))
            );

        }

        return newColors;
    }

    private XYDataset createDataset(String metricName) {

        series = new XYSeriesCollection();

        //For each collection of evaluations
        for (int i = 0; i < evaluationsCollection.size(); i++) {

            if (!set.contains(i)) {

                List<AbstractEvaluation> evaluations = evaluationsCollection.
                        get(i);
                XYSeries newXYSerie = new XYSeries(queryNames.get(i));

                int evalIndex = 0;

                for (AbstractEvaluation eval : evaluations) {
                    if (evalIndex % reportFrecuency == 0) {
                        newXYSerie.add(eval.getLabeledSetSize(),
                                eval.getMetricValue(metricName));
                    }
                    ++evalIndex;
                }
                series.addSeries(newXYSerie);
            }

        }
        //The series that belongs to passive learning is the last
        if (passiveEvaluation != null) {

            series.addSeries(createPassiveLearningSerie(metricName,
                    evaluationsCollection.get(0)));
        }
        return series;
    }

    public XYSeries createPassiveLearningSerie(String metricName,
            List<AbstractEvaluation> evaluations) {

        XYSeries xyseriesPassive = new XYSeries("Passive learning");

        // Fill the series of passive learning
        int evalIndex = 0;
        for (AbstractEvaluation eval : evaluations) {

            if (evalIndex % reportFrecuency == 0) {
                xyseriesPassive.add(eval.getLabeledSetSize(),
                        passiveEvaluation.getMetricValue(metricName));
            }
            ++evalIndex;
        }

        return xyseriesPassive;
    }

    /**
     *
     * @param evaluation The evaluation to add
     */
    public void add(AbstractEvaluation evaluation) {

        evaluationsCollection.get(evaluationsCollection.size() - 1).add(
                evaluation);

        //fire the itemStateChanged method
        jComboBoxItemStateChanged();

    }

    public void addSerie(ArrayList<AbstractEvaluation> evaluations,
            String queryName) {

        if (!setSeries.contains(queryName)) {
            setSeries.add(queryName);
            evaluationsCollection.add(evaluations);
            queryNames.add(queryName);
            curveOptions = null;

        }
    }

    /**
     *
     * @return Update the information of table.
     */
    private Object[][] informationTable() {
        
    	int length = queryNames.size();
        Object[][] row = new Object[length][3];

        for (int i = 0; i < queryNames.size(); i++) {
            if (set.contains(i)) {
                row[i][0] = new Boolean(null);
            } else {
                row[i][0] = new Boolean("true");
            }
            row[i][1] = queryNames.get(i);
            row[i][2] = controlCurveColor.get(queryNames.get(i));

        }
        jComboBoxItemStateChanged();
        return row;
    }

    /**
     *
     * @param items The items for the combobox component
     */
    public void setMeasuresNames(String[] items) {

        for (String item : items) {
            comboBox.addItem(item);
        }

    }

    /**
     * Enable the visual components
     */
    public void enabledMetrics() {
        comboBox.setEnabled(true);
        menubar.setEnabled(true);
        menubar.getMenu(0).setEnabled(true);

        slider.setValue(1);
        slider.setMaximum((evaluationsCollection.get(0).size() > 1)
                ? (evaluationsCollection.get(0).size() - 1) : 1);
        slider.setEnabled(true);
    }

    /**
     *
     * @param args NOT IN USE.
     */
    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                ExternalBasicChart demo = new ExternalBasicChart("Active learning "
                        + "process", "", "Number of labeled instances");
                demo.pack();
                RefineryUtilities.centerFrameOnScreen(demo);
                demo.setVisible(true);
            }
        });

    }

    /**
     *
     * @return Create the menu that allows load the result from others
     * experiments.
     */
    private JMenu createMenu() {

        final JMenu fileMenu = new JMenu("Options");

        final JFileChooser f = new JFileChooser();
        f.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        fileMenu.add("Add report file or directory").addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (curveOptions != null) {
                    curveOptions.setVisible(false);
                    curveOptions = null;
                }
                int action = f.showOpenDialog(fileMenu);

                if (action == JFileChooser.APPROVE_OPTION) {

                    loadReportFile(f.getSelectedFile());

                }

            }
        });

        fileMenu.add("Area under learning curve (ALC)").addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                if (comboBox.getItemCount() != 0) {

                    if (!comboBox.getSelectedItem().toString().isEmpty()) {

                        StringBuilder st = new StringBuilder();

                        st.append("Measure: ").append(comboBox.getSelectedItem()).append("\n\n");

                        for (int query = 0; query < queryNames.size(); query++) {

                            double value = LearningCurveUtility.getArea(evaluationsCollection.get(query), comboBox.getSelectedItem().toString());

                            String valueString = String.format("%.3f", value);

                            st.append(queryNames.get(query)).append(": ").append(valueString).append("\n");

                        }

                        JOptionPane.showMessageDialog(content, st.toString(), "Area under the learning curve", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        }
        );

        fileMenu.add("Clear").addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                queryNames = new ArrayList<String>();
                evaluationsCollection = new ArrayList<List<AbstractEvaluation>>();
                comboBox.removeAllItems();
                controlCurveColor = new HashMap<String, Color>();
                colors.clear();
                data = null;
                set.clear();
                 curveOptions.setVisible(false);
                curveOptions = null;
                setSeries.clear();
                passiveEvaluation = null;

            }
        }
        );
        
        
        fileMenu.add("Curve options").addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent arg0) {

                data = informationTable();
                
                if (queryNames.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please add curves");
                    return;
                }
                if (curveOptions == null) {
                      curveOptions = new LearningCurvesVisualTable(ExternalBasicChart.this);
                }else{
                 curveOptions.setVisible(true);
                }
              

            }

        }
        );

        final JCheckBox viewPoints = new JCheckBox("View points's shapes");
        viewPoints.setSelected(false);

        viewPoints.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                viewPointsForm = viewPoints.isSelected();
                jComboBoxItemStateChanged();
            }
        });

        fileMenu.add(viewPoints);

        final JCheckBox withOutColor = new JCheckBox("View without color");
        withOutColor.setSelected(false);

        withOutColor.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                viewWithOutColor = withOutColor.isSelected();
                jComboBoxItemStateChanged();
            }
        });

        fileMenu.add(withOutColor);

        return fileMenu;
    }

    private void loadReportFile(File x) {

        if (x.isDirectory()) {
            File[] temp = x.listFiles();
            for (File file : temp) {
                loadReportFile(file);
            }

        } else if (x.isFile()) {
            try {
                LoadDataFromReporterFile fileInput = new LoadDataFromReporterFile(x);

                if (comboBox.getItemCount() == 0) {
                    {
                        setMeasuresNames(fileInput.getEvaluations().get(0)
                                .getMetricNames());
                    }
                } else if (!compareMetrics(fileInput.getEvaluations().get(0)
                        .getMetricNames())) {
                    JOptionPane
                            .showMessageDialog(
                                    chartPanel,
                                    "The report file that you are trying to load does not belong to the same category");
                    return;
                }

                addSerie(fileInput.getEvaluations(), fileInput.getProperties().getProperty(
                        "Query strategy"));

                //fire the jComboBoxStateChanged()
                slider.setValue(1);
                jComboBoxItemStateChanged();

                if (fileInput.getEvaluations().size() > max) {
                    max = fileInput.getEvaluations().size() - 1;
                    slider.setMaximum(max);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "A error has happened on "
                        + "having loaded the file: " + x.getName(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     *
     * @param metricNames Metric names.
     * @return
     * <p>
     * true: if the combobox's values are equals to the metrics's names</p>
     * <p>
     * false: otherwise</p>
     */
    public boolean compareMetrics(String[] metricNames) {

        for (int i = 0; i < metricNames.length; ++i) {

            if (!comboBox.getItemAt(i).equals(metricNames[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     *
     * @return The evaluation of supervised learning.
     */
    public IEvaluation getPassiveEvaluation() {
        return passiveEvaluation;
    }

    /**
     *
     * @param passiveEvaluation The evaluation of supervised learning.
     */
    public void setPassiveEvaluation(IEvaluation passiveEvaluation) {
        this.passiveEvaluation = passiveEvaluation;
    }

    /**
     * Disable the visual components in order to they are not functional in the
     * AL process
     */
    public void setDisabledComponents() {

        comboBox.setEnabled(false);
        menubar.setEnabled(false);
        slider.setEnabled(false);
        menubar.getMenu(0).setEnabled(false);
    }

	public Set<Integer> getSet() {
		return set;
	}

	public void setSet(Set<Integer> set) {
		this.set = set;
	}

	public Object[][] getData() {
		return data;
	}

	public void setData(Object[][] data) {
		this.data = data;
	}

	public ArrayList<String> getQueryNames() {
		return queryNames;
	}

	public void setQueryNames(ArrayList<String> queryNames) {
		this.queryNames = queryNames;
	}

	public HashMap<String, Color> getControlCurveColor() {
		return controlCurveColor;
	}

	public void setControlCurveColor(HashMap<String, Color> controlCurveColor) {
		this.controlCurveColor = controlCurveColor;
	}
    
}
