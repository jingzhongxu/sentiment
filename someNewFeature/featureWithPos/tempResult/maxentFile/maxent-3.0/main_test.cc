#include <string>
#include <list>
#include <cstdio>
#include <sstream>
#include <iostream>
#include <fstream>
#include "maxent.h"

/*
读取测试文件并输出结果，结果格式为 "模型结果(1/0) 实际结果 分类结果“
*/
int main(int argc,char** argv)
{
	if(argc!=4)
	{
		fprintf(stderr,"useage [testFile] [modelFile] [resultFile]\n");
		return -1;
	}

	std::ifstream ifs;
	ifs.open(argv[1]);
	if(!ifs.is_open())
	{
		fprintf(stderr,"open testFile failed!!!\n");
		return -1;
	}

	std::ofstream ofs;
	ofs.open(argv[3]);
	if(!ofs.is_open())
	{
		fprintf(stderr,"open result file failed!\n");
		return -1;
	}




	ME_Model model;
	model.load_from_file(argv[2]);


	std::string line,token,flag;

	while(!ifs.eof() && !ifs.fail())
	{
		getline(ifs,line);
		std::stringstream ss(line);
		ME_Sample s;
		int i=0;
		while(ss>>token)
		{
			if(i==0)
			{
				flag = token;
				// std::cout<<flag<<std::endl;
				i++;
			}
			else
			{
				s.add_feature(token);
				i++;
			}
		}

		model.classify(s);
		if(s.label==flag)
		{
			ofs<<"1 "<<flag<<" "<<s.label<<"\n";
		}
		else
		{
			ofs<<"0 "<<flag<<" "<<s.label<<"\n";
		}	

	}


	ifs.close();
	ofs.close();
	return 0;
}