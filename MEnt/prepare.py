import os
import os.path
import jieba
import codecs

def __readCorpus(root):
	for dirpath,dirlist,filelist in os.walk(root):
		if dirpath.endswith("pos"):
			posParagraphList = __work4ReadCorpus(dirpath,filelist)
		elif dirpath.endswith("neg"):
			negParagraphList = __work4ReadCorpus(dirpath,filelist)
	return posParagraphList,negParagraphList


def __work4ReadCorpus(root,filelist):
	return [__word4ReadCorpus2(codecs.open(os.path.join(root,fileA),"r",encoding='gbk'),fileA) for fileA in filelist]


def __word4ReadCorpus2(fr,fileA):
	# print(fileA)
	paragraphList = [line.strip() for line in fr if not line.isspace() and line.find("content>")==-1]
	fr.close()
	return paragraphList[0]

def __readSentimentWord(root):
	with codecs.open(root,"r",encoding='utf-8') as fr:
		sentimentWord = [line.strip() for line in fr if not line.isspace()]
	return sentimentWord


def readCorpus(paragraphRoot,sentimentWordRoot):
	posParagraphList,negParagraphList = __readCorpus(paragraphRoot)
	sentimentWord = __readSentimentWord(sentimentWordRoot)
	return posParagraphList,negParagraphList,sentimentWord


def __segmentParagraph(paragraphList):
	return [list(jieba.cut(paragraph)) for paragraph in paragraphList]

def word2Feature(posParagraphList,negParagraphList,sentimentWord):
	posSegments = __segmentParagraph(posParagraphList)
	negSegments = __segmentParagraph(negParagraphList)
	posFeatures=[]
	negFeatures=[]
	
	for posSegment in posSegments:
		oneExample = __work4Word2Feature_2(len(sentimentWord)*2)
		[__work4Word2Feature_3(oneExample,sentimentWord.index(word)) for word in posSegment if word in sentimentWord]
		posFeatures.append(oneExample)
	for negSegment in negSegments:
		oneExample = __work4Word2Feature_2(len(sentimentWord)*2)
		[__work4Word2Feature_3(oneExample,sentimentWord.index(word) + len(sentimentWord)) for word in negSegment if word in sentimentWord]
		negFeatures.append(oneExample)

	boundryTwoClass = len(posFeatures)  # the boundryTwoClass value postion is at the second class begin position
	return posFeatures+negFeatures,boundryTwoClass

def __work4Word2Feature_2(length):
	return [0 for i in range(length)]

def __work4Word2Feature_3(listA,index):
	listA[index] = 1


def test():
	posParagraphList,negParagraphList = __readCorpus("./corpus/all1")
	print("posParagraphList length: ",len(posParagraphList))
	print("negParagraphList length: ",len(negParagraphList))
	print("one posParagraphList: ",posParagraphList[10])
	print("one negParagraphList: ",negParagraphList[10])


	sentimentWord = __readSentimentWord("./corpus/fre4train.txt")
	print("sentimentWord length: ",len(sentimentWord))
	print("one sentimentWord: ",sentimentWord[10])

	print("**"*30)
	

	posSegments = __segmentParagraph(posParagraphList)
	negSegments = __segmentParagraph(negParagraphList)
	print("posSegments length: ",len(posSegments))
	print("negSegments length: ",len(negSegments))
	print("one posSegment example: ",posSegments[10])
	print("one negSegment example: ",negSegments[10])

	print("**"*30)	

	posFeatures=[]
	negFeatures=[]
	
	for posSegment in posSegments:
		oneExample = __work4Word2Feature_2(len(sentimentWord)*2)
		[__work4Word2Feature_3(oneExample,sentimentWord.index(word)) for word in posSegment if word in sentimentWord]
		posFeatures.append(oneExample)
	for negSegment in negSegments:
		oneExample = __work4Word2Feature_2(len(sentimentWord)*2)
		[__work4Word2Feature_3(oneExample,sentimentWord.index(word)+ len(sentimentWord)) for word in negSegment if word in sentimentWord]
		negFeatures.append(oneExample)

	print("posFeatures length: ",len(posFeatures))
	print("negFeatures length: ",len(negFeatures))
	print("posFeatures[x] length: ",len(posFeatures[10]))
	print("negFeatures[x] length: ",len(negFeatures[10]))
	print("posFeatures[x] 0 nums half pos: ",posFeatures[10][:len(sentimentWord)].count(0))
	print("posFeatures[x] 0 nums half neg: ",posFeatures[10][len(sentimentWord):].count(0))
	print("negFeatures[x] 0 nums half pos: ",negFeatures[10][:len(sentimentWord)].count(0))
	print("negFeatures[x] 0 nums half neg: ",negFeatures[10][len(sentimentWord):].count(0))

def testFun():
	posParagraphList,negParagraphList,sentimentWord = readCorpus("./corpus/test","./corpus/fre4train.txt")
	print("posParagraphList length: ",len(posParagraphList))
	print("negParagraphList length: ",len(negParagraphList))
	print("one posParagraphList: ",posParagraphList[10])
	print("one negParagraphList: ",negParagraphList[10])
	print("sentimentWord length: ",len(sentimentWord))
	print("one sentimentWord: ",sentimentWord[10])
	print("**"*30)

	allFeatures,boundryTwoClass = word2Feature(posParagraphList,negParagraphList,sentimentWord)
	print("allFeatures length: ",len(allFeatures))
	print("boundryTwoClass vlaue: ",boundryTwoClass)
	print("allFeatures[x] length: ",len(allFeatures[10]))
	print("posFeatures[x] 0 nums half pos: ",allFeatures[10][:len(sentimentWord)].count(0))
	print("posFeatures[x] 0 nums half neg: ",allFeatures[10][len(sentimentWord):].count(0))
	print("negFeatures[x] 0 nums half pos: ",allFeatures[190][:len(sentimentWord)].count(0))
	print("negFeatures[x] 0 nums half neg: ",allFeatures[190][len(sentimentWord):].count(0))

if __name__=='__main__':
	testFun()