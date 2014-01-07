from prepare import ProcessCorpus
import pickle
from random import randrange
import datetime


'''
this is a moduel for useing pocket method to train perceptron for sentiment classfication
'''
class PerceptronPocket:
	def __init__(self,pickleFile):
		with open(pickleFile,"rb") as frb:
			obj = pickle.load(frb)
			self.points = obj.posSamplePointList + obj.negSamplePointList
			boundary = len(obj.posSamplePointList)
			self.label = [1 if i <boundary else -1 for i in range(len(self.points))]

	def initialize(self):
		length = len(self.points[0])
		self.w = [0 for i in range(length)]
		self.pointNum = len(self.points)
	

	def findRandomMistake(self):
		randomNum = randrange(self.pointNum)
		for num in range(self.pointNum):
			index = (num + randomNum) % self.pointNum
			if self.judgeMistake(index,self.w):
				return index
		return -1

	def statisticMistake(self,w_temp):
		mistakeNum = 0
		for index in range(len(self.points)):
			if self.judgeMistake(index,w_temp):
				mistakeNum+=1
		return mistakeNum
	
	def updatePara(self,listw):
		self.w = listw
	
	def getTempw(self,index):
		update = [xi*self.label[index] for xi in self.points[index]]
		return [self.w[i] + update[i] for i in range(len(self.w))]

	def judgeMistake(self,index,w_temp):
		wx = sum([wi*xi for wi,xi in zip(w_temp,self.points[index])])
		if self.label[index] * wx <=0 : return True
		else: return False	

	def train(self,iteratorNum):
		lastIndexMistakeNum = len(self.points)
		for itertime in range(iteratorNum):
			index = self.findRandomMistake()
			w_temp = self.getTempw(index)
			mistakeNum = self.statisticMistake(w_temp)
			if  mistakeNum< lastIndexMistakeNum:
				self.updatePara(w_temp)
				lastIndexMistakeNum = mistakeNum
			print('iteratorNum ',itertime)

	def pickleobj(self,pickleFile):
		with open(pickleFile,"wb") as fwb:
			pickle.dump(self.w,fwb)


def test():
	begin =datetime.datetime.now()
	perceptronObj = PerceptronPocket('./pickle/fre4train.train')
	perceptronObj.initialize()
	print('self.points length: ',len(perceptronObj.points))
	print('self.label length: ',len(perceptronObj.label))
	print('self.label.count(1)',perceptronObj.label.count(1))
	print('self.label.count(-1)',perceptronObj.label.count(-1))
	perceptronObj.train(500)
	perceptronObj.pickleobj('./pickle/fre4train.result.out')
	end = datetime.datetime.now()
	print('all cost time: ',end-begin)


if __name__=='__main__':
	test()


