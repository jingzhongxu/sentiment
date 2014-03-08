import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;
import java.util.List;


public class TestArrayListConstructor
{
	public static void outputArrayList(List list)
    {
    	Iterator iter = list.iterator();
    	for(;iter.hasNext();)
    	{
    		System.out.println(iter.next());
    	}
    }

    public static void outputArray(Integer[] array)
    {
    	for(int i=0;i<array.length;i++)
    	{
    		System.out.print(array[i]+"  ");
    	}
    	System.out.println();
    }

	public static void main(String[] args)
	{
		ArrayList list1 = new ArrayList();
		list1.add(1);
		list1.add(2);

		ArrayList list2 = new ArrayList(list1);
		
		System.out.println("list1");
		TestArrayListConstructor.outputArrayList(list1);
		System.out.println("list2");
		TestArrayListConstructor.outputArrayList(list2);
		System.out.println("test copy");

		list2.set(0,1222);
		System.out.println("list1 after test copy");
		TestArrayListConstructor.outputArrayList(list1);
		System.out.println("list2 after test copy");
		TestArrayListConstructor.outputArrayList(list2);
		System.out.println("----------------------------------------------");


		// int[] arrays = new int[4];
		Integer[] arrays = new Integer[4];
		System.out.println("arrays");
		TestArrayListConstructor.outputArray(arrays);
		System.out.println("arrays as List");
		TestArrayListConstructor.outputArrayList(Arrays.asList(arrays));


		ArrayList list3 = new ArrayList(Arrays.asList(arrays));
		ArrayList list4 = new ArrayList(Arrays.asList(arrays));
		System.out.println("list3");
		TestArrayListConstructor.outputArrayList(list3);
		System.out.println("list4");
		TestArrayListConstructor.outputArrayList(list4);

		list3.set(0,22222);
		list3.set(1,22);
		System.out.println("list3 after modify");
		TestArrayListConstructor.outputArrayList(list3);
		System.out.println("list4 after modify");
		TestArrayListConstructor.outputArrayList(list4);
	}
}