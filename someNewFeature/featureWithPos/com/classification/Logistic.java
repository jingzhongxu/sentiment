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
	public ArrayList<ArrayList<Integer>> posTrainSamples;
	public ArrayList<ArrayList<Integer>> negTrainSamples;
	public ArrayList<ArrayList<Integer>> posTestSamples;
	public ArrayList<ArrayList<Integer>> negTestSamples;

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
					throw new Exception("input dimension less than real dimensions");
				}
			}
			if(flag==1)
				posTrainSamples.add(featureItem);	
			else if(flag==-1)
				negTrainSamples.add(featureItem);
			else
				throw new Exception("somgthing wrong!!! flag==0");
		}
		br.close();
	}

	public void trainingModel() throws Exception
	{
		if(posTrainSamples==null)
		{
			throw new Exception("initialize must before training!");
		}

		if(iteratorTime<50)
			iteratorTime=50;
		System.out.println("iteratorTime = "+ iteratorTime);


		parameters = new ArrayList<Double>(dimensions);
		for(int i=0;i<dimensions;i++)
		{
			parameters.add(0.0);
		}

		for(int times=1;times<iteratorTime;times++)
		{
			if(times%10==0)
				System.out.println("training.... the "+ times+" iterator. Need "+iteratorTime);

			for(ArrayList<Integer> samples:posTrainSamples)
			{
				double y_i = 1;
				double h_function_x_i=computeFunctionH(samples,parameters);
				for(int j=0;j<dimensions;j++)
				{
					double x_i_j = samples.get(j);
					if(x_i_j==0)
						continue;
					double theta = parameters.get(j);
					theta += alpha * (y_i - h_function_x_i)*x_i_j;
					parameters.set(j,theta);
				}
			}

			for(ArrayList<Integer> samples:negTrainSamples)
			{
				double y_i = 0;
				double h_function_x_i=computeFunctionH(samples,parameters);
				for(int j=0;j<dimensions;j++)
				{
					double x_i_j = samples.get(j);
					if(x_i_j==0)
						continue;
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


	public void test(String testFormatFileName) throws Exception
	{
		if(parameters==null)
		{
			throw new Exception("training must before test!");
		}

		posTestSamples = new ArrayList<ArrayList<Integer>>();
		negTestSamples = new ArrayList<ArrayList<Integer>>();
		
		ArrayList<Integer> constructs = new ArrayList<Integer>(dimensions);
		for(int i=0;i<dimensions;i++)
			constructs.add(0);

		BufferedReader br = new BufferedReader(new FileReader(new File(testFormatFileName)));
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
					throw new Exception("input dimension less than real dimensions");
				}
			}
			if(flag==1)
				posTestSamples.add(featureItem);	
			else if(flag==-1)
				negTestSamples.add(featureItem);
			else
				throw new Exception("somgthing wrong!!! flag==0");
		}
		br.close();
		System.out.println("posTestSamples.size()=" + posTestSamples.size());
		System.out.println("negTestSamples.size()=" + negTestSamples.size());


		ArrayList<Boolean> posResult = new ArrayList<Boolean>();
		ArrayList<Boolean> negResult = new ArrayList<Boolean>();

		for(ArrayList<Integer> samples:posTestSamples)
		{
			double finalValue = computeFunctionH(samples,parameters);
			if(finalValue >=0.5)
				posResult.add(true);
			else
				posResult.add(false);
		}
		for(ArrayList<Integer> samples:negTestSamples)
		{
			double finalValue = computeFunctionH(samples,parameters);
			if(finalValue <=0.5)
				negResult.add(true);
			else
				negResult.add(false);
		}

		System.out.println("The test result is:");
		double[] accuracy = computeAccuracy(posResult,negResult);
		double posRecall = computeRecall(posResult);
		double negRecall = computeRecall(negResult);

		System.out.println("posAccuracy=" + accuracy[0] + ",posRecall=" + posRecall + ",posFscore=" + computeFScore(accuracy[0],posRecall));
		System.out.println("negAccuracy=" + accuracy[1] + ",negRecall=" + negRecall + ",negFscore=" + computeFScore(accuracy[1],negRecall));
	}

	private double[] computeAccuracy(ArrayList<Boolean> posList,ArrayList<Boolean> negList)
	{
		double [] accuracy = new double[2];
		int postrue = Collections.frequency(posList,true);
		int posFlase = posList.size()-postrue;
		int negtrue = Collections.frequency(negList,true);
		int negfalse = negList.size()-negtrue;

		double posAccuracy = (double)postrue/(postrue+negfalse);
		double negAccuracy = (double)negtrue/(negtrue+posFlase);
		accuracy[0] = posAccuracy;
		accuracy[1] = negAccuracy;
		return accuracy;
	}
	
	private double computeRecall(ArrayList<Boolean> list)
	{
		int times = Collections.frequency(list,true);
		return (double)times/list.size();
	}

	private double computeFScore(double accuracy,double recall)
	{
		return (2*accuracy*recall/(accuracy+recall));
	}

	public static void main(String[] args) throws Exception
	{
		Logistic logistic = new Logistic("./tempResult/properties.out");
		System.out.println("dimensions="+logistic.dimensions);
		System.out.println("iteratorTime="+logistic.iteratorTime);
		System.out.println("alpha="+logistic.alpha);
		System.out.println("logistic.posSampleNums="+logistic.posSampleNums);
		System.out.println("logistic.negSampleNums="+logistic.negSampleNums);
		System.out.println("----------------------------------");

		logistic.initialize("./tempResult/trainFormat.out");
		System.out.println(logistic.posTrainSamples.size());
		System.out.println(logistic.negTrainSamples.size());
		// System.out.println(Collections.frequency(logistic.posTrainSamples.get(0),1));
		// Logistic.outputList(logistic.posTrainSamples.get(0));

		logistic.trainingModel();
		System.out.println("0 nums in para="+Collections.frequency(logistic.parameters,0.0));

		logistic.test("./tempResult/testFormat.out");
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