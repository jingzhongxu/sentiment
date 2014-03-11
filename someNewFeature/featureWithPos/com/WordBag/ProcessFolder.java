import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;

import edu.fudan.ml.types.Dictionary;
import edu.fudan.nlp.cn.tag.CWSTagger;
import edu.fudan.nlp.cn.tag.POSTagger;

public class ProcessFolder implements Serializable
{
	public File posFolder;
	public File negFolder;
	public ArrayList<String> posDocuments = new ArrayList<String>();
	public ArrayList<String> negDocuments = new ArrayList<String>();
	public ArrayList<String> posDocumentsPOS = new ArrayList<String>();
	public ArrayList<String> negDocumentsPOS = new ArrayList<String>();
	public transient POSTagger tag;
	public HashSet<String> wordWithPOSDict = new HashSet<String>();


	public ProcessFolder(String posFolderName,String negFolderName) throws Exception
	{
		this.posFolder = new File(posFolderName);
		this.negFolder = new File(negFolderName);
		if(!(posFolder.exists() && negFolder.exists()))
		{
			throw new Exception("folder or file in not exist!");
		}

		CWSTagger cws = new CWSTagger("./models/seg.m");
		tag = new POSTagger(cws,"models/pos.m");
		tag.SetTagType("en");
	}

    public void readFileInFolder() throws Exception
    {
    	File[] files = posFolder.listFiles();
    	for(File file:files)
    	{
    		int length = (int)file.length();
    		InputStream is = new FileInputStream(file);
    		byte[] buffer = new byte[length];
    		is.read(buffer);
    		String row = new String(buffer,"gbk");
    		String[] arrays = row.trim().split("\n");
    		posDocuments.add(arrays[1]);
    		is.close();
    	}

    	files = negFolder.listFiles();
    	for(File file:files)
    	{
    		int length = (int)file.length();
    		InputStream is = new FileInputStream(file);
    		byte[] buffer = new byte[length];
    		is.read(buffer);
    		String row = new String(buffer,"gbk");
    		String[] arrays = row.trim().split("\n");
    		negDocuments.add(arrays[1]);
    		is.close();
    	}
    }

    public void segmentAndPostag() throws Exception
    {
    	if(posDocuments.size()==0)
    	{
    		this.readFileInFolder();
    	}

    	Iterator<String> iter = posDocuments.iterator();
    	for(;iter.hasNext();)
    	{
    		String temp = tag.tag(iter.next());
    		posDocumentsPOS.add(temp);
			String[] arrays = temp.split(" ");
			for(String ele:arrays)
			{
				wordWithPOSDict.add(ele);
			}
    	}
    	iter = negDocuments.iterator();
    	for(;iter.hasNext();)
    	{
    		String temp = tag.tag(iter.next());
    		negDocumentsPOS.add(temp);
			String[] arrays = temp.split(" ");
			for(String ele:arrays)
			{
				wordWithPOSDict.add(ele);
			}
    	}
    }

    public static void main(String[] args) throws Exception
    {
    	ProcessFolder obj = new ProcessFolder("../corpus/train/pos","../corpus/train/neg");
    	obj.readFileInFolder();
    	// obj.outputArrayList(obj.posDocuments);
    	// obj.outputArrayList(obj.negDocuments);
    	// System.out.println(obj.posDocuments.size());
    	// System.out.println(obj.negDocuments.size());

    	obj.segmentAndPostag();
    	obj.outputArrayList(obj.posDocumentsPOS);
    	obj.outputArrayList(obj.negDocumentsPOS);
    	System.out.println(obj.posDocumentsPOS.size());
    	System.out.println(obj.negDocumentsPOS.size());

    	obj.outputArrayList(new ArrayList<String>(obj.wordWithPOSDict));
    

    	/*********************  Serializable Object *****************/
    	ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("./processFolder.ser")));
    	oos.writeObject(obj);
    	oos.close();

    	// ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("./processFolder.ser")));
    	// ProcessFolder obj = (ProcessFolder)ois.readObject();
    	// ois.close();
    	// obj.outputArrayList(obj.posDocumentsPOS);
    	// obj.outputArrayList(obj.negDocumentsPOS);
    	// System.out.println(obj.posDocumentsPOS.size());
    	// System.out.println(obj.negDocumentsPOS.size());
    }

    public void outputArrayList(ArrayList<String> list)
    {
    	Iterator<String> iter = list.iterator();
    	for(;iter.hasNext();)
    	{
    		System.out.println(iter.next());
    	}
    }
}