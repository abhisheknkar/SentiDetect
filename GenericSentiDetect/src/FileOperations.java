import java.io.*;
import java.util.ArrayList;

public class FileOperations {
	public static <E> void writeObject(E Obj, File fout) throws IOException
	{
		FileOutputStream foutStream = new FileOutputStream(fout);
		ObjectOutputStream oos = new ObjectOutputStream(foutStream);
		oos.writeObject(Obj);		
	}

	
	public static <E> E readObject(File fin) throws IOException
	{
		FileInputStream finstream = null;
		ObjectInputStream objinstream = null;
		E object = null;
		try
		{
			finstream = new FileInputStream(fin);
			objinstream = new ObjectInputStream(finstream);
			object = (E) objinstream.readObject(); 
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			if (finstream != null)
			{
				finstream.close();
				objinstream.close();
			}
		}
		return object;
	}
	
	
}
