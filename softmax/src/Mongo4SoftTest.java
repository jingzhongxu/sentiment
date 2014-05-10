// 这是我用来测试用java处理softmax的训练数据的类
/*
1.Arrays.toArray(arrayObject)这个里面的arrayObject还是不能是原生数据类型
*/
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.ServerAddress;


import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

public class Mongo4SoftTest
{

	public static ArrayList<Double> getDataFromMongo(DBObject DBObect)
	{
		Map map = DBObect.toMap();
		int parameterNums = map.size()-1;//减去id这个键值

		// List<Double> list = new ArrayList<Double>(parameterNums);
		// 这里错的，toArray还是要是原生数据类型
		// double[] array = new double[parameterNums];
		// list = Arrays.asList(array);

		ArrayList<Double> list = new ArrayList<Double>(Collections.nCopies(parameterNums,0.0));

		map.forEach((key,value) -> {
			if(key.equals("_id"))
				return;
			else{
				int index = Integer.valueOf((String)key) -1;
				double para = Double.valueOf((String)value);
				list.set(index,para);
			}
		});

		return list;
	}





	public static void main(String[] args) throws Exception
	{
		MongoClient client = new MongoClient("localhost",44444);
		DB db = client.getDB("testData");
		DBCollection collection1 = db.getCollection("collection1");
		DBCollection collection2 = db.getCollection("collection2");
		DBCollection collection3 = db.getCollection("collection3");
		DBCollection collection4 = db.getCollection("collection4");
		DBCollection collection5 = db.getCollection("collection5");

		DBObject dbObject1 = collection1.findOne();
		Map map1 = dbObject1.toMap();

		Set<String> keys = map1.keySet();
		System.out.println(keys.size());
		keys.stream().forEach(o1 -> System.out.print(o1 + " "));
		System.out.println("\n-------------------------------------------------");

		ArrayList<Double> test = getDataFromMongo(dbObject1);
		test.stream().forEach(o1 -> System.out.print(o1 + " "));
		System.out.println();

	}


}