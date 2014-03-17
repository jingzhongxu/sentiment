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


	public ArrayList<Double> p_x_pos_vector;
	public ArrayList<Double> p_x_neg_vector;
	public double p_x;
	public ArrayList<Double> p_xy_pos_vector;
	public ArrayList<Double> p_xy_neg_vector;
	public double p_xy;
	public ArrayList<Double> parameters;// 它的维度是实际的2倍，因为我们要训练在不同的class label下的相关参数
	public ArrayList<Double> expectPXY_f;/*这是我feature的期望，就是我训练的时候需要去让我的p(x)p(y|x)f(x,y)=p(x,y)f(x,y)这个等式的右边*/
	public int C;    //这是我模型迭代的时候用的参数，类似学习率的东西


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

	private void computeEpxyf(boolean flag)
	{
		if(p_x==0.0 && p_x_pos_vector==null)
			computeP_X(flag);
		if(p_xy==0.0 && p_xy_pos_vector==null)
			computeP_XY(flag);

		expectPXY_f = new ArrayList<Double>(dimensions);
		for(int i=0;i<dimensions;i++)
		{
			expectPXY_f.set(i,p_)
		}

			



	}




	private void computeC()
	{
		int[] array = new int[dimensions];
		int max =0;
		
		for(ArrayList<Integer> list :posTrainSamples)
		{
			for(int i=0;i<dimensions;i++)
			{
				array[i]+=list.get(i);
			}
		}
		for(ArrayList<Integer> list:negTrainSamples)
		{
			for(int i=0;i<dimensions;i++)
			{
				array[i]+=list.get(i);
			}
		}

		for(int j=0;j<dimensions;j++)
		{
			if(array[j]<max)
				continue;
			else
			{
				max=array[j];
			}
		}
		C=max;
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

		System.out.println("compute C.....");
		maximumEntropy.computeC();
		System.out.println(maximumEntropy.C);

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