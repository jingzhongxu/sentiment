import jieba
import os
import os.path
import codecs
import pickle

class ProcessCorpus:

	def __init__(self,corpusRoot,sentimentWordRoot):
		self.corpusRoot = corpusRoot
		self.sentimentWordRoot = sentimentWordRoot

	def getSentimentWord(self):
		self.sentimentword=[]
		with codecs.open(self.sentimentWordRoot,"r",encoding="utf-8") as fr:
			[self.sentimentword.append(word.strip()) for word in fr if not word.isspace()]

	def __work4GetPosNeg(self,dirpath,filename,posParagraphList,negParagraphList):
		with codecs.open(os.path.join(dirpath,filename),"r",encoding="gbk") as fr:
			if dirpath.endswith("pos"): 
				[posParagraphList.append(paragraph.strip()) for paragraph in fr if not paragraph.isspace() and paragraph.find("content")==-1]
			elif dirpath.endswith("neg"):
				[negParagraphList.append(paragraph.strip()) for paragraph in fr if not paragraph.isspace() and paragraph.find("content")==-1]

	def __work4GetPosNegBuild(self,listname,index):
		listname[index] = 1

	def __getPosNegBuild(self,posParagraphList,negParagraphList):
		self.posSamplePointList=[]
		self.negSamplePointList=[]
		for paragraph in posParagraphList:
			oneSamplePoint=[0 for i in range(len(self.sentimentword))]
			[self.__work4GetPosNegBuild(oneSamplePoint,self.sentimentword.index(word)) for word in jieba.cut(paragraph) if word in self.sentimentword]
			self.posSamplePointList.append(oneSamplePoint)
		for paragraph in negParagraphList:
			oneSamplePoint=[0 for i in range(len(self.sentimentword))]
			[self.__work4GetPosNegBuild(oneSamplePoint,self.sentimentword.index(word)) for word in jieba.cut(paragraph) if word in self.sentimentword]
			self.negSamplePointList.append(oneSamplePoint)

	def getPosNegAttributes(self):
		if not hasattr(self,"sentimentword") : self.getSentimentWord()	
		negParagraphList=[]
		posParagraphList=[]
		for dirpath,dirlist,filelist in os.walk(self.corpusRoot):
			[self.__work4GetPosNeg(dirpath,fileA,posParagraphList,negParagraphList) for fileA in filelist]
		
		self.__getPosNegBuild(posParagraphList,negParagraphList)


		'''test'''
		# print(len(self.posSamplePointList))
		# print(len(self.negSamplePointList))
		# [print(self.sentimentword[index]) for index in range(len(self.sentimentword)) if self.negSamplePointList[0][index]!=0]
		# print("---"*10)
		# [print(self.sentimentword[index]) for index in range(len(self.sentimentword)) if self.posSamplePointList[0][index]!=0]

	def classtest(self):
		self.getPosNegAttributes()

def main():
	obj = ProcessCorpus("./corpus/test","./corpus/fre4train.txt")
	obj.classtest()
	with open("./pickle/fre4train.test","wb") as fw:
		pickle.dump(obj,fw)

	# with open("./pickle/fre4train.pickle","rb") as fr:
	# 	obj = pickle.load(fr)
	# print('obj.posParagraphList length: ',len(obj.posSamplePointList))
	# print('obj.sentimentword length: ',len(obj.sentimentword))
	# print('obj.negParagraphList length: ',len(obj.negSamplePointList))

if __name__=='__main__':
	main()