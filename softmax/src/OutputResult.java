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
import java.io.FileWriter;
import java.util.Map;
import java.text.MessageFormat;
import java.util.Arrays;

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
			ArrayList<Double> temp = new ArrayList<Double>(Collections.nCopies(parameterNums,0.0));
			String[] tokens = content.split(" ");
			for(int i = 0; i<tokens.length; i++)
			{
				temp.set(i,Double.valueOf(tokens[i]));
			}
			parameters.add(temp);
		} 
		br.close();
	}

	public void getResult(String outputResultFile) throws Exception
	{
		FileWriter fw = new FileWriter(outputResultFile);
		processEachCol(collection1,1,fw);
		processEachCol(collection1,2,fw);
		processEachCol(collection1,3,fw);
		processEachCol(collection1,4,fw);
		processEachCol(collection1,5,fw);		
		fw.close();
	}

	private void processEachCol(DBCollection collection,int label,FileWriter fw) throws Exception
	{
		fw.write(collection.getName() + ": \n");
		DBCursor cursor = collection.find();
		while(cursor.hasNext())
		{
			DBObject dBObject = cursor.next();
			ArrayList<Double> singleSample = work4GetSampleFromDB(dBObject);
			ArrayList<Double> result = getEveryStarInner(singleSample);
			ArrayList<Integer> indexSort = new ArrayList<Integer>(Arrays.asList(0,1,2,3,4));
			indexSort.sort((o1,o2) -> {if(result.get(o1) > result.get(o2))
											return -1;
									   else
									   		return 1;
			 							});

			int result_one = indexSort.get(0) + 1;//这里直接转化为我的label,所以需要+1
			int result_two = indexSort.get(1) + 1; 
			fw.write("accuracy:" + label + "  result_one:" + result_one + " value=" + result.get(indexSort.get(0)) + "   result_two:" + result_two + " value=" + result.get(indexSort.get(1)) + "\n");
		}

		fw.write("\n\n");
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


	public static void main(String[] args) throws Exception
	{
		OutputResult obj = new OutputResult("./result/trainResult_500.out",500);
		obj.getResult("./result/top2.out");

	}
}