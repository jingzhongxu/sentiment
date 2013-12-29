import os
import os.path
import jieba
import pickle
import math
import codecs

def unPickleTrainingResult(fileRoot):
	with open(fileRoot,"rb") as frb:
		sentimentWord = pickle.load(frb)
		paraList = pickle.load(frb)
	return sentimentWord,paraList

def __readCorpus(root):
	for dirpath,dirlist,filelist in os.walk(root):
		if dirpath.endswith("neg"):
			negParaList = __work4ReadCorpus(dirpath,filelist)
		elif dirpath.endswith("pos"):
			posParaList = __work4ReadCorpus(dirpath,filelist)
	return posParaList,negParaList

def __work4ReadCorpus(dirpath,filelist):
	paragraphList=[]
	for filea in filelist:
		with codecs.open(os.path.join(dirpath,filea),"r",encoding="gbk") as fr:
			[paragraphList.append(line.strip()) for line in fr if not line.isspace() and line.find("content")==-1]
	return paragraphList

def __corpus2Features(sentimentWord,paragraphList):
	partSamplePoint=[]
	for paragraph in paragraphList:
		oneSamplePoint = [0 for i in range(len(sentimentWord))]
		[__assAsExpInWork4Corpus2Features(oneSamplePoint,sentimentWord.index(word)) for word in list(jieba.cut(paragraph)) if word in sentimentWord]
		partSamplePoint.append(oneSamplePoint)
	return partSamplePoint


def __assAsExpInWork4Corpus2Features(listName,index):
	listName[index] =1

def __preTestWork(testRoot,pickleroot):
	posParaList,negParaList = __readCorpus(testRoot)
	sentimentWord,paraList = unPickleTrainingResult(pickleroot)
	posSamplePointList = __corpus2Features(sentimentWord,posParaList)
	negSamplePointList = __corpus2Features(sentimentWord,negParaList)
	return sentimentWord,paraList,posSamplePointList,negSamplePointList
	

def __workForTest(paraList,singlePoint):
	temp = sum([para*theta for para,theta in zip(paraList,singlePoint)])
	return 1/(1 + math.exp((-1)*temp))
	

def test():
	sentimentWord,paraList,posSamplePointList,negSamplePointList = __preTestWork("./corpus/test","./trainResult/trainResult.fre4all")
	posResult = [1 if __workForTest(paraList,singlePoint)>0.5 else -1 if __workForTest(paraList,singlePoint)<0.5 else 0 for singlePoint in posSamplePointList]
	negResult = [1 if __workForTest(paraList,singlePoint)<0.5 else -1 if __workForTest(paraList,singlePoint)>0.5 else 0 for singlePoint in negSamplePointList]
	print(len(sentimentWord))
	print ((posResult.count(1) + negResult.count(1))/200)
	print (posResult.count(1),negResult.count(1))


def testSingle(paraList,sentimentWord,sentence):
	oneSamplePoint = [0 for i in range(len(sentimentWord))]
	[__assAsExpInWork4Corpus2Features(oneSamplePoint,sentimentWord.index(word)) for word in list(jieba.cut(sentence)) if word in sentimentWord]
	temp = sum([feature*parameter for feature,parameter in zip(oneSamplePoint,paraList)])
	return oneSamplePoint
	# return 1/(1 + math.exp((-1)*temp))

def testCoding():
	'''
	sentimentWord,paraList,posSamplePointList,negSamplePointList = __preTestWork("./corpus/test","./trainResult1.out")
	print("sentimentWord length: " , len(sentimentWord))
	print("posParagraphList length: " , len(posSamplePointList))
	print("negParagraphList length: " , len(negSamplePointList))
	print("paraList length: " , len(paraList))
	'''

	sentimentWord,paraList = unPickleTrainingResult("./trainResult/trainResultPresent.out")
	sentence = "在电视上看到于老师的讲课觉得不错买了这本书。但是我想说，这本书没什么收藏价值。能看电视还是看电视吧"
	oneSamplePoint = testSingle(paraList,sentimentWord,sentence)
	temp = sum([feature*parameter for feature,parameter in zip(oneSamplePoint,paraList)])
	print(temp)
	print(1/(1 + math.exp((-1)*temp)))
	hasWord = [word for word in list(jieba.cut(sentence)) if word in sentimentWord]
	print(hasWord)


def main():
	# testCoding()
	test()

if __name__=='__main__':
	main()