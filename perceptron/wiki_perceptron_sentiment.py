from prepare import ProcessCorpus
import pickle
from random import randrange
import datetime
import numpy as np

'''
this is a moduel for useing wiki method to train perceptron for sentiment classfication
'''
class WikiPerceptron:
	def __init__(self,pickleFile,alpha=0.5):
		with open(pickleFile,"rb") as frb:
			obj = pickle.load(frb)
		self.rowPoints = obj.posSamplePointList + obj.negSamplePointList
		self.boundary = len(obj.posSamplePointList)
		self.label = [1 if i <self.boundary else -1 for i in range(len(self.rowPoints))]
		self.alpha = alpha

	def initialize(self):
		self.points = [[1]+ rowPoints_item for rowPoints_item in self.rowPoints]
		length = len(self.points[0])
		self.w = [0 for i in range(length)]
		self.pointNum = len(self.points)

	def __computeOutput(self,index):
		return  np.sign(sum([w_item*point_item for w_item,point_item in zip(self.w,self.points[index])]))

	def update(self,index):
		output = self.__computeOutput(index)
		if output!=self.label[index]:
			for w_index,w_value in enumerate(self.w):
				self.w[w_index] = w_value + self.alpha * (output-self.label[index])*self.points[index][w_index]

	def __getErrorRate(self):
		outputList = [self.__computeOutput(index)for index in range(self.pointNum)]
		result = [1 if output==self.label[index] else 0 for index,output in enumerate(outputList)]
		return result.count(0)/self.pointNum

	def trainWithError(self,errorRate=0.15):
		if not hasattr(self,'w'):self.initialize()
		error = 1.0
		iteratorTime =0
		while error > errorRate:
			for index in range(self.pointNum):
				self.update(index)
			error= self.__getErrorRate()
			iteratorTime+=1
			print('itertime ',iteratorTime,'error ',error)

	def pickleW(self,outputRoot):
		with open(outputRoot,'wb') as fwb:
			pickle.dump(self.w,fwb)

	def work(self,outputRoot):
		self.initialize()
		self.trainWithError()
		self.pickleW('./result.wikiPerSenti.out')


def main():
	obj = WikiPerceptron('./pickle/fre4train.train')
	obj.work('./pickle/fre4train.result4UseWiki.out')

if __name__=='__main__':
	main()


