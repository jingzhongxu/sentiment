from prepare import ProcessCorpus
import pickle
import numpy as np

class Test:
	def __init__(self,testPickleFile,trainResultFile):
		with open(testPickleFile,'rb') as frb:
			obj = pickle.load(frb)
			self.points = obj.posSamplePointList + obj.negSamplePointList
			self.boundary = len(obj.posSamplePointList)
			self.label = [1 if i < self.boundary else -1 for i in range(len(self.points))]
		with open(trainResultFile,'rb') as frb:
			self.w = pickle.load(frb)

	def getTestResult(self):
		result_temp = [np.sign(sum([wi*point_item for wi,point_item in zip(self.w,points_item)])) for points_item in self.points]
		self.result = [1 if result_temp_item==lable_item else 0 for result_temp_item,lable_item in zip(result_temp,self.label)]
		return self.result[:self.boundary].count(1)/len(self.result[:self.boundary]),self.result[self.boundary:].count(1)/len(self.result[self.boundary:]),self.result.count(1)/len(self.result)

def test():
	testObj = Test('./pickle/fre4train.test','./pickle/fre4train.result.out')
	print('points length: ',len(testObj.points))
	print("label length: ",len(testObj.label))
	print("w length: ",len(testObj.w))
	# print(testObj.w)

	print(testObj.getTestResult())
	print('result length: ',len(testObj.result))
	# print(testObj.result)



if __name__=='__main__':
	test()