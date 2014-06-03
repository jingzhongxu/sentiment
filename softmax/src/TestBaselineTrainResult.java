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
import java.util.Arrays;

public class TestBaselineTrainResult
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

	public TestBaselineTrainResult(int parameterNums,String paraFile) throws Exception
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

	public void processMoreLoose()
	{
		results = new ArrayList<ArrayList<Boolean>>(starNums);
		results.add(initSingleResult(collection1));
		results.add(initSingleResult(collection2));
		results.add(initSingleResult(collection3));
		results.add(initSingleResult(collection4));
		results.add(initSingleResult(collection5));

		processEachCollectionMoreLoose(collection1,1,results.get(0));
		processEachCollectionMoreLoose(collection2,2,results.get(1));
		processEachCollectionMoreLoose(collection3,3,results.get(2));
		processEachCollectionMoreLoose(collection4,4,results.get(3));
		processEachCollectionMoreLoose(collection5,5,results.get(4));

	}

	private ArrayList<Boolean> initSingleResult(DBCollection colleciton)
	{
		return new ArrayList<Boolean>(Collections.nCopies((int)colleciton.count(),false));
	}

	private void processEachCollectionMoreLoose(DBCollection collection,int label,ArrayList<Boolean> result)
	{
		DBCursor cursor = collection.find();
		int cursor_num = 0;
		while(cursor.hasNext())
		{
			DBObject obj = cursor.next();
			ArrayList<Double> samples = work4GetSampleFromDB(obj);
			ArrayList<Double> inners = getEveryStarInner(samples);
			ArrayList<Integer> indexSort = new ArrayList<Integer>(Arrays.asList(0,1,2,3,4));
			indexSort.sort((o1,o2) -> {if(inners.get(o1) > inners.get(o2))
											return -1;
									   else
									   		return 1;
			 							});

			int result_label = indexSort.get(0) + 1;//实际的label没有从0开始
			switch(label)
			{
				case 1:
					if(result_label==1 || result_label==2)
						result.set(cursor_num,true);
					else
						result.set(cursor_num,false);
					break;
				case 2:
					if(result_label==1 || result_label==2)
						result.set(cursor_num,true);
					else
						result.set(cursor_num,false);
					break;
				case 3:
					if(result_label == 3)
						result.set(cursor_num,true);
					else
						result.set(cursor_num,false);
					break;
				case 4:
					if(result_label==4 || result_label==5)
						result.set(cursor_num,true);
					else
						result.set(cursor_num,false);
					break;
				case 5:
					if(result_label==4 || result_label==5)
						result.set(cursor_num,true);
					else
						result.set(cursor_num,false);
					break;
			}			
			System.out.print(MessageFormat.format("process  collection{0} {1} lines!\t\r",label,Integer.toString(cursor_num++)));
		}
		System.out.println();
	}

	private ArrayList<Double> work4GetSampleFromDB(DBObject obj)
	{
		Map map = obj.toMap();
		ArrayList<Double> list = new ArrayList<Double>(Collections.nCopies(parameterNums,0.0));
		map.forEach((key,value) -> {
			if(((String)key).matches("\\d+"))
			{
				list.set(Integer.valueOf((String)key),((Integer)value).doubleValue());				
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
		TestBaselineTrainResult obj = new TestBaselineTrainResult(50829,"./result/trainResult_baseline.out");
		obj.processMoreLoose();
		obj.outputResult();
	}

}