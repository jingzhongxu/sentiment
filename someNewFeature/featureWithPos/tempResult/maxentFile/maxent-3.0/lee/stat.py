# 这和我之前的svm的stat文件都不一样，这个我在处理之前的result文件的时候，无论是哪种，只要正确就是1,错误就是0；而svm的那个是libsvm训练的，所有就是实际的类别，所以我要分类处理
class MaxStat:
	def __init__(self,resultFileName):
		self.fileName = resultFileName
	
	def read(self):
		posResult = list()
		negResult = list()
		with open(self.fileName) as fr:
			[self.__work4Read(posResult,line[0]) if line[2]=="P" else self.__work4Read(negResult,line[0]) for line in fr]
		self.posResult = posResult
		self.negResult = negResult

	def __work4Read(self,listA,result):
		if result=="1":
			listA.append("1")
		else:
			listA.append("0")

	def computeRecall(self):
		if not hasattr(self,"posResult"):
			self.read()
		self.posRecall = self.posResult.count("1")/len(self.posResult) 
		self.negRecall = self.negResult.count("1")/len(self.negResult)	

	def computeAccuracy(self):
		if not hasattr(self,"posResult"):
			self.read()
		posAccNumbers = self.posResult.count("1")
		negAccNumbers = self.negResult.count("1")
		self.posAccuracy = posAccNumbers/(posAccNumbers+len(self.negResult)-negAccNumbers) 
		self.negAccuracy = negAccNumbers/(negAccNumbers+len(self.posResult)-posAccNumbers)

	def computeFScore(self):
		if not hasattr(self,"posAccuracy"):
			self.computeAccuracy()
		if not hasattr(self,"posRecall"):
			self.computeRecall()
		self.posFscore = 2*self.posAccuracy*self.posRecall/(self.posAccuracy+self.posRecall)
		self.negFscore = 2*self.negAccuracy*self.negRecall/(self.negAccuracy+self.negRecall)

	def outputResult(self):
		if not hasattr(self,"posFscore"):
			self.computeFScore()
		print("posAccuracy=",self.posAccuracy,",posRecall=",self.posRecall,",posFscore=",self.posFscore)
		print("negAccuracy=",self.negAccuracy,",negRecall=",self.negRecall,",negFscore=",self.negFscore)

def main():
	obj = MaxStat("./result.out")
	obj.outputResult()
	# obj.computeRecall()
	# print(obj.posResult)
	# print(obj.negResult)
	# print(obj.posRecall)
	# print(obj.negRecall)

def work4Test(listA,result):
	if result=="1":
		listA.append(1)
	else:
		listA.append(0)	

def test(fileName):
	posResult = list()
	negResult = list()
	with open(fileName) as fr:
		# [print(line[0],line[2]) for line in fr]
		[work4Test(posResult,line[0]) if line[2]=="P" else work4Test(negResult,line[0]) for line in fr]
		print(posResult)
		print(negResult)


if __name__ == '__main__':
	main()
	# test("./result.out")