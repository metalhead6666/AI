package net.sf.jclal.gui.view.components.chart;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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
 * Visual Component to visualize the learning curve resulting from an active
 * learning experiment
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 */
public class StaticBasicChart extends JFrame {

    private static final long serialVersionUID = 7079334419440144035L;

    private XYSeriesCollection series;
    private ChartPanel chartPanel;
    private JFreeChart chart;
    private JPanel content;
    private String windowsTitle;
    private String chartTitle;
    private String xTitle;
    private ArrayList<String> queryNames;

    public StaticBasicChart(String windowsTitleParam, String chartTitleParam,
            String xTitleParam) {

        windowsTitle = windowsTitleParam;
        chartTitle = chartTitleParam;
        xTitle = xTitleParam;

        queryNames = new ArrayList<String>();
        
        queryNames.add("ML-ES");
        queryNames.add("ML-RC");
        queryNames.add("US-ED");
        queryNames.add("CMN");
        queryNames.add("BinMin");
        queryNames.add("ML");
        queryNames.add("MML");
        queryNames.add("MMC");

        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(640, 480));
        chartPanel.setFillZoomRectangle(true);
        chartPanel.setMouseWheelEnabled(true);
        
        XYDataset dataset = createDataset();
        
        createChart(dataset, chartTitle, xTitleParam, "Measure");

        chartPanel.setChart(chart);

        chartPanel.repaint();

        content = new JPanel(new BorderLayout());
        content.add(chartPanel, BorderLayout.CENTER);

        setTitle(windowsTitle);
        setContentPane(this.content);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }
    
    private XYDataset createDataset() {

        series = new XYSeriesCollection();

        int index = 0;

        //For each collection of evaluations
        for (int serie=0;serie<8;serie++) {

            XYSeries newXYSerie = new XYSeries(queryNames.get(index++));

            for (int x=0;x<100;x++) {
                
            	newXYSerie.add(x,
                            90-serie*5);
            }

            series.addSeries(newXYSerie);
        }
        
        return series;

    }

    private JFreeChart createChart(XYDataset dataset, String title,
            String xTitle, String yTitle) {
    	
    	Color [] colors= new Color[]{Color.BLACK,Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW, Color.CYAN, Color.MAGENTA, new Color(111,83,64)};
    	
        chart = ChartFactory.createXYLineChart(title, xTitle, yTitle, dataset,
                PlotOrientation.VERTICAL, true, true, false);

        chart.setBackgroundPaint(Color.white);

        chart.getXYPlot().setBackgroundPaint(Color.white);
        chart.getXYPlot().setDomainGridlinePaint(Color.white);
        chart.getXYPlot().setRangeGridlinePaint(Color.white);
        
        int numSeries = series.getSeriesCount();

        XYLineAndShapeRenderer renderer = ((XYLineAndShapeRenderer) chart.getXYPlot().getRenderer());
        
        renderer.setDrawSeriesLineAsPath(true);
        
        //ok
        renderer.setSeriesStroke(0, new BasicStroke(2.0F));
        
        //ok
        renderer.setSeriesStroke(
                1, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f, new float[] {2}, 0)
            );
            
        //ok
        renderer.setSeriesStroke(
                2, new BasicStroke(
                    2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    1.0f, new float[] {6.0f,2.0f,6.0f,2.0f}, 0.0f
                )
            );
        
       //ok      
       renderer.setSeriesStroke(
                3, new BasicStroke(
                    2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    1.0f, new float[] {12.0f,2.0f,2.0f,2.0f}, 0.0f
                )
            );
       
       //ok      
       renderer.setSeriesStroke(
                4, new BasicStroke(
                    2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    1.0f, new float[] {12.0f,2.0f,2.0f,2.0f,2.0f,2.0f}, 0.0f
                )
            );
       
       //ok
        renderer.setSeriesStroke(
                5, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f, new float[] {12,2,12,2,2,2,2,2,2,2,2,2}, 0)
            );
        
      //ok
        renderer.setSeriesStroke(
                6, new BasicStroke(
                    2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    1.0f, new float[] {6.0f,2.0f,6.0f,2.0f,2.0f,2.0f}, 0.0f
                )
            );
        
      //ok
        renderer.setSeriesStroke(
                7, new BasicStroke(
                    2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    1.0f, new float[] {6.0f,2.0f,6.0f,2.0f,6.0f,2.0f,2.0f,2.0f,2.0f,2.0f,2.0f,2.0f}, 0.0f
                )
            );
        
        for (int i = 0; i < numSeries; i++) {
        	
        	if(i<colors.length)
        		renderer.setSeriesPaint(i,colors[i]);
        	
            renderer.setSeriesLinesVisible(i, true);

        }

        
        chart.getXYPlot().setRenderer(renderer);

        return chart;
    }

    /**
     *
     * @param args NOT IN USE.
     */
    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                StaticBasicChart demo = new StaticBasicChart("Active learning process",
                        "", "Number of labeled instances");
                demo.pack();
                RefineryUtilities.centerFrameOnScreen(demo);
                demo.setVisible(true);
            }
        });

    }
}
