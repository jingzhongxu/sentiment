package com.MI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;

public class Prepare4TrainMI
{
	public GetFeatureByMI getFeatureByMIObj;
	public ArrayList<String> finalFeature;  
	public ArrayList<ArrayList<Integer>> posRepresent = new ArrayList<ArrayList<Integer>>();
	public ArrayList<ArrayList<Integer>> negRepresent = new ArrayList<ArrayList<Integer>>();
 
	public Prepare4TrainMI(String posFoldName,String negFoleName) throws Exception
	{
		this.getFeatureByMIObj = new GetFeatureByMI(posFoldName,negFoleName);
		getFeatureByMIObj.readFileInFolder();
		getFeatureByMIObj.posAndFilter();
		getFeatureByMIObj.getFeatureByMI();
	}

	public Prepare4TrainMI(GetFeatureByMI obj) throws Exception
	{
		getFeatureByMIObj=obj;
		if(getFeatureByMIObj.featureMI.size()==0)
			getFeatureByMIObj.getFeatureByMI();
	}

	public void setFinalFeature(int featureNums)
	{
		LinkedList<Map.Entry<String,Double>> list = GetFeatureByMI.sortMiDict(getFeatureByMIObj.featureMI);	
		int i=0;
		finalFeature = new ArrayList<String>();
		for(Map.Entry<String,Double> map_entry:list)
		{
			if(++i<featureNums)
			{
				String feature = map_entry.getKey();
				finalFeature.add(feature);
			}
			else
			{
				break;
			}
		}	
	} 

	public void getRepresetnt() throws Exception
	{
		if(finalFeature==null)
		{
			throw new Exception("You must setFinalFeature with featureNums First!!");
		}

		int dimension = finalFeature.size();

		ArrayList<Integer> consturctUse = new ArrayList<Integer>(dimension);
		for(int i=0;i<dimension;i++)
		{
			consturctUse.add(0);
		}


		for(int i=0;i<getFeatureByMIObj.posDocumentsPOS.size();i++)
		{
			ArrayList<Integer> itemRepresent = new ArrayList<Integer>(consturctUse);
			String posSentence = getFeatureByMIObj.posDocumentsPOS.get(i);
			String[] temps = posSentence.split(" ");
			for(String temp : temps)
			{
				int index = finalFeature.indexOf(temp);
				if(index!=-1)
				{
					itemRepresent.set(index,1);
				}
			}
			posRepresent.add(itemRepresent);
		}
		for(int i=0;i<getFeatureByMIObj.negDocumentsPOS.size();i++)
		{
			ArrayList<Integer> itemRepresent = new ArrayList<Integer>(consturctUse);
			String negSentence = getFeatureByMIObj.negDocumentsPOS.get(i);
			
			String[] temps = negSentence.split(" ");
			for(String temp : temps)
			{
				int index = finalFeature.indexOf(temp);
				if(index!=-1)
				{
					itemRepresent.set(index,1);
				}
			}
			negRepresent.add(itemRepresent);
		}
	}


	public static void main(String[] args) throws Exception
	{
		Prepare4TrainMI prepare4TrainMIObj = new Prepare4TrainMI("../corpus/all/pos","../corpus/all/neg");
		// prepare4TrainMIObj.getFeatureByMIObj.outputSortedSort(GetFeatureByMI.sortMiDict(prepare4TrainMIObj.getFeatureByMIObj.featureMI),30);
		prepare4TrainMIObj.setFinalFeature((int)(prepare4TrainMIObj.getFeatureByMIObj.featureMI.size() * 0.75));
		prepare4TrainMIObj.getRepresetnt();

		System.out.println(prepare4TrainMIObj.posRepresent.size());
		System.out.println(prepare4TrainMIObj.negRepresent.size());
		System.out.println(prepare4TrainMIObj.posRepresent.get(0).size());
		System.out.println(prepare4TrainMIObj.negRepresent.get(0).size());

		Prepare4TrainMI.outputAsTrainFormat(prepare4TrainMIObj.posRepresent,prepare4TrainMIObj.negRepresent,"./tempResult/trainFormat.out");
	}

	public static <T extends Number> void outputAsTrainFormat(ArrayList<ArrayList<T>> poslist,ArrayList<ArrayList<T>> neglist,String fileName) throws Exception
	{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName)));
		for(ArrayList<T> listItem:poslist)
		{
			bw.write("+1: ");
			for(int i=0;i<listItem.size();i++)
			{
				bw.write(listItem.get(i) + " ");
			}
			bw.write("\n");
		}
		for(ArrayList<T> listItem:neglist)
		{
			bw.write("-1: ");
			for(int i=0;i<listItem.size();i++)
			{
				bw.write(listItem.get(i) + " ");
			}
			bw.write("\n");
		}
		bw.close();
	}
}