import codecs
import os
import os.path
import jieba
import math
import pickle

class Construct:
	def __init__(self,pathStr):
		for dirpath,dirlis,filelist in os.walk(pathStr):
			if dirpath.endswith('neg'):
				self.posParaList = self.__work4ReadCorpus(dirpath,filelist)
			elif dirpath.endswith('pos'):
				self.negParaList = self.__work4ReadCorpus(dirpath,filelist)
	
	def __work4ReadCorpus(dirpath,filelist):
		paragraphList=[]
		for filea in filelist:
			with codecs.open(os.path.join(dirpath,filea),"r",encoding="gbk") as fr:
				[paragraphList.append(line.strip()) for line in fr if not line.isspace() and line.find("content")==-1]
		return paragraphList			

	