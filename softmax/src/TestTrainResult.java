import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.ServerAddress;

import java.util.ArrayList;
import java.util.Collections;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map;
import java.text.MessageFormat;

public class TestTrainResult
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
	public ArrayList<ArrayList<Boolean>> results;

	public TestTrainResult(int parameterNums,String paraFile) throws Exception
	{
		this.parameterNums = parameterNums;

		//初始化mongo
		mongoClient = new MongoClient("localhost",44444);
		db = mongoClient.getDB("testData");
		collection1 = db.getCollection("collection1");
		collection2 = db.getCollection("collection2");
		collection3 = db.getCollection("collection3");
		collection4 = db.getCollection("collection4");
		collection5 = db.getCollection("collection5"); 

		//初始化parameters
		parameters = new ArrayList<ArrayList<Double>>(starNums);
		BufferedReader br = new BufferedReader(new FileReader(paraFile));
		String content;
		int lineNums = 0;
		while(null != (content=br.readLine()))
		{
			ArrayList<Double> single = new ArrayList<Double>(Collections.nCopies(parameterNums,0.0));
			String[] tokens = content.split(" ");
			// System.out.println(tokens.length);
			for(int j=0; j<tokens.length; j++)
			{
				single.set(j,Double.valueOf(tokens[j]));
			}
			parameters.add(single);
			lineNums++;
		}
		br.close();
	}

	public void processResult()
	{
		results = new ArrayList<ArrayList<Boolean>>(starNums);
		results.add(initSingleResult(collection1));
		results.add(initSingleResult(collection2));
		results.add(initSingleResult(collection3));
		results.add(initSingleResult(collection4));
		results.add(initSingleResult(collection5));

		processEachCollection(collection1,1,results.get(0));
		processEachCollection(collection2,2,results.get(1));
		processEachCollection(collection3,3,results.get(2));
		processEachCollection(collection4,4,results.get(3));
		processEachCollection(collection5,5,results.get(4));

	}

	private ArrayList<Boolean> initSingleResult(DBCollection colleciton)
	{
		return new ArrayList<Boolean>(Collections.nCopies((int)colleciton.count(),false));
	}

	private void processEachCollection(DBCollection colleciton,int label,ArrayList<Boolean> result)
	{
		DBCursor cursor = colleciton.find();
		int cursor_num = 0;
		while(cursor.hasNext())
		{
			DBObject dBObect = cursor.next();
			ArrayList<Double> singleSample = work4GetSampleFromDB(dBObect);
			ArrayList<Double> inners = getEveryStarInner(singleSample);
			double sum = inners.stream().mapToDouble(o1 -> o1).sum();
			
			ArrayList<Double> probobilitys = new ArrayList<Double>(starNums);
			double max = 0.0;
			int max_index = -1;
			for(int j=0; j<starNums;j++)
			{
				double value = inners.get(j)/sum;
				probobilitys.add(value);
				if(max < value)
				{
					max = value;
					max_index = j;
				}
			}

			if(max_index == label-1)
			{
				result.set(cursor_num,true);
			}

			System.out.print(MessageFormat.format("process  collection{0} {1} lines!\t\r",label,Integer.toString(cursor_num++)));
		}
		System.out.println();
		cursor.close();
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

	private ArrayList<Double> getEveryStarInner(ArrayList<Double> singleSample) 
	{
		ArrayList<Double> result = new ArrayList<Double>(starNums);
		parameters
		.stream()
		.map(o1 -> {return Math.exp(computeInner(o1,singleSample));})
		.forEach(o1 -> {result.add(o1);});

		return result;
	}

	private double computeInner(ArrayList<Double> thetaVector,ArrayList<Double> singleSample) 
	{	
		
		double sum = 0;

		for(int i=0; i<thetaVector.size(); i++)
		{
			if(singleSample.get(i)!=0.0)
				sum += thetaVector.get(i) * singleSample.get(i);
		}
		return sum;
	}



	public void outputResult()
	{
		for(int j=0; j<results.size(); j++)
		{
			int nums =(int) (results.get(j).stream()
			.filter(o1 -> o1==true)
			.count());
			System.out.println(nums + "  " + results.get(j).size() );			
		}
	}

	public static void main(String[] args) throws Exception
	{
		TestTrainResult obj = new TestTrainResult(500,"./result/trainResult_500.out");
		// System.out.println(obj.parameters.get(0).get(1));
		// System.out.println(obj.parameters.get(1).get(1));
		// System.out.println(obj.parameters.get(2).get(1));
		// System.out.println(obj.parameters.get(3).get(1));
		obj.processResult();
		obj.outputResult();
	}

}