import sys
class Compute:
	def __init__(self,libsvmResultFileName,divideNumber):
		self.filename = libsvmResultFileName
		self.divideNumber = divideNumber
	
	def readContent(self):
		self.posResult=list()
		self.negResult=list()
		with open(self.filename,"r") as fr:
			[self.posResult.append(line.strip()) if index<self.divideNumber else self.negResult.append(line.strip()) for index,line in enumerate(fr)]

	def computeRecall(self):
		if not hasattr(self,"posResult"):
			self.readContent()
		self.posRecall = self.posResult.count("1")/len(self.posResult) 
		self.negRecall = self.negResult.count("-1")/len(self.negResult)	

	def computeAccuracy(self):
		if not hasattr(self,"posResult"):
			self.readContent()
		posAccNumbers = self.posResult.count("1")
		negAccNumbers = self.negResult.count("-1")
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
	if len(sys.argv)!=2:
		print("[useage processFile")
		return
	obj = Compute(sys.argv[1],200)
	obj.outputResult()

def test():
	with open("./testFormat.result.out","r") as fr:
		for index,line in enumerate(fr):
			print(index,line.strip(),type(line.strip()))

if __name__=='__main__':
	main()
	# test()