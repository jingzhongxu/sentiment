import codecs
import os
import os.path
import jieba
import math
import pickle
import random

def __readSentimentWord(root):
	with open(root,"r") as fr:
		sentimentWord = [line.strip() for line in fr if not line.isspace()]
	sentimentWord = list(set(sentimentWord))
	return sentimentWord

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

def __assAsExpInWork4Corpus2Features(listName,index):
	listName[index] =1


def __work4Corpus2Features(sentimentWord,paragraphList):
	partSamplePointList=[]
	for sentence in paragraphList:
		oneSamplePointList=[0 for i in range(len(sentimentWord))]
		[__assAsExpInWork4Corpus2Features(oneSamplePointList,sentimentWord.index(word)) for word in list(jieba.cut(sentence)) if word in sentimentWord]
		partSamplePointList.append(oneSamplePointList)
	return partSamplePointList

def __corpus2Features(sentimentWord,posParaList,negParaList):
	posSamplePointList = __work4Corpus2Features(sentimentWord,posParaList)
	negSamplePointList = __work4Corpus2Features(sentimentWord,negParaList)
	samplePointList = posSamplePointList + negSamplePointList
	return posSamplePointList,negSamplePointList

def __initParameter(paraNums):
	return [random.random() for i in range(paraNums)]

def initAndPre(sentimentWordFileRoot,trainRoot):
	sentimentWord = __readSentimentWord(sentimentWordFileRoot)
	posParaList,negParaList = __readCorpus(trainRoot)
	posSamplePointList,negSamplePointList = __corpus2Features(sentimentWord,posParaList,negParaList)
	samplePointList = posSamplePointList + negSamplePointList
	paraList = __initParameter(len(sentimentWord))


def trainingModel(iterNums,alpha,posSamplePointList,negSamplePointList,paraList):
	for i in range(iterNums):
		[__updatePara(paraList,featureVector,1.0,alpha) for featureVector in posSamplePointList]
		[__updatePara(paraList,featureVector,0.0,alpha) for featureVector in negSamplePointList]
		print("迭代第 ",i+1,"次")


def __computeHfun(thetaVector,featureVector):
	temp=sum([theta*feature for theta,feature in zip(thetaVector,featureVector)])
	return 1/(1 + math.exp((-1)*temp))

def __updatePara(thetaVector,featureVector,y,alpha):
	Hfun = __computeHfun(thetaVector,featureVector)
	for index,theta in enumerate(thetaVector):
		theta = theta + alpha * (y - Hfun) * featureVector[index]
		thetaVector[index] = theta

def pickleTrainingResult(fileRoot,sentimentWord,paraList):
	with open(fileRoot,"wb") as fwb:
		pickle.dump(sentimentWord,fwb)
		pickle.dump(paraList,fwb)

def unPickleTrainingResult(fileRoot):
	with open(fileRoot,"rb") as frb:
		sentimentWord = pickle.load(frb)
		paraList = pickle.load(frb)
	return sentimentWord,paraList

def testCoding():
	sentimentWord = __readSentimentWord("./corpus/fre4all.txt")
	print("sentimentWord length: ", len(sentimentWord))

	posParaList,negParaList = __readCorpus("./corpus/train")
	print("posParaList length: ", len(posParaList))
	print("negParaList length: ", len(negParaList))	

	
	# sample review test
	'''
	partSamplePointList = __work4Corpus2Features(sentimentWord,posParaList[:1])
	oneSamplePointList = partSamplePointList[0]
	print(posParaList[0])
	print(list(jieba.cut(posParaList[0])))
	temp =[word for word in list(jieba.cut(posParaList[0])) if word in sentimentWord]
	print(temp)
	print("oneSamplePointList length: ",len(oneSamplePointList))
	print(oneSamplePointList.count(0))
	temp2=[(sentimentWord[index],pos) for index,pos in enumerate(oneSamplePointList) if pos!=0]
	print("temp2 length: ",len(temp2))
	print(temp2)

	'''
	posSamplePointList,negSamplePointList = __corpus2Features(sentimentWord,posParaList,negParaList)
	samplePointList = posSamplePointList + negSamplePointList
	print(len(samplePointList))

	
	paraList = __initParameter(len(sentimentWord))
	print("paraList length: ",len(paraList))
	temp = [theta*feature for theta,feature in zip(paraList,posSamplePointList[0])]
	print(len(temp))


	trainingModel(1000,0.001,posSamplePointList,negSamplePointList,paraList)
	pickleTrainingResult("./trainResult/trainresult.fre4all",sentimentWord,paraList)


def main():
	testCoding()

if __name__=='__main__':
	main()