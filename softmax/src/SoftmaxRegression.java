import java.util.ArrayList;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

import java.util.HashMap;
import java.text.MessageFormat;


public class SoftmaxRegression
{
	public HashMap<Integer,ArrayList<ArrayList<Double>>> samples;
	public ArrayList<ArrayList<Double>> parameters;
	public int starNums = 5;//多分类的类别数
	
	public int parameterNums;//参数的维度
	public int sampleNums = 0;

	ArrayList<Double> singleParameter;
	ArrayList<ArrayList<Double>> oneStarSamples;
	ArrayList<ArrayList<Double>> twoStarSamples;
	ArrayList<ArrayList<Double>> threeStarSamples;
	ArrayList<ArrayList<Double>> fourStarSamples;
	ArrayList<ArrayList<Double>> fiveStarSamples;

	public SoftmaxRegression(int starNums,int parameterNums)
	{
		this.parameterNums = parameterNums;
		parameters = new ArrayList<ArrayList<Double>>(starNums);
		
		singleParameter = new ArrayList<Double>(parameterNums);
		for(int j=0;j<parameterNums;j++)
		{
			singleParameter.add(0.0);
		}	

		parameters.add(singleParameter);
		for(int i=1; i<starNums; i++)
		{
			parameters.add((ArrayList<Double>)singleParameter.clone());
		}

		oneStarSamples = new ArrayList<ArrayList<Double>>();
		twoStarSamples = new ArrayList<ArrayList<Double>>();
		threeStarSamples = new ArrayList<ArrayList<Double>>();
		fourStarSamples = new ArrayList<ArrayList<Double>>();
		fiveStarSamples = new ArrayList<ArrayList<Double>>();

	}

	public void initilize(String file) throws Exception
	{
		samples = new HashMap<Integer,ArrayList<ArrayList<Double>>>();

		BufferedReader br = new BufferedReader(new FileReader(file));
		String content;
		int lineNums=0;

		while(null != (content = br.readLine()))
		{
			String[] firstSplit = content.split(":");
			int star = Integer.valueOf(firstSplit[0].trim());
			
			String[] secondSplit = firstSplit[1].trim().split(" ");
			// System.out.println(secondSplit.length);
			ArrayList<Double> singleSample = (ArrayList<Double>)singleParameter.clone();

			for(int i =0; i< secondSplit.length; i++)
			{
				singleSample.set(i,Double.valueOf(secondSplit[i]));
			}
			switch(star)
			{
				case 1:
					oneStarSamples.add(singleSample);
					break;
				case 2:
					twoStarSamples.add(singleSample);
					break;
				case 3:
					threeStarSamples.add(singleSample);
					break;
				case 4:
					fourStarSamples.add(singleSample);
					break;
				case 5:
					fiveStarSamples.add(singleSample);
					break;
			}
			// MessageFormat messsageFormat = new MessageFormat("process {0} lines!");
			System.out.print(MessageFormat.format("process {0} lines!\t\r",Integer.toString(++lineNums)));
		}
		br.close();
		sampleNums = lineNums;
		System.out.println();
	}

	public void training(double alpha,int iternums) throws Exception
	{
		for(int iter=0; iter<iternums; iter++)
		{
			System.out.println("iter " + iter + " times");
			traverseSamplesAndUpdateParameter(alpha,oneStarSamples,0);
			traverseSamplesAndUpdateParameter(alpha,twoStarSamples,1);
			traverseSamplesAndUpdateParameter(alpha,threeStarSamples,2);
			traverseSamplesAndUpdateParameter(alpha,fourStarSamples,3);
			traverseSamplesAndUpdateParameter(alpha,fiveStarSamples,4);
		}
	}

	private void traverseSamplesAndUpdateParameter(double alpha,ArrayList<ArrayList<Double>> starSamples,int starIndex)
	{
		for(ArrayList<Double> starSample : starSamples)
		{
			ArrayList<Double> expResult = getInner(starSample);
			double sum = expResult.stream().mapToDouble(o1 -> o1).sum();
			for(int j=0; j<starNums; j++)
			{
				double p = expResult.get(j)/sum;
				if(j == starIndex)
				{	
					for(int n=0; n<parameterNums; n++)
					{
						double para_old = parameters.get(j).get(n);
						double para_new = para_old + alpha * starSample.get(n) * (1 - p);
						parameters.get(j).set(n,para_new);
					}
				}
				else
				{
					for(int n=0; n<parameterNums; n++)
					{
						double para_old = parameters.get(j).get(n);
						double para_new = para_old + alpha * starSample.get(n) * (-1) * p;
						parameters.get(j).set(n,para_new);
					}						
				}
			}
		}
	}

	private double computeInner(ArrayList<Double> thetaVector,ArrayList<Double> singleSamples) 
	{
		if(thetaVector.size()!= singleSamples.size())
		{
			System.out.println("the length is wrong in computeInner");
			return -1;
		}		
		
		double sum = 0;

		for(int i=0; i<thetaVector.size(); i++)
		{
			sum += thetaVector.get(i) * singleSamples.get(i);
		}
		return sum;
	}

	public double computeProbabilityOfGivenXAndEqualsJ(int j,ArrayList<Double> singleSamples) 
	{
		double pj = 0.0;
		double sum = 0.0;
		for(int i=0; i<parameters.size(); i++)
		{
			double temp_1 = computeInner(parameters.get(i),singleSamples);
			double temp_2 = Math.exp(temp_1);
			if(i==j-1)
				pj = temp_2;
			sum += temp_2;
		}
		return pj/sum;
	}

	private ArrayList<Double> getInner(ArrayList<Double> singleSamples) 
	{
		ArrayList<Double> result = new ArrayList<Double>(starNums);
		parameters
		.stream()
		.map(o1 -> {return Math.exp(computeInner(o1,singleSamples));})
		.forEach(o1 -> {result.add(o1);});

		return result;
	}

	private double computeGradient4lineJ(int y,int j)
	{
		return 0;
	}

	public static void main(String[] args) throws Exception
	{
		SoftmaxRegression softmaxRegression = new SoftmaxRegression(5,500);
		softmaxRegression.initilize("/home/lee/material/corpus/jd/4train.txt");
	}

}