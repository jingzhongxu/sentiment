package com.classification;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileReader;

import java.util.Properties;
import java.util.Collections;
import java.util.ArrayList;

public class Logistic
{
	public int iteratorTime;
	public int dimensions;
	public int posSampleNums;
	public int negSampleNums;
	public double alpha;
	public ArrayList<ArrayList<Integer>> posSamples;
	public ArrayList<ArrayList<Integer>> negSamples;
	public ArrayList<Double> parameters;

	public Logistic(String configFileName) throws Exception
	{
		Properties properties = new Properties();
		try
		{
			properties.load(new FileInputStream(new File(configFileName)));
		}
		catch(Exception e) 
		{
			System.out.println("Properties load failed!!");
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
					dimensions = Integer.valueOf(properties.getProperty(key));
					break;
				case "posSampleNums":
					posSampleNums = Integer.valueOf(properties.getProperty(key));
					break;
				case "negSampleNums":
					negSampleNums = Integer.valueOf(properties.getProperty(key));
					break;
				case "alpha":
					alpha = Double.valueOf(properties.getProperty(key));
					break;
			}
		}
	}

	public void initialize(String formatFileName) throws Exception
	{
		posSamples = new ArrayList<ArrayList<Integer>>();
		negSamples = new ArrayList<ArrayList<Integer>>();
		
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
						// if(Integer.valueOf(elements[i])==1)
						// 	System.out.println("ddd");
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
					t`hrow new Exception("input dimension less than real dimensions");
				}
			}
			if(flag==1)
				posSamples.add(featureItem);	
			else if(flag==-1)
				negSamples.add(featureItem);
			else
				throw new Exception("somgthing wrong!!! flag==0");
		}
		br.close();
	}

	public void trainingModel()
	{
		if(iteratorTime<50)
			iteratorTime=50;
		
		parameters = new ArrayList<Double>(dimensions);
		for(int i=0;i<dimensions;i++)
		{
			parameters.add(0);
		}

		for(int times=1;times<iteratorTime;times++)
		{
			for(ArrayList<Integer> samples:posSamples)
			{
				double y_i = 1;
				double h_function_x_i=computeFunctionH(samples,parameters);
				for(int j=0;j<dimensions;j++)
				{
					double x_i_j = samples.get(j);
					if(x_i_j==0)
						return;
					double theta = parameters.get(j);
					theta += alpha * (y_i - h_function_x_i)*x_i_j;
					parameters.set(j,theta);
				}
			}

			for(ArrayList<Integer> samples:negSamples)
			{
				double y_i = 0;
				double h_function_x_i=computeFunctionH(samples,parameters);
				for(int j=0;j<dimensions;j++)
				{
					double x_i_j = samples.get(j);
					if(x_i_j==0)
						return;
					double theta = parameters.get(j);
					theta += alpha * (y_i - h_function_x_i)*x_i_j;
					parameters.set(j,theta);
				}
			}
		}

	}

	/******************************** compute   H function  **************************************/
	private double computeFunctionH(ArrayList<Integer> samples,ArrayList<Double> parameters)
	{
		double sum=0;
		for(int i=0;i<dimensions;i++)
		{
			double value = samples.get(i) * parameters.get(i);
			sum+=value;
		}
		double finalValue = 1/(1+Math.exp((-1)*sum));
		return finalValue;
	}




	public static void main(String[] args) throws Exception
	{
		Logistic logistic = new Logistic("./tempResult/properties.out");
		System.out.println(logistic.dimensions);
		System.out.println(logistic.iteratorTime);
		System.out.println(logistic.alpha);
		System.out.println(logistic.posSampleNums);
		System.out.println(logistic.negSampleNums);
		System.out.println("----------------------------------");

		logistic.initialize("./tempResult/trainFormat.out");
		System.out.println(logistic.posSamples.size());
		System.out.println(logistic.negSamples.size());
		System.out.println(Collections.frequency(logistic.posSamples.get(0),1));
		// Logistic.outputList(logistic.posSamples.get(0));

	}

	private static void outputList(ArrayList<Integer> list)
	{
		for(int i:list)
		{
			System.out.print(i+" ");
		}
		System.out.println();
	}
}


