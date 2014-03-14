package com.classification;

import java.util.ArrayList;
import java.util.Properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;


public class MaximumEntropy
{
	public int iteratorTime;
	public int dimensions;
	public int posSampleNums;
	public int negSampleNums;
	public ArrayList<ArrayList<Integer>> posTrainSamples;
	public ArrayList<ArrayList<Integer>> negTrainSamples;

	public ArrayList<Integer> parameters;

	public ArrayList<Double> p_x_vector;
	public double p_x;
	public ArrayList<Double> p_xy_vector;
	public double p_xy;
	
	public MaximumEntropy(String configNames) throws Exception
	{
		Properties properties = new Properties();
		try
		{
			properties.load(new FileInputStream(new File(configNames)));
		}
		catch (Exception e)
		{
			throw new Exception("Properties load failed!!");
		}

		for(String key:properties.stringPropertyNames())
		{
			switch(key)
			{
				case "iteratorTime":
					iteratorTime = Integer.valueOf(properties.getProperty(key));
					break;
				case "dimensions":
					dimensions = 2 * Integer.valueOf(properties.getProperty(key));
					break;
				case "posSampleNums":
					posSampleNums = Integer.valueOf(properties.getProperty(key));
					break;
				case "negSampleNums":
					negSampleNums = Integer.valueOf(properties.getProperty(key));
					break;
			}
		}
	} 

	public void initialize(String formatFileName) throws Exception
	{
		posTrainSamples = new ArrayList<ArrayList<Integer>>();
		negTrainSamples = new ArrayList<ArrayList<Integer>>();
		
		ArrayList<Integer> constructs = new ArrayList<Integer>(dimensions);
		for(int i=0;i<dimensions;i++)
			constructs.add(0);

		BufferedReader br = new BufferedReader(new FileReader(new File(formatFileName)));
		String line = "";
		while((line=br.readLine())!=null)
		{
			ArrayList<Integer> featureItem = new ArrayList<Integer>(constructs);
			String[] elements = line.split(" ");
			int flag = 0;
			for(int i=0; i<elements.length; i++)
			{
				try
				{
					if(i!=0)
					{
						featureItem.set(i-1,Integer.valueOf(elements[i]));
					}
					else
					{
						if(elements[i].matches("\\+1.*"))
						{
							flag = 1;
						}
						else if(elements[i].matches("-1.*"))
						{
							flag =-1;
						}
					}
				}
				catch(IndexOutOfBoundsException e)
				{
					throw new Exception("input dimension less than real dimensions");
				}
			}
			if(flag==1)
			{
				ArrayList<Integer> temp = new ArrayList<Integer>(constructs);
				ArrayList<Integer> result = new ArrayList<Integer>();
				result.addAll(featureItem);
				result.addAll(temp);
				posTrainSamples.add(result);	
			}
			else if(flag==-1)
			{
				ArrayList<Integer> temp = new ArrayList<Integer>(constructs);
				ArrayList<Integer> result = new ArrayList<Integer>();
				result.addAll(temp);
				result.addAll(featureItem);
				negTrainSamples.add(result);
			}
			else
				throw new Exception("somgthing wrong!!! flag==0");
		}
		br.close();
	}

	public void trainingModel()
	{

	}

	private void computeP_X(boolean onOff)
	{
		if(onOff == false)
		{
			p_x = 1/(posSampleNums+negSampleNums);
			return;
		}


	}
	private void computeP_XY(boolean onOff)
	{
		if(onOff == false)
		{
			p_xy = 1/(posSampleNums+negSampleNums);
			return;
		}
	}







	public static void main(String[] args) throws Exception
	{
		MaximumEntropy maximumEntropy = new MaximumEntropy("./tempResult/properties.out");
		System.out.println("dimensions="+maximumEntropy.dimensions);
		System.out.println("iteratorTime="+maximumEntropy.iteratorTime);
		System.out.println("maximumEntropy.posSampleNums="+maximumEntropy.posSampleNums);
		System.out.println("maximumEntropy.negSampleNums="+maximumEntropy.negSampleNums);
		System.out.println("----------------------------------");

		maximumEntropy.initialize("./tempResult/trainFormat.out");
		System.out.println(maximumEntropy.posTrainSamples.size());
		System.out.println(maximumEntropy.negTrainSamples.size());
	}




}