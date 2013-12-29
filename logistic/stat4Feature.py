import os
import os.path
import jieba
import math
import codecs

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

def __segment(paragraphList):
	return [list(jieba.cut(paragraph)) for paragraph in paragraphList]

def __statistic(segmentParagraphList):
	wordDict={}
	[[__work4Statistic(wordDict,word) for word in paragraph] for paragraph in segmentParagraphList]
	return wordDict

def __work4Statistic(wordDict,word):
	wordDict[word] = wordDict.get(word,0)+1		

def __getConditionWord(wordDict,wordFre):
	return [word for word,fre in wordDict.items() if fre>=wordFre]

def __writeFeature(root,wordList):
	with open(root,"w") as fw:
		[fw.write(word+"\n") for word in wordList]

def testCoding():
	posParaList,negParaList = __readCorpus("./corpus/train")
	print(len(posParaList))
	print(len(negParaList))
	segmentParagraphList = __segment(posParaList+negParaList)
	print(len(segmentParagraphList))
	wordDict = __statistic(segmentParagraphList)
	wordResult = __getConditionWord(wordDict,3)
	print(len(wordResult))
	wordResult = __getConditionWord(wordDict,4)
	print(len(wordResult))

	__writeFeature("./corpus/fre4train.txt",wordResult)

	'''
	posParaList,negParaList = __readCorpus("./corpus/all1")
	print(len(posParaList))
	print(len(negParaList))
	print(negParaList,posParaList)
	segmentParagraphList = __segment(negParaList)
	print(segmentParagraphList)
	print(__statistic(segmentParagraphList))
	'''

def main():
	testCoding()

if __name__=='__main__':
	main()