package com.MI;

public class Prepare4TrainMI
{
	public GetFeatureByMI getFeatureByMIObj;

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






	public static void main(String[] args) throws Exception
	{
		Prepare4TrainMI prepare4TrainMIObj = new Prepare4TrainMI("../corpus/all1/pos","../corpus/all1/neg");
		prepare4TrainMIObj.getFeatureByMIObj.outputSortedSort(GetFeatureByMI.sortMiDict(prepare4TrainMIObj.getFeatureByMIObj.featureMI),30);
	}
}