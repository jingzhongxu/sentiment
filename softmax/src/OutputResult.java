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

public class OutputResult
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

	public OutputResult(String file,int parameterNums) throws Exception
	{
		this.parameters = new ArrayList<ArrayList<Double>>();
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
		BufferedReader br = new BufferedReader(new FileReader(file));
		String content;
		while(null != (content = br.readLine()))
		{
			ArrayList<Double> temp = Collections.ncopies(parameterNums,0.0);
			String[] tokens = content.split(" ");
			for(int i = 0; i<tokens.length; i++)
			{
				temp.set(i,Double.valueOf(tokens[i]));
			}
			parameters.add(temp);
		} 
		br.close();
	}

	public void getResult()
	{

	}

	private void trainEach(DBCollection colletion,int label)
	{
		Cursor cursor = collection.find();
		while(cursor.hasNext())
		{
			DBObject dBObject = cursor.next();
			ArrayList<Double> singleSample = work4GetSampleFromDB(dBObject);
			ArrayList<Double> result = getEveryStarInner(singleSample);
			
		}
	}

	private ArrayList<Double> work4GetSampleFromDB(DBObject dBObject)
	{
		Map map = dBObject.toMap();
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


	public static void main(String[] args) throws Exceptions
	{
		OutputResult obj = new OutputResult("./result/trainResult_500.out",500);

	}
}