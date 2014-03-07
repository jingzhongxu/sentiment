import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class TestRead
{
	public static void main(String[] args) throws Exception
	{
		File file = new File("../corpus/all/pos/4490.txt");		
		int length = (int)file.length();
		byte[] buffer = new byte[length];

		InputStream is = new FileInputStream(file);
		is.read(buffer);

		is.close();
		String tt = new String(buffer,"gbk");
		System.out.println(tt);
		System.out.println("----------------------");
		System.out.println(tt.trim());
		System.out.println("----------------------");
		String gg=tt.trim();

		System.out.println("----------------------");
		String[] arrays = gg.split("\n");
		for(String temp:arrays)
		{
			System.out.println(temp);
		}
		System.out.println("----------------------");
		System.out.println(arrays[1]);
	}
}