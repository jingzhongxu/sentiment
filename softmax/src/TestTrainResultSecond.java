//这是我要统计准确率，之前的我只统计了召回率

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

/*
这个版本我改动的有点大，
1.首先是增加了准确率的计算，之前只计算召回率了
2.在计算准确率的过程中，发现测试样本的个数应该是一样的，所有又设置了测试样本的个数上限是500,即都比500小
3.TestTrainResult中我计算召回率的时候，是所有的测试样本(没有因为平衡的个数限制)都是直接初始化数据库大小的，不适合现在的这个类
所以results的声明定义初始化需要更改，之前是在processResult中定义及初始化工作，同时借助initSingleResult函数，现在在当前类中的
构造函数完成定义及初始化工作
4.同时之前TestTrainResult类中，我在统计results的时候，是用的set方法(因为我不用管这个list的大小，它已经初始化好了)，当前类中我要改成
像accruacys一样的更新方法。
*/
public class TestTrainResultSecond extends TestTrainResult
{

	public ArrayList<ArrayList<Boolean>> accuracys;

	public TestTrainResultSecond(int parameterNums,String paraFile) throws Exception
	{
		super(parameterNums,paraFile);
		accuracys = new ArrayList<ArrayList<Boolean>>(this.starNums);
		for(int i=0; i<this.starNums; i++)
		{
			ArrayList<Boolean> accuracy = new ArrayList<Boolean>(500);
			accuracys.add(accuracy);
		}
		results = new ArrayList<ArrayList<Boolean>>(starNums);
		for(int i=0; i< starNums; i++)
		{
			ArrayList<Boolean> result = new ArrayList<Boolean>(500);
			results.add(result);
		}
	}

	public void processResult()
	{
		// results = new ArrayList<ArrayList<Boolean>>(starNums);
		// results.add(initSingleResult(collection1));
		// results.add(initSingleResult(collection2));
		// results.add(initSingleResult(collection3));
		// results.add(initSingleResult(collection4));
		// results.add(initSingleResult(collection5));

		processEachCollection(collection1,1,results.get(0));
		processEachCollection(collection2,2,results.get(1));
		processEachCollection(collection3,3,results.get(2));
		processEachCollection(collection4,4,results.get(3));
		processEachCollection(collection5,5,results.get(4));

	}
	private void processEachCollection(DBCollection colleciton,int label,ArrayList<Boolean> result)
	{
		DBCursor cursor = colleciton.find();
		int cursor_num = 0;
		while(cursor.hasNext())
		{
			if(cursor_num > 500)
				break;

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
				// result.set(cursor_num,true);
				result.add(true);
				accuracys.get(max_index).add(true);
			}
			else
			{
				result.add(false);
				accuracys.get(max_index).add(false);
			}

			System.out.print(MessageFormat.format("process  collection{0} {1} lines!\t\r",label,Integer.toString(cursor_num++)));
		}
		System.out.println();
		cursor.close();
	}

	public void processMoreLoose()
	{

		// results = new ArrayList<ArrayList<Boolean>>(starNums);
		// results.add(initSingleResult(collection1));
		// results.add(initSingleResult(collection2));
		// results.add(initSingleResult(collection3));
		// results.add(initSingleResult(collection4));
		// results.add(initSingleResult(collection5));
		processEachCollectionMoreLoose(collection1,1,results.get(0));
		processEachCollectionMoreLoose(collection2,2,results.get(1));
		processEachCollectionMoreLoose(collection3,3,results.get(2));
		processEachCollectionMoreLoose(collection4,4,results.get(3));
		processEachCollectionMoreLoose(collection5,5,results.get(4));

	}
	private void processEachCollectionMoreLoose(DBCollection collection,int label,ArrayList<Boolean> result)
	{
		DBCursor cursor  = collection.find();
		int cursor_num = 0;

		while(cursor.hasNext())
		{
			
			if(cursor_num > 500)
				break;

			DBObject obj = cursor.next();
			ArrayList<Double> singleSample = work4GetSampleFromDB(obj);
			ArrayList<Double> inners = getEveryStarInner(singleSample);	
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
					{
						// result.set(cursor_num,true);
						result.add(true);
						accuracys.get(result_label-1).add(true);
					}
					else
					{
						// result.set(cursor_num,false);
						result.add(false);
						accuracys.get(result_label-1).add(false);
					}
					break;
				case 2:
					if(result_label==1 || result_label==2)
					{
						// result.set(cursor_num,true);
						result.add(true);
						accuracys.get(result_label-1).add(true);					
					}
					else
					{
						// result.set(cursor_num,false);
						result.add(false);						
						accuracys.get(result_label-1).add(false);
					}
					break;
				case 3:
					if(result_label == 3)
					{
						// result.set(cursor_num,true);
						result.add(true);
						accuracys.get(result_label-1).add(true);		
					}
					else
					{
						// result.set(cursor_num,false);
						result.add(false);
						accuracys.get(result_label-1).add(false);
					}
					break;
				case 4:
					if(result_label==4 || result_label==5)
					{
						// result.set(cursor_num,true);
						result.add(true);
						accuracys.get(result_label-1).add(true);						
					}
					else
					{
						// result.set(cursor_num,false);
						result.add(false);
						accuracys.get(result_label-1).add(false);
					}
					break;
				case 5:
					if(result_label==4 || result_label==5)
					{
						// result.set(cursor_num,true);
						result.add(true);
						accuracys.get(result_label-1).add(true);								
					}
					else
					{
						// result.set(cursor_num,false);
						result.add(false);
						accuracys.get(result_label-1).add(false);
					}
					break;
			}
			System.out.print(MessageFormat.format("process  collection{0} {1} lines!\t\r",label,Integer.toString(cursor_num++)));
		}
		System.out.println();
	}

	public void outputResult()
	{
		System.out.println("this is output recall:");
		for(int j=0; j<results.size(); j++)
		{
			int nums =(int) (results.get(j).stream()
			.filter(o1 -> o1==true)
			.count());
			System.out.println(nums + "  " + results.get(j).size() + "    " + ((float)nums/results.get(j).size()));			
		}

		System.out.println("--------------------------------------------\nnow is output accuracy:");
		// System.out.println(accuracys.size());
		// System.out.println(accuracys.get(0).size());
		for(int i=0; i< accuracys.size(); i++)
		{
			int nums = (int) (accuracys.get(i).stream().
				filter(o1 -> o1==true)).count();
			System.out.println(nums + "   " + accuracys.get(i).size() + "   " + ((float)nums/accuracys.get(i).size()));
		}
	}


	public static void main(String[] args) throws Exception
	{
		TestTrainResultSecond obj = new TestTrainResultSecond(500,"./result/trainResult_500.result");
		obj.processResult();
		

		// obj.processMoreLoose();
		

		obj.outputResult();

	}

}