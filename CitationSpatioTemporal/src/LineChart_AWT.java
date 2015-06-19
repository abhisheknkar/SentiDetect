import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

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
import org.jfree.data.category.DefaultCategoryDataset;

public class LineChart_AWT extends ApplicationFrame
{
   public LineChart_AWT( String applicationTitle , String chartTitle, String Xtitle, String Ytitle, TreeMap<Integer, Double> T) throws IOException
   {
      super(applicationTitle);
      JFreeChart lineChart = ChartFactory.createLineChart(chartTitle,Xtitle,Ytitle,createDataset(T),
    		  PlotOrientation.VERTICAL,
    		  true,true,false);

      ChartPanel chartPanel = new ChartPanel( lineChart );
      chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
      setContentPane( chartPanel );

      
      CategoryAxis domainAxis = lineChart.getCategoryPlot().getDomainAxis();
      ValueAxis rangeAxis = lineChart.getCategoryPlot().getRangeAxis();
      rangeAxis.setRange(0.0, 10);
      
      int width = 640; /* Width of the image */
      int height = 480; /* Height of the image */ 
      File LineChart = new File( "Outputs\\" + chartTitle +".jpeg" ); 
      ChartUtilities.saveChartAsJPEG( LineChart , lineChart , width , height );
   	}

   private DefaultCategoryDataset createDataset(TreeMap<Integer, Double> T )
   {
	   DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
	   for (Map.Entry<Integer, Double> t : T.entrySet())
	   {
		   dataset.addValue(t.getValue(), "values", t.getKey());
	   }
      return dataset;
   }

   public static void Plot(TreeMap<Integer, Double> T, String applicationTitle, String chartTitle, String Xtitle, String Ytitle) throws IOException
   {
      LineChart_AWT chart = new LineChart_AWT(applicationTitle, chartTitle, Xtitle, Ytitle, T);
      chart.pack( );
      RefineryUtilities.centerFrameOnScreen( chart );
      chart.setVisible( true );
   }
}