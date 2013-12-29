import os
import codecs
import os.path
import jieba
import math
import pickle


def __initSentimentWordDict(sentimentWordRootAndName):
	sentimentWord=[]
	with open(sentimentWordRootAndName,"r") as fr:
		[sentimentWord.append(line.strip()) for line in fr]
	return list(set(sentimentWord))

def __preProcess(currentDir):
	posList=[]
	negList=[]
	for dirpath,dirlist,filelist in os.walk(currentDir):
		if dirpath.endswith("neg"):
			__readContent(dirpath,filelist,negList)

		elif dirpath.endswith("pos"):
			__readContent(dirpath,filelist,posList)
		
	return [posList,negList]


def __readContent(dirpath,filelist,outputlist):
	for fileA in filelist:
		with codecs.open(os.path.join(dirpath,fileA),"r",encoding="gbk") as fr:
			for line_raw in fr:
				if not line_raw.isspace(): 
					if line_raw.find("content")==-1:
						line = line_raw.strip()
						outputlist.append(line)



def __chineseSegment(paraList,sentimentWordList):
	sentimentDict = dict([(w,0) for w in sentimentWordList])
	paraSegList=[list(set(jieba.cut(sentence))) for sentence in paraList]

	for sentenceSeg in paraSegList:
		for word in sentenceSeg:
			if word in sentimentWordList:
				sentimentDict[word]=sentimentDict.get(word,0)+1
	# [[sentimentDict[word]=(sentimentDict.get(word,0)+1) for word in sentenceSeg] for sentenceSeg in paraSegList]
	return sentimentDict


def __maxLikelihood(posDict,negDict,posParaNums,negParaNums):
	posLikelihoodDict=dict([(key,(value+1)/(posParaNums+2)) for key,value in posDict.items()])
	negLikelihoodDict=dict([(key,(value+1)/(negParaNums+2)) for key,value in negDict.items()])
	return [posLikelihoodDict,negLikelihoodDict]


def initAndTraining(initFileRootAndName,corpusRoot):
	print("init and training please wait!")
	sentimentWord = __initSentimentWordDict(initFileRootAndName)
	posList,negList=__preProcess(corpusRoot)
	posSegDict = __chineseSegment(posList,sentimentWord)
	negSegDict = __chineseSegment(negList,sentimentWord)
	posLikelihoodDict,negLikelihoodDict=__maxLikelihood(posSegDict,negSegDict,len(posList),len(negList))
	print("sentiment list length :" ,len(sentimentWord))
	print("posList list length :" ,len(posList),"    ","posList list length :",len(negList))
	print("posSegDict list length :" ,len(posSegDict),"    ","negSegDict list length :",len(negSegDict))
	print("posLikelihoodDict list length :" ,len(posLikelihoodDict),"    ","negLikelihoodDict list length :",len(negLikelihoodDict))
	return sentimentWord,posLikelihoodDict,negLikelihoodDict


def pickleTrainingResult(pickleOutputFile,posLikelihoodDict,negLikelihoodDict,sentimentWord):
	with open(pickleOutputFile,"wb") as fwb:
		pickle.dump(posLikelihoodDict,fwb)
		pickle.dump(negLikelihoodDict,fwb)
		pickle.dump(sentimentWord,fwb)
	return pickleOutputFile

def unpickleTrainingResult(pickleOutputFile):
	with open(pickleOutputFile,"rb") as frb:
		posLikelihoodDict=pickle.load(frb)
		negLikelihoodDict=pickle.load(frb)
		sentimentWord=pickle.load(frb)
	return posLikelihoodDict,negLikelihoodDict,sentimentWord


def testSentence(sentimentWord,sentence,posLikelihoodDict,negLikelihoodDict):
	wordlist = list(jieba.cut(sentence))

	posProList = [1-posLikelihoodDict.get(word) if word not in wordlist else posLikelihoodDict.get(word) for word in sentimentWord]
	negProList = [1-negLikelihoodDict.get(word) if word not in wordlist else negLikelihoodDict.get(word) for word in sentimentWord]
	posResult = sum(map(lambda x: math.log(x),posProList))
	negResult = sum(map(lambda x: math.log(x),negProList))

	sentencePosProList=[posLikelihoodDict.get(word) for word in wordlist if word in sentimentWord]
	sentenceNegProList=[negLikelihoodDict.get(word) for word in wordlist if word in sentimentWord]
	return posResult,negResult,sentencePosProList,sentenceNegProList

def testSentenenceSimple(sentimentWord,sentence,posLikelihoodDict,negLikelihoodDict):
	wordlist = list(jieba.cut(sentence))
	posProList = [1-posLikelihoodDict.get(word) if word not in wordlist else posLikelihoodDict.get(word) for word in sentimentWord]
	negProList = [1-negLikelihoodDict.get(word) if word not in wordlist else negLikelihoodDict.get(word) for word in sentimentWord]
	posResult = sum(map(lambda x: math.log(x),posProList))
	negResult = sum(map(lambda x: math.log(x),negProList))

	sentencePosProList=[posLikelihoodDict.get(word) for word in wordlist if word in sentimentWord]
	sentenceNegProList=[negLikelihoodDict.get(word) for word in wordlist if word in sentimentWord]
	return posResult,negResult


def testcoding2():
	posLikelihoodDict,negLikelihoodDict,sentimentWord = unpickleTrainingResult("./naviebayesuse/pickleTraining.out")
	__outputTrainingResult("./naviebayesuse/posLikelihoodDict.out","./naviebayesuse/negLikelihoodDict.out",posLikelihoodDict,negLikelihoodDict)

	sentence = ""
	posResult,negResult,posProList,negProList=testSentence(sentimentWord=sentimentWord,sentence=sentence,posLikelihoodDict=posLikelihoodDict,negLikelihoodDict=negLikelihoodDict)
	print("posResult:  "+str(posResult))
	print("negResult:  "+str(negResult))
	print(posProList)
	print(negProList)

def testCoding():
	sentimentWord,posLikelihoodDict,negLikelihoodDict=initAndTraining("./naviebayesuse/corpus/outputword.txt","./naviebayesuse/corpus/train")

	pickleTrainingResult("./naviebayesuse/pickleTraining.out",posLikelihoodDict,negLikelihoodDict,sentimentWord)

	sentence = "这本书的内容太精彩了，印刷质量也非常好，快递也十分给力！"
	posResult,negResult,posProList,negProList=testSentence(sentimentWord=sentimentWord,sentence=sentence,posLikelihoodDict=posLikelihoodDict,negLikelihoodDict=negLikelihoodDict)
	print("posResult:  "+str(posResult))
	print("negResult:  "+str(negResult))
	print(posProList)
	print(negProList)
	


def __outputTrainingResult(posLikelihoodDictFile,negLikelihoodDictFile,posLikelihoodDict,negLikelihoodDict):
	with open(posLikelihoodDictFile,"w") as fw:
		[fw.write(key +" : " +str(value) + "\n") for key,value in posLikelihoodDict.items()]
	with open(negLikelihoodDictFile,"w") as fw:
		[fw.write(key +" : " +str(value) + "\n") for key,value in negLikelihoodDict.items()]

def __preTest(testRoot):
	posTestList=[]
	negTestList=[]
	for dirpath,dirlist,filelist in os.walk(testRoot):
		if dirpath.endswith("neg"):
			__readContent(dirpath,filelist,negTestList)

		elif dirpath.endswith("pos"):
			__readContent(dirpath,filelist,posTestList)

	return [posTestList,negTestList]




def testModeling():
	posTestList,negTestList = __preTest("./naviebayesuse/corpus/test")
	# print(len(posTestList),len(negTestList))
	posLikelihoodDict,negLikelihoodDict,sentimentWord = unpickleTrainingResult("./naviebayesuse/pickleTraining.out")

	temp_posReturn = [testSentenenceSimple(sentimentWord,sentence,posLikelihoodDict,negLikelihoodDict) for sentence in posTestList]
	posResultList = [1 if posResult>negResult else -1 if posResult<negResult else 0 for posResult,negResult in temp_posReturn]
	temp_negReturn = [testSentenenceSimple(sentimentWord,sentence,posLikelihoodDict,negLikelihoodDict) for sentence in negTestList]
	negResultList = [1 if posResult<negResult else -1 if posResult>negResult else 0 for posResult,negResult in temp_negReturn]

	# print(len(temp_posReturn))
	# print(len(posResultList))
	print("posResult: ", posResultList.count(1))
	# print(len(temp_negReturn))
	# print(len(negResultList))
	print("negResult: ",negResultList.count(1))

	print("the finalResule: ",(posResultList.count(1)+negResultList.count(1))/200)


def main():
	# testCoding()
	testModeling()

if __name__=='__main__':
	main()
