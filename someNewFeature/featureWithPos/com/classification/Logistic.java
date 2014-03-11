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
				if(i!=0)
				{
					featureItem.set(Integer.valueOf(elements[i]),i-1);
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
			if(flag==1)
				posSamples.add(featureItem);	
			else if(flag==-1)
				negSamples.add(featureItem);
			else
				throw new Exception("somgthing wrong!!! flag==0");
		}
		br.close();
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
	}
}


