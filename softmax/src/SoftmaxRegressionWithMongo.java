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


	public SoftmaxRegressionWithMongo(int parameterNums,String database) throws Exception
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
		// db = mongoClient.getDB("trainData");
		db = mongoClient.getDB(database);
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
			System.out.print(" process " + dbCollection.getName() + MessageFormat.format(" {1} lines!\t\r",Integer.toString(lineNum++)));

			if(lineNum>50000)
				break;

		}
		System.out.println();
		cursor.close();
	}

	public void trainRandom(double alpha,int iternums) throws Exception
	{
		for(int iter=0; iter<iternums; iter++)
		{
			System.out.println("\n iter " + iter + " times");			
			
			DBCursor cursor_1 = collection1.find();
			DBCursor cursor_2 = collection2.find();
			DBCursor cursor_3 = collection3.find();
			DBCursor cursor_4 = collection4.find();
			DBCursor cursor_5 = collection5.find();

			int allNum = cursor_1.count() + cursor_2.count() + cursor_3.count() + cursor_4.count() + cursor_5.count();
			int lineNum = 0;

			while(lineNum < allNum)
			{
				int randomCollectionIndex = (int)(Math.random() * 5) + 1;
				DBObject dBObject;
				switch(randomCollectionIndex)
				{
					case 1:
						if(cursor_1.hasNext())
						{
							dBObject = cursor_1.next();
							work4trainRandom(dBObject,1,alpha);
							System.out.print(MessageFormat.format("process  collection{0} all {1} lines\t\r",randomCollectionIndex,++lineNum));
						}
						break;
					case 2:
						if(cursor_2.hasNext())
						{
							dBObject = cursor_2.next();
							work4trainRandom(dBObject,2,alpha);
							System.out.print(MessageFormat.format("process  collection{0} all {1} lines\t\r",randomCollectionIndex,++lineNum));							
						}
						break;
					case 3:
						if(cursor_3.hasNext())
						{
							dBObject = cursor_3.next();
							work4trainRandom(dBObject,3,alpha);
							System.out.print(MessageFormat.format("process  collection{0} all {1} lines\t\r",randomCollectionIndex,++lineNum));	
						}				
						break;
					case 4:
						if(cursor_4.hasNext())
						{
							dBObject = cursor_4.next();
							work4trainRandom(dBObject,4,alpha);
							System.out.print(MessageFormat.format("process  collection{0} all {1} lines\t\r",randomCollectionIndex,++lineNum));
						}
						break;
					case 5:
						if(cursor_5.hasNext())
						{					
							dBObject = cursor_5.next();
							work4trainRandom(dBObject,5,alpha);
							System.out.print(MessageFormat.format("process  collection{0} all {1} lines\t\r",randomCollectionIndex,++lineNum));
						}
						break;
				}//switch
			}//while
			System.out.println();
		}//for

		System.out.println();
	}

	private void work4trainRandom(DBObject dBObject , int lable, double alpha)
	{
		ArrayList<Double> singleSample = work4GetSampleFromDB(dBObject);
		ArrayList<Double> expResult = getEveryStarInner(singleSample);
		double sum = expResult.stream().mapToDouble(o1 -> o1).sum();

		for(int j =0; j<starNums; j++)
		{
			double p = expResult.get(j)/sum;

			if(j==lable-1)
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

		// SoftmaxRegressionWithMongo obj = new SoftmaxRegressionWithMongo(500,"trainData");
		// obj.training(0.00001,1);
		// obj.outputTrainResult("./result/trainResult_500.out");


		SoftmaxRegressionWithMongo obj = new SoftmaxRegressionWithMongo(500,"trainData2");
		obj.trainRandom(0.0001,50);

		obj.outputTrainResult("./result/trainResult_500.result");
	}
}