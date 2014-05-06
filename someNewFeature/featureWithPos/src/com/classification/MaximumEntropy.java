package com.classification;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Collections;

import java.io.File;
import java.io.FileInputStream;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;


public class MaximumEntropy
{
	public int iteratorTime;
	public int dimensions;
	public int posSampleNums;
	public int negSampleNums;
	public ArrayList<ArrayList<Integer>> posTrainSamples;
	public ArrayList<ArrayList<Integer>> negTrainSamples;
	public ArrayList<ArrayList<Integer>> posTestSamples;
	public ArrayList<ArrayList<Integer>> negTestSamples;
	public ArrayList<Boolean> posResult;
	public ArrayList<Boolean> negResult;


	public ArrayList<Double> p_x_pos_vector;
	public ArrayList<Double> p_x_neg_vector;
	public double p_x;
	public ArrayList<Double> p_xy_pos_vector;
	public ArrayList<Double> p_xy_neg_vector;
	public double p_xy;
	public ArrayList<Double> parameters;// 它的维度是实际的2倍，因为我们要训练在不同的class label下的相关参数
	public int C;    //这是我模型迭代的时候用的参数，类似学习率的东西
	public ArrayList<Double> expectPXY_f;//这是我后来领悟,其实看了stanford的那个介绍，这里面直接数就可以了

	public ArrayList<ArrayList<Integer>> posTrainSamplesRotate;
	public ArrayList<ArrayList<Integer>> negTrainSamplesRotate;

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
		posTrainSamples = new ArrayList<ArrayList<Integer>>(posSampleNums);
		negTrainSamples = new ArrayList<ArrayList<Integer>>(negSampleNums);
		
		ArrayList<Integer> constructs = new ArrayList<Integer>(dimensions/2);
		for(int i=0;i<dimensions/2;i++)
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

		//我之前是在计算P(y|x)的时候才算属于另一个label的概率，也就是要反转sample的list,这样每次都需要重新构造，现在直接在initialize的时候初始化
		posTrainSamplesRotate = new ArrayList<ArrayList<Integer>>(posSampleNums);
		negTrainSamplesRotate = new ArrayList<ArrayList<Integer>>(negSampleNums);
		for(ArrayList<Integer> pos:posTrainSamples)
		{
			ArrayList<Integer> temp = new ArrayList<Integer>(pos);
			Collections.rotate(temp,dimensions/2);
			posTrainSamplesRotate.add(temp);
		}
		for(ArrayList<Integer> neg:negTrainSamples)
		{
			ArrayList<Integer> temp = new ArrayList<Integer>(neg);
			Collections.rotate(temp,dimensions/2);
			negTrainSamplesRotate.add(temp);
		}
	}

	private void computeC()
	{
		expectPXY_f = new ArrayList<Double>(dimensions);
		for(int j=0;j<dimensions;j++)
		{
			expectPXY_f.add(0.0);
		}

		for(ArrayList<Integer> list :posTrainSamples)
		{
			for(int i=0;i<dimensions;i++)
			{
				int temp = list.get(i) + (expectPXY_f.get(i).intValue());
				expectPXY_f.set(i,(double)temp);
			}
		}
		for(ArrayList<Integer> list:negTrainSamples)
		{
			for(int i=0;i<dimensions;i++)
			{
				int temp = list.get(i) + (expectPXY_f.get(i).intValue());
				expectPXY_f.set(i,(double)temp);
			}
		}
		C=(Collections.max(expectPXY_f)).intValue();
		System.out.println("C is " + C);
	}
    
    //我用stanford的maxent的介绍的GIS,它里面的ExpectEmpirical都会有一个1/N,model的Expect也是有1/N的，因为是除法关系，直接约分了。
	public void trainingModel() throws Exception
	{
		if(expectPXY_f==null)
			computeC();
		
		for(int time=0; time<iteratorTime; time++)
		{
			for(int i=0; i<dimensions; i++)
			{
				double expectEmpirical_featureI = expectPXY_f.get(i);
				
				//这里需要改进，这里完全没有做平滑
				if(expectEmpirical_featureI==0)
					continue;
				
				double expectIter = computeExpect_f_currentModel(i);
				double lambda_i = parameters.get(i);
				// double temp =(1/(double)C)*Math.log(expectEmpirical_featureI/expectIter);
				// System.out.println("C:"+C+" expectEmpirical_featureI:"+ expectEmpirical_featureI + " expectIter " + expectIter + "  temp " +temp);
				// parameters.set(i,lambda_i+temp);
				lambda_i += (1/(double)C)*Math.log(expectEmpirical_featureI/expectIter);
				parameters.set(i,lambda_i);
			
				if(i%100==0)
					System.out.print(".");
			}
			System.out.println("\ntraining the " + time + " time....");
			
			// PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("./tempResult/MaxentTrainResult.out",true)));
			// pw.println("parameters " + time +" : ");
			// for(int j=0;j<dimensions;j++)
			// {
			// 	pw.print(parameters.get(j) + " ");
			// }
			// pw.println();
			// pw.close();
		}
	}

	private double computeP_YgivenX(ArrayList<Integer> list,ArrayList<Integer> rotateList)
	{
		double temp1 = computeInner(list,parameters);
		double temp2 = computeInner(rotateList,parameters);	
		return Math.exp(temp1)/(Math.exp(temp1)+Math.exp(temp2));

	}	
	private double computeInner(ArrayList<Integer> feature,ArrayList<Double> para)
	{
		double sum=0;
		for(int i=0;i<feature.size();i++)
			sum+=feature.get(i)*para.get(i);
		return sum;
	}

	private double computeExpect_f_currentModel(int dimensionNum) throws Exception
	{
		double result =0.0;
		if(dimensionNum < parameters.size()/2)
		{
			for(int j=0;j<posSampleNums;j++)
			{
				double f_i = posTrainSamples.get(j).get(dimensionNum);
				if(f_i!=0 && f_i!=1.0)
				{
					System.out.println(j+"  "+ dimensionNum);
					Thread.sleep(2000);
				}

				if(f_i==0)
					continue;
				double p_y_givenx = computeP_YgivenX(posTrainSamples.get(j),posTrainSamplesRotate.get(j));
				result += p_y_givenx*f_i;
			}
		}
		else
		{
			for(int j=0;j<negSampleNums;j++)
			{
				double f_i = negTrainSamples.get(j).get(dimensionNum);

				if(f_i!=0 && f_i!=1.0)
				{
					System.out.println(j+"  "+ dimensionNum);
					Thread.sleep(2000);
				}

				if(f_i==0)
					continue;
				double p_y_givenx = computeP_YgivenX(negTrainSamples.get(j),negTrainSamplesRotate.get(j));
				result += p_y_givenx*f_i;
			}
		}
		// System.out.println("result " +result);
		return result;
	}

	public void testInitialize(String testformatFile) throws Exception
	{
		negTestSamples = new ArrayList<ArrayList<Integer>>();
		posTestSamples = new ArrayList<ArrayList<Integer>>();
		
		ArrayList<Integer> constructs = new ArrayList<Integer>(dimensions/2);
		for(int i=0;i<dimensions/2;i++)
			constructs.add(0);

		BufferedReader br = new BufferedReader(new FileReader(new File(testformatFile)));
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
				posTestSamples.add(featureItem);	
			}
			else if(flag==-1)
			{
				negTestSamples.add(featureItem);
			}
			else
				throw new Exception("read test format somgthing wrong!!! flag==0");
		}
		br.close();

		// System.out.println(posTestSamples.size());
		// System.out.println(negTestSamples.size());
		// System.out.println(posTestSamples.get(0).size());
		// System.out.println(posTestSamples.get(0).size());
	}
	public void test() throws Exception
	{
		posResult = new ArrayList<Boolean>(posTestSamples.size());
		negResult = new ArrayList<Boolean>(posTestSamples.size());
		for(ArrayList<Integer> list:posTestSamples)
		{
			switch(getResult4tTest(list))
			{
				case 1:
					posResult.add(true);
					break;
				case 0:
					posResult.add(false);
					break;
			}
		}
		for(ArrayList<Integer> list:negTestSamples)
		{
			switch(getResult4tTest(list))
			{
				case 1:
					negResult.add(false);
					break;
				case 0:
					negResult.add(true);
					break;
			}
		}
		double[] accuracy = computeAccuracy(posResult,negResult);
		double posRecall = computeRecall(posResult);
		double negRecall = computeRecall(negResult);
		double posf_score = computeFScore(accuracy[0],posRecall);
		double negf_score = computeFScore(accuracy[1],negRecall);
		System.out.println("posAccuracy=" + accuracy[0] + ",posRecall=" + posRecall + ",posFscore=" + posf_score);
		System.out.println("negAccuracy=" + accuracy[1] + ",negRecall=" + negRecall + ",negFscore=" + negf_score);
	}

	private int getResult4tTest(ArrayList<Integer> testList)
	{
		double sum_pos =0.0;
		double sum_neg =0.0;
		for(int i=0;i<dimensions/2;i++)
		{
			sum_pos+= ((double)testList.get(i))*parameters.get(i);
			sum_neg+= ((double)testList.get(i))*parameters.get(dimensions/2 +i);
		}
		double posProbability = Math.exp(sum_pos)/(Math.exp(sum_pos)+ Math.exp(sum_neg));
		double negProbability = Math.exp(sum_neg)/(Math.exp(sum_pos)+ Math.exp(sum_neg));
		return posProbability>=negProbability ? 1:0;
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
		MaximumEntropy maximumEntropy = new MaximumEntropy("./tempResult/properties.out");
		System.out.println("dimensions="+maximumEntropy.dimensions);
		System.out.println("iteratorTime="+maximumEntropy.iteratorTime);
		System.out.println("maximumEntropy.posSampleNums="+maximumEntropy.posSampleNums);
		System.out.println("maximumEntropy.negSampleNums="+maximumEntropy.negSampleNums);
		System.out.println("----------------------------------");

		System.out.println("initialize....");		
		maximumEntropy.initialize("./tempResult/trainFormat.out");
		System.out.println(maximumEntropy.posTrainSamples.size());
		System.out.println(maximumEntropy.negTrainSamples.size());

		System.out.println("compute C.....");
		maximumEntropy.computeC();
		System.out.println(maximumEntropy.C);


		System.out.println("training model...");
		maximumEntropy.trainingModel();


		maximumEntropy.outputTrainResult("./tempResult/MaxentTrainResult.out");

		maximumEntropy.testInitialize("./tempResult/testFormat.out");

		maximumEntropy.test();
	}



	private void outputTrainResult(String fileName) throws Exception
	{
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
		for(int i=0;i<dimensions;i++)
		{
			bw.write(parameters.get(i)+" ");
		}
		bw.write("\n");
		bw.close();
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
}

enum Classification
{
	POS(1),NEG(0);

	private int statusCode;
	private Classification(int i)
	{
		statusCode = i;
	}
	public int getStatusCode()
	{
		return statusCode;
	}
}