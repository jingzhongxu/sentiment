import java.util.ArrayList;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

import java.util.HashMap;



public SoftmaxRegression
{
	public HashMap<ArrayList<Double>,Integer> samples;
	public ArrayList<ArrayList<Double>> parameters;
	public int classNums;
	public int parameterNums;
	public int sampleNums;

	public SoftmaxRegression(int classNums,int parameters)
	{
		this.classNums = classNums;
		this.parameters = parameters;
	}

	public void getSamples(File file) throws Exception
	{
		samples = new HashMap<ArrayList<Double>,Integer>();

		ArrayList<Double> oneStarSample = new ArrayList<Double>();
		ArrayList<Double> twoStarSample = new ArrayList<Double>();
		ArrayList<Double> threeStarSample = new ArrayList<Double>();
		ArrayList<Double> fourStarSample = new ArrayList<Double>();
		ArrayList<Double> fiveStarSample = new ArrayList<Double>();

		BufferedReader br = new BufferedReader(new FileReader(file));


	}






}