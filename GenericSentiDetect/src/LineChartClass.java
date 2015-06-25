import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class LineChartClass extends ApplicationFrame
{
	private static ChartPanel chartPanel;
	private static JFreeChart chart;
	private static XYSeriesCollection dataset = new XYSeriesCollection();
	private static String XLabel, YLabel, Title;
	
	public LineChartClass(String title, String xAxisLabel, String yAxisLabel) throws IOException
	{
		super(title);
		XLabel = xAxisLabel; 
		YLabel = yAxisLabel;
		Title = title;
	}

	public void makePlot()
	{	
		chart = createChart(Title,XLabel,YLabel);
		chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize( new java.awt.Dimension( 800 , 600 ) );
		setContentPane( chartPanel );
	}

	private XYDataset createDataset(TreeMap<Integer, Double> T)
	{
		String SeriesName = "First";
		XYSeries series1 = new XYSeries(SeriesName);
		for (Map.Entry<Integer, Double> t : T.entrySet())
		{
//		   System.out.println(t.getKey() + "," + t.getValue());
			series1.add( t.getKey(), t.getValue());
		}
		dataset.addSeries(series1);
		return dataset;
	}
   
	public static void addToDataset(TreeMap<Integer, Double> T, String SeriesName)
	{
		XYSeries series1 = new XYSeries(SeriesName);
		if(T.size() > 0)
		{
			for (Map.Entry<Integer, Double> t : T.entrySet())
			{
				series1.add( t.getKey(), t.getValue());
			}
			dataset.addSeries(series1);

		}
	}
	
	public static void clearDataset()
	{
		dataset.removeAllSeries();
	}
	
	private static JFreeChart createChart(String title, String xAxisLabel, String yAxisLabel)
	{
		JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset);
		XYPlot plot = chart.getXYPlot();
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		plot.setRenderer(renderer);
		
		return chart;
	}	
	public void plot() throws IOException
	{
		this.pack();
		RefineryUtilities.centerFrameOnScreen(this);
		this.setVisible(true);
	}	

	public void save(File fout) throws IOException
	{
		int width = 640; 
	    int height = 480; 
	    ChartUtilities.saveChartAsJPEG(fout ,chart, width, height);	
    }	
	public void setYRange(double min, double max)
	{
		ValueAxis rangeAxis = chart.getXYPlot().getRangeAxis();
		rangeAxis.setRange(min,max);
	}
}