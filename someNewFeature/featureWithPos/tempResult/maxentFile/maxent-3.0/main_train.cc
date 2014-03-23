#include <string>
#include <list>
#include <cstdio>
#include <sstream>
#include <iostream>
#include <fstream>
#include "maxent.h"


int main(int argc,char** argv)
{
	if(argc!=3)
	{
		fprintf(stderr,"useage mc_main [input file] [outputfile]\n");
		return-1;
	}

	std::ifstream ifs;
	ifs.open(argv[1]);
	if(!ifs.is_open())
	{
		fprintf(stderr,"read file mistake!!\n");
		return -1;
	}

	std::string line,token;
	ME_Model model;

	while(!ifs.eof() && !ifs.fail())
	{
		getline(ifs,line);
		std::stringstream ss(line);
		int i=0;
		ME_Sample s;
		while(ss>>token)
		{
			if(i==0)
			{
				s.set_label(token);
				// std::cout<<token<<std::endl;
				i++;
			}
			else
			{
				s.add_feature(token);
				i++;
			}
		}
		model.add_training_sample(s);
	}

	model.train();

	model.save_to_file(argv[2]);
	return 0;
}

