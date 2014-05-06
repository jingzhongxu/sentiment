import java.io.ObjectInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;


import edu.fudan.ml.types.Dictionary;
import edu.fudan.nlp.cn.tag.CWSTagger;
import edu.fudan.nlp.cn.tag.POSTagger;

public class Prepare4Train4ProcessFolder
{
	public ProcessFolder processFolderObj;
	public ArrayList<ArrayList<Integer>> posRepresentVector = new ArrayList<ArrayList<Integer>>();
	public ArrayList<ArrayList<Integer>> negRepresentVector = new ArrayList<ArrayList<Integer>>();
	public ArrayList<String> wordSets;

	public Prepare4Train4ProcessFolder(){}

	public Prepare4Train4ProcessFolder(ProcessFolder obj)
	{
		this.processFolderObj = obj;
	}

	public void readObj(File file) throws Exception
	{
		/**if use constructor with no parameter**/
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
		this.processFolderObj = (ProcessFolder)ois.readObject();
		ois.close();
	}

	public void initialize()
	{
		this.wordSets = new ArrayList<String>(processFolderObj.wordWithPOSDict);
		int dimensionNums = wordSets.size();
		int lengthPosVectors = processFolderObj.posDocumentsPOS.size();
		int lengthNegVectors = processFolderObj.negDocumentsPOS.size();

		ArrayList<Integer> list4Construct = new ArrayList<Integer>(dimensionNums);
		for(int i=0;i<dimensionNums;i++)
		{
			list4Construct.add(0);
		}


		for(int i=0;i<lengthPosVectors;i++)
		{
			ArrayList<Integer> innerList = new ArrayList<Integer>(list4Construct);
			String[] tokens = processFolderObj.posDocumentsPOS.get(i).split(" ");
			for(String token:tokens)
			{
				int index = wordSets.indexOf(token);
				if (index!=-1)
					innerList.set(index,1);
			}
			posRepresentVector.add(innerList);
		}
		for(int i=0;i<lengthNegVectors;i++)
		{
			ArrayList<Integer> innerList = new ArrayList<Integer>(list4Construct);
			String[] tokens = processFolderObj.negDocumentsPOS.get(i).split(" ");
			for(String token:tokens)
			{
				int index = wordSets.indexOf(token);
				if (index!=-1)
					innerList.set(index,1);
			}
			negRepresentVector.add(innerList);
		}
	}

	public static void main(String[] args) throws Exception
	{
    	Prepare4Train4ProcessFolder prepare4Train4ProcessFolderObj = new Prepare4Train4ProcessFolder();
    	prepare4Train4ProcessFolderObj.readObj(new File("./processFolder.ser"));
    	System.out.println(prepare4Train4ProcessFolderObj.processFolderObj.posDocumentsPOS.size());
    	System.out.println(prepare4Train4ProcessFolderObj.processFolderObj.negDocumentsPOS.size());
		prepare4Train4ProcessFolderObj.initialize();
    	System.out.println(prepare4Train4ProcessFolderObj.posRepresentVector.size());
    	System.out.println(prepare4Train4ProcessFolderObj.posRepresentVector.get(1).size());
    	System.out.println(prepare4Train4ProcessFolderObj.negRepresentVector.size());
    	System.out.println(prepare4Train4ProcessFolderObj.negRepresentVector.get(1).size());			
	}
}