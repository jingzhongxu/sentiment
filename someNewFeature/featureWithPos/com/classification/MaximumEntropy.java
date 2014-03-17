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

	public ArrayList<Double> p_x_pos_vector;
	public ArrayList<Double> p_x_neg_vector;
	public double p_x;
	public ArrayList<Double> p_xy_pos_vector;
	public ArrayList<Double> p_xy_neg_vector;
	public double p_xy;
	public ArrayList<Double> parameters;
	
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

		parameters = new ArrayList<Double>(dimensions);
		for(int i=0;i<dimensions;i++)
			parameters.add(0.0);
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

		p_x_pos_vector = new ArrayList<Double>(posSampleNums);
		p_x_neg_vector = new ArrayList<Double>(negSampleNums);


		for(int j=0;j<posSampleNums;j++)
		{
			int nums = 0;
			ArrayList<Integer> x = posTrainSamples.get(j);
			for(ArrayList<Integer> temp:posTrainSamples)
			{
				if(isSame(x,temp))
					nums++;
			}
			for(ArrayList<Integer> temp: negTrainSamples)
			{
				if(isSame(x,temp))
					nums++;
			}
			p_x_pos_vector.add((double)nums/posSampleNums);		
		}
		for(int j=0;j<negSampleNums;j++)
		{
			int nums = 0;
			ArrayList<Integer> x = negTrainSamples.get(j);
			for(ArrayList<Integer> temp:posTrainSamples)
			{
				if(isSame(x,temp))
					nums++;
			}
			for(ArrayList<Integer> temp: negTrainSamples)
			{
				if(isSame(x,temp))
					nums++;
			}
			p_x_neg_vector.add((double)nums/negSampleNums);		
		}
	}
	private boolean isSame(ArrayList<Integer> list1,ArrayList<Integer>list2)
	{
		boolean flag = true;
		for(int i=0;i<dimensions;i++)
		{
			if(list1.get(i)==list2.get(i))
				continue;
			else
			{
				flag = false;
				return flag;
			}	
		}
		return flag;
	}
	private void computeP_XY(boolean onOff)
	{
		if(onOff == false)
		{
			p_xy = 1/(posSampleNums+negSampleNums);
			return;
		}

		p_xy_pos_vector = new ArrayList<Double>(posSampleNums);
		p_xy_neg_vector = new ArrayList<Double>(negSampleNums);
		// for(int i=0;i<posSampleNums;i++)
		// 	p_xy_pos_vector.add(0.0);
		// for(int i=0;i<negSampleNums;i++)
		// 	p_xy_neg_vector.add(0.0);

		for(int j=0;j<posSampleNums;j++)
		{
			int nums = 0;
			ArrayList<Integer> x = posTrainSamples.get(j);
			for(ArrayList<Integer> temp:posTrainSamples)
			{
				if(isSame(x,temp))
					nums++;
			}
			p_xy_pos_vector.add((double)nums/posSampleNums);		
		}
		for(int j=0;j<negSampleNums;j++)
		{
			int nums = 0;
			ArrayList<Integer> x = negTrainSamples.get(j);
			for(ArrayList<Integer> temp:negTrainSamples)
			{
				if(isSame(x,temp))
					nums++;
			}
			p_xy_neg_vector.add((double)nums/posSampleNums);		
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

		System.out.println("maximumEntropy computeP_X......");
		maximumEntropy.computeP_X(true);
		System.out.println(maximumEntropy.p_x_neg_vector.size());
		System.out.println("random test:" + maximumEntropy.p_x_pos_vector.get(0));

		System.out.println("maximumEntropy computeP_XY......");
		maximumEntropy.computeP_XY(true);
		System.out.println(maximumEntropy.p_xy_neg_vector.size());
		System.out.println("random test:"+ maximumEntropy.p_xy_pos_vector.get(1));
	}




}