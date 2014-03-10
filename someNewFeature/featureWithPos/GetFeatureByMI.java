import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import edu.fudan.ml.types.Dictionary;
import edu.fudan.nlp.cn.tag.CWSTagger;
import edu.fudan.nlp.cn.tag.POSTagger;

public class GetFeatureByMI
{
	public File posFolder;
	public File negFolder;
	public ArrayList<String> posDocuments = new ArrayList<String>();
	public ArrayList<String> negDocuments = new ArrayList<String>();
	public ArrayList<String> posDocumentsPOS = new ArrayList<String>();
	public ArrayList<String> negDocumentsPOS = new ArrayList<String>();
	public transient POSTagger tag;
	public HashMap<String,Integer> posWordOccurTimes = new HashMap<String,Integer>();
	public HashMap<String,Integer> negWordOccurTimes = new HashMap<String,Integer>();
	public HashMap<String,Integer> wordOccurTimes = new HashMap<String,Integer>();

	public HashMap<String,Double> featureMI = new HashMap<String,Double>();
	public HashSet<String> featureDict = new HashSet<String>();



	public GetFeatureByMI(String posFolderName,String negFolderName) throws Exception
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


    private void putIntoDict(HashMap<String,Integer> map,String item,ArrayList<String> alreadyPutInThisSentence)
    {
    	if(alreadyPutInThisSentence.contains(item))
    		return;
    	if(map.containsKey(item))
    	{
    		int times = map.get(item);
    		map.put(item,++times);
    	}
    	else
    	{
    		map.put(item,1);
    	}
    	alreadyPutInThisSentence.add(item);
    }
    public void posAndFilter() throws Exception
    {
    	if(posDocuments.size()==0)
    	{
    		this.readFileInFolder();
    	}

    	for(Iterator<String> iter = posDocuments.iterator();iter.hasNext();)
    	{
    		String temp = tag.tag(iter.next());
    		posDocumentsPOS.add(temp);
			
    		ArrayList<String> alreadyPutInThisSentence1 = new ArrayList<String>();
    		ArrayList<String> alreadyPutInThisSentence2 = new ArrayList<String>();
			
			String[] arrays = temp.split(" ");
			for(String ele:arrays)
			{
				String[] word_pos = ele.split("/");
				if(word_pos[1].equals("AD") || word_pos[1].equals("VV") || word_pos[1].equals("VA") || word_pos[1].equals("JJ"))
				{
					putIntoDict(posWordOccurTimes,ele,alreadyPutInThisSentence1);
					putIntoDict(wordOccurTimes,ele,alreadyPutInThisSentence2);
				}
			}
    	}


    	for(Iterator<String> iter = negDocuments.iterator();iter.hasNext();)
    	{
    		String temp = tag.tag(iter.next());
    		negDocumentsPOS.add(temp);
			
    		ArrayList<String> alreadyPutInThisSentence1 = new ArrayList<String>();
    		ArrayList<String> alreadyPutInThisSentence2 = new ArrayList<String>();

			String[] arrays = temp.split(" ");
			for(String ele:arrays)
			{
				String[] word_pos = ele.split("/");
				if(word_pos[1].equals("AD") || word_pos[1].equals("VV") || word_pos[1].equals("VA") || word_pos[1].equals("JJ"))
				{
					putIntoDict(negWordOccurTimes,ele,alreadyPutInThisSentence1);
					putIntoDict(wordOccurTimes,ele,alreadyPutInThisSentence2);
				}
			}
    	}    	
    }

    public void getFeatureByMI()
    {
    	int N =posDocuments.size() + negDocuments.size();// 训练文档中的总数
    	int n_pos = posDocuments.size();
    	int n_neg = negDocuments.size();
    	double pro_pos = (double)n_pos/N;
    	double pro_neg = (double)n_neg/N;
    	Set<String> keys = wordOccurTimes.keySet();
    	for(String key:keys)
    	{
    		int  a_pos = posWordOccurTimes.containsKey(key)? posWordOccurTimes.get(key):0;//pos类中包含key这个特征的文档频数
    		int  b_pos = negWordOccurTimes.containsKey(key)? negWordOccurTimes.get(key):0;//不属于pos类(也就是neg类)中包含key这个特征的文档频数
    		int  c_pos = posWordOccurTimes.containsKey(key)?(n_pos-posWordOccurTimes.get(key)):(n_pos);//属于pos类但没有包含key这个特征的文档频数
    		double I_pos_key = Math.log((double)a_pos*N/((a_pos+c_pos)*(a_pos+b_pos))); // I(ti,ci)=log(A*N/((A+C)*(A+B))

    		int  a_neg = negWordOccurTimes.containsKey(key)? negWordOccurTimes.get(key):0;//neg类中包含key这个特征的文档频数
    		int  b_neg = posWordOccurTimes.containsKey(key)? posWordOccurTimes.get(key):0;//不属于neg类(也就是pos类)中包含key这个特征的文档频数
    		int  c_neg = negWordOccurTimes.containsKey(key)? (n_neg-negWordOccurTimes.get(key)):(n_neg);//属于neg类但没有包含key这个特征的文档频数
    		double I_neg_key = Math.log((double)a_neg*N/((a_neg+c_neg)*(a_neg+b_neg)));

    		double finalValue = pro_pos*I_pos_key + pro_neg*I_neg_key;
    		featureMI.put(key,finalValue);

    		System.out.println(key+"   a_pos="+a_pos+"  b_pos="+b_pos+" c_pos="+c_pos+"  I_pos_key="+I_pos_key +"  a_neg="+a_neg+" b_neg="+b_neg+" c_neg="+c_neg + " I_neg_key="+I_neg_key +" finalValue="+finalValue);
    	}
    }


    public static void main(String[] args) throws Exception
	{
		GetFeatureByMI obj = new GetFeatureByMI("../corpus/all1/pos","../corpus/all1/neg");
    	obj.readFileInFolder();
    	obj.posAndFilter();

    	obj.outputArrayList(obj.posDocumentsPOS);
    	obj.outputArrayList(obj.negDocumentsPOS);

    	String[] temp = obj.posDocumentsPOS.get(0).split(" ");
    	obj.outputArrayList(new ArrayList<String>(Arrays.asList(temp[0].split("/"))));
    	System.out.println("---------------------------------------");

    	obj.outputHashMap(obj.posWordOccurTimes);
    	obj.outputHashMap(obj.negWordOccurTimes);

    	// obj.outputPOSResult("./posresult.txt");
    	// obj.outputWordFeature("./feature.txt");
    	System.out.println("---------------------------------------");
    	obj.getFeatureByMI();
    	obj.outputHashMap(obj.featureMI);
	}

	public void outputArrayList(ArrayList<String> list)
    {
    	Iterator<String> iter = list.iterator();
    	for(;iter.hasNext();)
    	{
    		System.out.println(iter.next());
    	}
    }
    public <T> void outputHashMap(HashMap<String,T> map)
    {
    	Set<Map.Entry<String,T>> sets=map.entrySet();
    	for(Map.Entry<String,T> m:sets)
    	{
    		System.out.println(m.getKey()+ "  " + m.getValue());
    	}
    }
    private void outputPOSResult(String fileName) throws Exception
    {
    	BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName)));
    	for(String result:posDocumentsPOS)
    	{
    		bw.write(result + "     POS\n");
    	}
    	bw.write("------------------------------------\n\n\n\n");
    	for(String result:negDocumentsPOS)
    	{
    		bw.write(result + "     NEG\n");
    	}
    	bw.close();
    }

    private void outputWordFeature(String fileName) throws Exception
    {
    	BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName)));
    	
		Set<Map.Entry<String,Integer>> sets=posWordOccurTimes.entrySet();
    	for(Map.Entry<String,Integer> m:sets)
    	{
    		bw.write(m.getKey()+ "  " + m.getValue()+"    POS\n");
    	}
    	bw.write("------------------------------------\n\n\n\n");
    	
    	sets=negWordOccurTimes.entrySet();
    	for(Map.Entry<String,Integer> m:sets)
    	{
    		bw.write(m.getKey()+ "  " + m.getValue()+"    NEG\n");
    	}
    	bw.close();
    }

}