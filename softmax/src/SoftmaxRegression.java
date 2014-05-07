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
	public int classNums; //参数的维度
	
	public int parameterNums;
	public int sampleNums = 0;

	ArrayList<ArrayList<Double>> oneStarSamples;
	ArrayList<ArrayList<Double>> twoStarSamples;
	ArrayList<ArrayList<Double>> threeStarSamples;
	ArrayList<ArrayList<Double>> fourStarSamples;
	ArrayList<ArrayList<Double>> fiveStarSamples;

	public SoftmaxRegression(int starNums,int classNums,int parameterNums)
	{
		this.classNums = classNums;
		this.parameterNums = parameterNums;
		parameters = new ArrayList<ArrayList<Double>>(starNums);
		ArrayList<Double> singleParameter = new ArrayList<Double>(classNums);
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
			ArrayList<Double> singleSamples = new ArrayList<Double>(classNums);
			String[] secondSplit = firstSplit[1].trim().split(" ");
			System.out.println(secondSplit.length);
			for(int i =0; i< secondSplit.length; i++)
			{
				singleSamples.set(i,Double.valueOf(secondSplit[i]));
			}
			switch(star)
			{
				case 1:
					oneStarSamples.add(singleSamples);
					break;
				case 2:
					twoStarSamples.add(singleSamples);
					break;
				case 3:
					threeStarSamples.add(singleSamples);
					break;
				case 4:
					fourStarSamples.add(singleSamples);
					break;
				case 5:
					fiveStarSamples.add(singleSamples);
					break;
			}
			MessageFormat messsageFormat = new MessageFormat("process {0} lines!");
			System.out.println(messsageFormat.format(++lineNums));
		}
		sampleNums = lineNums;
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
			for(int j=0; j<classNums; j++)
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
		ArrayList<Double> result = new ArrayList<Double>(classNums);
		parameters
		.stream()
		.map(o1 -> {return Math.exp(computeInner(o1,singleSamples));})
		.forEach(o1 -> {result.add(o1);});
		

		// List<Double> list = parameters
		// .stream()
		// .map(x -> {
		// 	try{
		// 		return Math.exp(computeInner(x,singleSamples));
		// 	}catch (Exception e){
				
		// 	}
		// });
		// list.stream().forEach(o1 -> {result.add(o1);});
		return result;
	}

	private double computeGradient4lineJ(int y,int j)
	{
		return 0;
	}

}