import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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


}
