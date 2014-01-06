import matplotlib.pyplot as plt
import numpy as np
from random import randrange

class PerceptronPocket:
	def __init__(self):
		posSamplePointList = [[randrange(0,5) for j in range(2)]for i in range(10)]
		negSamplePointList = [[randrange(5,10) for j in range(2)]for i in range(10)]
		self.rowPoints = posSamplePointList + negSamplePointList
		self.boundary = len(posSamplePointList)
		self.label = [1 if i <self.boundary else -1 for i in range(len(self.rowPoints))]

	def initialize(self):
		self.points = [[1]+ rowPoints_item for rowPoints_item in self.rowPoints]
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
		if not hasattr(self,'w'): self.initialize()
		lastIndexMistakeNum = len(self.points)
		print('lastIndexMistakeNum ',lastIndexMistakeNum)
		for itertime in range(iteratorNum):
			index = self.findRandomMistake()
			w_temp = self.getTempw(index)
			mistakeNum = self.statisticMistake(w_temp)
			print('mistakeNum: ',mistakeNum)
			if  mistakeNum <= lastIndexMistakeNum:
				self.updatePara(w_temp)
				lastIndexMistakeNum = mistakeNum
				print('update')

		X = np.arange(-10,100,30)
		# Y = (-1)*(self.w[0] + self.w[1]* X)/self.w[2]
		Y = eval(self.__getY4Matlib())
		plt.axis([-10,110,-10,110])
		plt.scatter(self.points_x_pos,self.points_y_pos,marker='o')
		plt.scatter(self.points_x_neg,self.points_y_neg,marker='x')
		plt.plot(X,Y,'b')
		plt.show()

		print('iteratorNum ',itertime," w ",self.w)

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
	obj.transformY()
	print(obj.points)
	print(obj.points_x_pos)
	print(obj.points_y_pos)
	print(obj.points_x_neg)
	print(obj.points_y_neg)	
	obj.train(20000)

if __name__=='__main__':
	main()