import java.io.File;

public class ProcessFolder
{
	public File posFolder;
	public File negFolder;

	public ProcessFolder(String posFolder,String negFolder)
	{
		this.posFolder = new File(posFolder);
		this.negFolder = new File(negFolder);
		if(!posFolder.exist() || !negFolder.exist())
		{
			// System.out.println("The folder or file is not exist!");
			throw new Exception("folder or file in not exist!");
		}
	}

    public void readFileInFolder()
    {
    	File[] files = posFolder.listFiles();
    	for(File file:files)
    	{
    		
    	}
    }


}