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
	}

	public void initilize(String file) throws Exception
	{
		samples = new HashMap<Integer,ArrayList<ArrayList<Double>>>();

		ArrayList<ArrayList<Double>> oneStarSamples = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> twoStarSamples = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> threeStarSamples = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> fourStarSamples = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> fiveStarSamples = new ArrayList<ArrayList<Double>>();

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

	public void training(double alpha,int iternums)
	{
		for(int i=0; i<iternums;i++)
		{

		}
	}

	private double computeInner(ArrayList<Double> thetaVector,ArrayList<Double> singleSamples)
	{
		if(thetaVector.size()!= singleSamples.size())
			throw new Exception("the length is wrong in computeInner");
		
		for(int i=0; i<thetaVector.size(); i++)
		{
			
		}
	}

	private double computeGradient()
	{
		
	}

}