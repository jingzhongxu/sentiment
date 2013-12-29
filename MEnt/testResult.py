import prepare
import jieba
import pickle

def initTestCourpus(testRoot,sentimentFile):
	posParagraphList,negParagraphList,sentimentWord = prepare.readCorpus(testRoot,sentimentFile)
	allFeatures,boundryTwoClass= prepare.word2Feature(posParagraphList,negParagraphList,sentimentWord)
	return sentimentWord,allFeatures,boundryTwoClass

def readParameters(paraRoot):
	with open(paraRoot,"rb") as frb:
		parameters = pickle.load(frb)
	return parameters  



def __work4computePGivenX(index,features,boundryTwoClass,parameters,sentimentWordLength):
	if index<boundryTwoClass:
		pos = features
		neg = features[sentimentWordLength:]+features[:sentimentWordLength]
	else:
		neg = features
		pos = features[sentimentWordLength:]+features[:sentimentWordLength]
	py1 = sum([pos_ele * parameter for pos_ele,parameter in zip(pos,parameters)])
	py0 = sum([neg_ele * parameter for neg_ele,parameter in zip(neg,parameters)])

	if index<boundryTwoClass:
		if py1>py0:return True
		else: return False
	else:
		if py1<py0:return True
		else: return False


def computePGivenX(allFeatures,parameters,boundryTwoClass,sentimentWordLength):
	result = [__work4computePGivenX(index,features,boundryTwoClass,parameters,sentimentWordLength) for index,features in enumerate(allFeatures)]
	return result[:boundryTwoClass].count(True),result[boundryTwoClass:].count(True)




def main():
	sentimentWord,allFeatures,boundryTwoClass = initTestCourpus("./corpus/test","./corpus/outputword.txt")
	parameters = readParameters("./trainResult.outputword")
	posNums,negNums = computePGivenX(allFeatures,parameters,boundryTwoClass,len(sentimentWord))
	print(posNums/100,negNums/100,(posNums+negNums)/200)

if __name__ == '__main__':
	main()