import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeneralOperations 
{
	public static <E> Double getMeanOfList(List<E> Obj, Integer infthresh)
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
		}
		if (count > 0) mean /= count;
		return mean;
	}
	public static <E> Double getMeanOfList(List<E> Obj)
	{
		Double mean = getMeanOfList(Obj, Integer.MAX_VALUE);
		return mean;
	}

	public static <E> Double getMedianOfList(List<E> Obj)
	{
		Double median = 0.0;
		Object[] ObjSorted = Obj.toArray();
		Arrays.sort(ObjSorted);	
		
		if((ObjSorted.length)%2 == 1) median = (double) ObjSorted[(ObjSorted.length - 1)/2];
		else median = 0.5 * ( (double) (ObjSorted[ObjSorted.length/2])  + (double) ObjSorted[ObjSorted.length/2-1]);
		return median;
	}
}
