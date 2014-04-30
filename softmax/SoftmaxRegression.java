import java.util.ArrayList;
import java.io.File;
import java.util.HashMap;

public SoftmaxRegression
{
	public HashMap<ArrayList<Double>,Integer> samples;
	public ArrayList<ArrayList<Double>> parameters;
	public int classNums;
	public int parameterNums;
	public int sampleNums;

	public SoftmaxRegression(int classNums,int parameters)
	{
		this.classNums = classNums;
		this.parameters = parameters;
	}

	public void getSamples(int sampleNums,File file)
	{
		this.sampleNums = sampleNums;
		samples = new HashMap<ArrayList<Double>,Integer>(sampleNums); 
				


	}






}