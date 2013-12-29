import os
import os.path
import jieba
import codecs

def readCorpus(root):
	for dirpath,dirlist,filelist in os.walk(root):
		if dirpath.endswith("pos"):
			posParagraphs=__readFiles(dirpath,filelist)
		elif dirpath.endswith("neg"):
			negParagraphs=__readFiles(dirpath,filelist)
	return posParagraphs,negParagraphs

def __readFiles(dirpath,filelist):
	paragraphs=[]
	for fileA in filelist:
		with codecs.open(os.path.join(dirpath,fileA),"r",encoding="gbk") as fr:
			[paragraphs.append(paragraph.strip()) for paragraph in fr if not paragraph.isspace() and paragraph.find("content")==-1]
			# if len(paragraphs)==2:print(os.path.join(dirpath,fileA))
	return paragraphs

def readSentimenword(filename):
	sentimentword=[]
	with codecs.open(filename,"r",encoding="utf-8") as fr:
		[sentimentword.append(word.strip()) for word in fr if not word.isspace()]
	return sentimentword

def __assAsExpInWork4Corpus2Features(oneSamplePointList,index):
	oneSamplePointList[index] = 1

def __work4Corpus2Features(sentimentword,paragraphList):
	partSamplePointList=[]
	for sentence in paragraphList:
		oneSamplePointList=[0 for i in range(len(sentimentword))]
		[__assAsExpInWork4Corpus2Features(oneSamplePointList,sentimentword.index(word)) for word in list(jieba.cut(sentence)) if word in sentimentword]
		partSamplePointList.append(oneSamplePointList)
	return partSamplePointList

def corpus2Features(sentimentWord,posParaList,negParaList):
	posSamplePointList = __work4Corpus2Features(sentimentWord,posParaList)
	negSamplePointList = __work4Corpus2Features(sentimentWord,negParaList)
	samplePointList = posSamplePointList + negSamplePointList
	return posSamplePointList,negSamplePointList


def writeFile4SVM(filename,posSamplePointList,negSamplePointList):
	with codecs.open(filename,"w",encoding="utf-8") as fw:
		[[fw.write("+1 " + str(index+1) + ":" +str(value) + " ") if index==0 else fw.write(str(index+1) + ":" +str(value) + " ")  if index!=len(oneSamplePointList)-1 else fw.write(str(index+1) + ":" +str(value) +" \n") for index,value in enumerate(oneSamplePointList)] for oneSamplePointList in posSamplePointList]
		[[fw.write("-1 " + str(index+1) + ":" +str(value) + " ") if index==0 else fw.write(str(index+1) + ":" +str(value) + " ")  if index!=len(oneSamplePointList)-1 else fw.write(str(index+1) + ":" +str(value) +" \n") for index,value in enumerate(oneSamplePointList)] for oneSamplePointList in negSamplePointList]


def work4Train():
	posParagraphs,negParagraphs = readCorpus("./corpus/train")
	sentimentword = readSentimenword("./corpus/outputword.txt")
	posSamplePointList,negSamplePointList = corpus2Features(sentimentword,posParagraphs,negParagraphs)
	writeFile4SVM("./trainAtest/outputword/svmtrain_outputword",posSamplePointList,negSamplePointList)

def work4Test():
	posParagraphs,negParagraphs = readCorpus("./corpus/test")
	sentimentword = readSentimenword("./corpus/outputword.txt")
	posSamplePointList,negSamplePointList = corpus2Features(sentimentword,posParagraphs,negParagraphs)
	writeFile4SVM("./trainAtest/outputword/svmtest_outputword",posSamplePointList,negSamplePointList)


def main():
	# test()
	work4Train()
	work4Test()

def test():
	posParagraphs,negParagraphs = readCorpus("./corpus/train")
	print(len(posParagraphs))
	print(len(negParagraphs))
	print(posParagraphs[0])
	sentimentword = readSentimenword("./corpus/fre4all.txt")
	print(len(sentimentword))
	print(sentimentword[0])
	posSamplePointList,negSamplePointList = corpus2Features(sentimentword,posParagraphs,negParagraphs)
	print(len(posSamplePointList))
	print(len(negSamplePointList))

	writeFile4SVM("./trainAtest/svmtrainall.result",posSamplePointList,negSamplePointList)

if __name__ =='__main__':
	main()

