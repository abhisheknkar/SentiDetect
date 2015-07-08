import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;


public class GeneralOperations 
{
	public static <E> Double getMeanOfList(List<E> Obj, Integer infthresh, Integer infToConsider, Integer infval)
	{
		Double mean=0.0;
		Integer count = 0;
		
		for(E e : Obj)
		{
			if ((Double) e < infthresh)
			{
				++count;
				mean += (Double) e;
			}
			else
			{
				if(infToConsider==1)
				{
					++count;
					mean += (double) infval;
				}
			}
		}
		if (count > 0) mean /= count;
		return mean;
	}
	public static <E> Double getMeanOfList(List<E> Obj)
	{
		Double mean = getMeanOfList(Obj, Integer.MAX_VALUE, 0, 0);
		return mean;
	}

	public static <E> Double getMedianOfList(List<E> Obj, double defaultvalue)
	{
		Double median = 0.0;
		Object[] ObjSorted = Obj.toArray();
		Arrays.sort(ObjSorted);	
		
		if((ObjSorted.length)%2 == 1) median = (double) ObjSorted[(ObjSorted.length - 1)/2];
		else median = 0.5 * ( (double) (ObjSorted[ObjSorted.length/2])  + (double) ObjSorted[ObjSorted.length/2-1]);

		return Math.min(median, defaultvalue);
	}

	public static <E> Double getMedianOfList(List<E> Obj)
	{
		Double median = getMedianOfList(Obj, Double.MAX_VALUE);
		return median;
	}	

	public static <E> Double getSumOfList(List<E> Obj, Integer infthresh, Integer infToConsider, Integer infval)
	{
		Double sum=0.0;
		Integer count = 0;
		for(E e : Obj)
		{
			if ((Double) e < infthresh)
			{
				++count;
				sum += (Double) e;
			}
			else
			{
				if(infToConsider==1)
				{
					++count;
					sum += (double) infval;
				}
			}
		}
		return sum;
	}

	public static int getLevenshteinDistance(String a, String b) 
	{
        a = a.toLowerCase();
        b = b.toLowerCase();
        // i == 0
        int [] costs = new int [b.length() + 1];

        for (int j = 0; j < costs.length; j++)
        {
        	costs[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) 
        {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) 
            {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

	public static SparseGraph<String, Integer> readGraph(File fin, String separator) throws IOException
	{
		//Learn usage of transformers!
		String line;
		int count = 0;
		String[] linesplit;
		BufferedReader in = new BufferedReader(new FileReader(fin));
		SparseGraph<String, Integer> G = new SparseGraph<String, Integer>();
		while((line=in.readLine()) != null)
		{
			linesplit = line.split(separator);
			G.addEdge(count++, linesplit[0], linesplit[1]);			
		}
		return G;
	}
	
	public static <E> int getBFSDistance(SparseGraph<E, Integer> G, E source, E dest, ArrayList<E> visitednodes, int distance)
	{
		int BFSDistance;
		visitednodes.add(source);
		for(E child : G.getNeighbors(source))
		{
			if(visitednodes.contains(child)) continue;
			if(child.equals(dest)) return 1;
		}
		
		for(E child : G.getNeighbors(source))
		{
			if(visitednodes.contains(child))continue;
			visitednodes.add(child);
			BFSDistance = getBFSDistance(G, child, dest, visitednodes, distance);
			if(BFSDistance != Integer.MAX_VALUE) distance = Math.min(BFSDistance + 1, distance);
		}
		return distance;
	}

}
