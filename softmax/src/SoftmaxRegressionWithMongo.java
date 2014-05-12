import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.ServerAddress;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.io.BufferedWriter;
import java.io.FileWriter;


public class SoftmaxRegressionWithMongo
{
	public int parameterNums;
	public ArrayList<ArrayList<Double>> parameters;
	public int starNums = 5;
	public MongoClient mongoClient;
	public DB db;
	public DBCollection collection1;
	public DBCollection collection2;
	public DBCollection collection3;
	public DBCollection collection4;
	public DBCollection collection5;


	public SoftmaxRegressionWithMongo(int parameterNums) throws Exception
	{
		this.parameterNums = parameterNums;
		ArrayList<Double> singlePara = new ArrayList<Double>(Collections.nCopies(parameterNums,0.0));
		parameters = new ArrayList<ArrayList<Double>>(starNums);
		parameters.add(singlePara);
		for(int i=1;i<starNums;i++)
		{
			parameters.add((ArrayList<Double>)singlePara.clone());
		}

		//初始化数据库相关
		mongoClient = new MongoClient("localhost",44444);
		db = mongoClient.getDB("trainData");
		collection1 = db.getCollection("collection1");
		collection2 = db.getCollection("collection2");
		collection3 = db.getCollection("collection3");
		collection4 = db.getCollection("collection4");
		collection5 = db.getCollection("collection5"); 
	}

	//这个是从数据库中读取的数据转化为可以训练的sample
	private ArrayList<Double> work4GetSampleFromDB(DBObject dBObect)
	{
		Map map = dBObect.toMap();
		ArrayList<Double> list = new ArrayList<Double>(Collections.nCopies(parameterNums,0.0));
		map.forEach((key,value) -> {
			if(key.equals("_id"))
				return;
			else{
				double para = Double.valueOf((String)value);
				if(para == 0.0)
					return;
				int index = Integer.valueOf((String)key) -1;
				list.set(index,para);
			}
		});
		return list;
	}


	public void training(double alpha,int iternums) throws Exception
	{
		for(int iter=0; iter<iternums; iter++)
		{
			System.out.println("\n iter " + iter + " times");
			train4EachCollection(collection1,alpha,1);
			train4EachCollection(collection2,alpha,2);
			train4EachCollection(collection3,alpha,3);
			train4EachCollection(collection4,alpha,4);
			train4EachCollection(collection5,alpha,5);
		}
	}

	private void train4EachCollection(DBCollection dbCollection,double alpha,int label)
	{
		ArrayList<Double> singleSample;
		DBCursor cursor = dbCollection.find();
		int lineNum = 1;
		while(cursor.hasNext())
		{
			DBObject dBObect = cursor.next();
			singleSample = work4GetSampleFromDB(dBObect);
			
			ArrayList<Double> expResult = getEveryStarInner(singleSample);
			double sum = expResult.stream().mapToDouble(o1 -> o1).sum();

			for(int j =0; j<starNums; j++)
			{
				double p = expResult.get(j)/sum;

				if(j==label-1)
				{
					for(int n=0; n<parameterNums; n++)
					{
						if(singleSample.get(n) == 0)
							continue;

						double para_old = parameters.get(j).get(n);
						double para_new = para_old + alpha * singleSample.get(n) * (1 - p);
						parameters.get(j).set(n,para_new);
					}
				}
				else
				{
					for(int n=0; n<parameterNums; n++)
					{
						if(singleSample.get(n) == 0)
							continue;

						double para_old = parameters.get(j).get(n);
						double para_new = para_old + alpha * singleSample.get(n) * (-1) * p;
						parameters.get(j).set(n,para_new);
					}					
				}

			}
			System.out.print(MessageFormat.format("process  collection{0} {1} lines!\t\r",label,Integer.toString(lineNum++)));
		}
		cursor.close();
	}

	private double computeInner(ArrayList<Double> thetaVector,ArrayList<Double> singleSample) 
	{
		if(thetaVector.size()!= singleSample.size())
		{
			System.out.println("the length is wrong in computeInner");
			return -1;
		}		
		
		double sum = 0;

		for(int i=0; i<thetaVector.size(); i++)
		{
			if(singleSample.get(i)!=0.0)
				sum += thetaVector.get(i) * singleSample.get(i);
		}
		return sum;
	}

	public double computeProbabilityOfGivenXAndEqualsJ(int j,ArrayList<Double> singleSample) 
	{
		double pj = 0.0;
		double sum = 0.0;
		for(int i=0; i<parameters.size(); i++)
		{
			double temp_1 = computeInner(parameters.get(i),singleSample);
			double temp_2 = Math.exp(temp_1);
			if(i==j-1)
				pj = temp_2;
			sum += temp_2;
		}
		return pj/sum;
	}

	private ArrayList<Double> getEveryStarInner(ArrayList<Double> singleSample) 
	{
		ArrayList<Double> result = new ArrayList<Double>(starNums);
		parameters
		.stream()
		.map(o1 -> {return Math.exp(computeInner(o1,singleSample));})
		.forEach(o1 -> {result.add(o1);});

		return result;
	}

	public void outputTrainResult(String filename) throws Exception
	{
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
		for(int j=0;j<parameters.size();j++)
		{
			for(int t=0;t<parameters.get(j).size();t++)
			{
				bw.write(parameters.get(j).get(t) + " ");
			}
			bw.write("\n");
		}
		bw.close();
	}

	public static void main(String[] args) throws Exception
	{
		SoftmaxRegressionWithMongo obj = new SoftmaxRegressionWithMongo(500);
		obj.training(0.00001,1);
		obj.outputTrainResult("./result/trainResult_500.out");
	}
}