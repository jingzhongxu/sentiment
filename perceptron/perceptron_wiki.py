import matplotlib.pyplot as plt
import numpy as np
from random import randrange

'''
this is a program for train percepton use wiki method
'''
class PerceptronPocket:
	def __init__(self,alpha = 0.5):
		posSamplePointList = [[randrange(0,70) for j in range(2)]for i in range(20)]
		negSamplePointList = [[randrange(40,100) for j in range(2)]for i in range(20)]
		self.rowPoints = posSamplePointList + negSamplePointList
		self.boundary = len(posSamplePointList)
		self.label = [1 if i <self.boundary else -1 for i in range(len(self.rowPoints))]
		self.alpha = alpha

	def initialize(self):
		self.points = [[1]+ rowPoints_item for rowPoints_item in self.rowPoints]
		length = len(self.points[0])
		self.w = [0 for i in range(length)]
		self.pointNum = len(self.points)
		self.transformY()
	
	def computeroutput(self,index):
		return np.sign(sum([wi*point_item for wi,point_item in zip(self.w,self.points[index])]))

	def update(self,index):
		output = self.computeroutput(index)
		for w_index,wi_value in enumerate(self.w):
			# print(self.alpha*(self.label[index]-output) ,self.points[index][w_index],self.alpha*(self.label[index]-output) * self.points[index][w_index])
			self.w[w_index] = wi_value + self.alpha*(self.label[index]-output) * self.points[index][w_index]
	
	def getFinalTrain(self):
		output = [self.computeroutput(index) for index in range(self.pointNum)]
		return output

	def getAccuraceInEveryStep(self):
		output = [self.computeroutput(index) for index in range(self.pointNum)]
		result = [1 if output_item==lable_item else 0 for output_item,lable_item in zip(output,self.label)]
		return result.count(1)

	def getErrorRate(self):
		accu = self.getAccuraceInEveryStep()
		return (self.pointNum - accu) /self.pointNum	

	def trainWithIteratorTime(self,iteratorNum):
		if not hasattr(self,'w'): self.initialize()
		
		for iteratorTime in range(iteratorNum):
			for index in range(len(self.points)):
				self.update(index)
			print('iteratorNum ',iteratorTime," w ",self.w,'accur ',self.getAccuraceInEveryStep())
			
		print(self.getFinalTrain())
		X = np.arange(-10,100,1)
		# Y = (-1)*(self.w[0] + self.w[1]* X)/self.w[2]
		Y = eval(self.__getY4Matlib())
		plt.axis([-10,110,-10,110])
		plt.scatter(self.points_x_pos,self.points_y_pos,marker='o')
		plt.scatter(self.points_x_neg,self.points_y_neg,marker='x')
		plt.plot(X,Y,'b')
		plt.show()

	def trainWithThreshold(self,errorRate=0.2):
		if not hasattr(self,'w'): self.initialize()
		error = 1.0
		iteratorTime =0
		while(error > errorRate):
			for index in range(len(self.points)):
				self.update(index)
			error = self.getErrorRate()
			print('iteratorNum ',iteratorTime," w ",self.w,'accur ',self.getAccuraceInEveryStep())
			iteratorTime +=1

		X = np.arange(-10,100,1)
		# Y = (-1)*(self.w[0] + self.w[1]* X)/self.w[2]
		Y = eval(self.__getY4Matlib())
		plt.axis([-10,110,-10,110])
		plt.scatter(self.points_x_pos,self.points_y_pos,marker='o')
		plt.scatter(self.points_x_neg,self.points_y_neg,marker='x')
		plt.plot(X,Y,'b')
		plt.show()


	def __getY4Matlib(self):
		return '(-1)*(self.w[0] + self.w[1]* X)/self.w[2]'

	def transformY(self):
		points_real = [(points_item[1],points_item[2]) for points_item in self.points]
		self.points_x_pos = [points_real_item[0] for index,points_real_item in enumerate(points_real) if index<self.boundary]
		self.points_y_pos = [points_real_item[1] for index,points_real_item in enumerate(points_real) if index<self.boundary]
		self.points_x_neg = [points_real_item[0] for index,points_real_item in enumerate(points_real) if index>=self.boundary]
		self.points_y_neg = [points_real_item[1] for index,points_real_item in enumerate(points_real) if index>=self.boundary]		



def main():
	obj = PerceptronPocket()
	obj.initialize()
	print(obj.points)
	print(obj.points_x_pos)
	print(obj.points_y_pos)
	print(obj.points_x_neg)
	print(obj.points_y_neg)	
	# obj.train(200)
	obj.trainWithThreshold()

if __name__=='__main__':
	main()