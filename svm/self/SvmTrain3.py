'''this is a program 4 svmTrain2.py. It improvements the iterator condition which the old one usually do not convergence'''
import jieba
import os
import os.path
import codecs
import pickle
import math
from random import randrange
import matplotlib.pyplot as plt
import numpy as np

def SvmTrain:
	def __init__(self,C,posPoints=None,negPoints=None):
		self.C = C
		self.tol = 10e-3
		self.b =0
		if posPoints!= None and negPoints!=None:
			self.points = posPoints + negPoints
			self.boundary = len(posPoints)
			self.label = [1 if i < self.boundary else -1 for i in range(len(self.points))]
			self.pointsNums = len(self.points)
			self.alpha = [0 for i in range(pointsNums)]
			self.w = [0 for i in range(pointsNumss)]
			self.dimesion = len(self.points[0])
		else:
			posPoints_self = [[randrange(1,100),randrange(1,100)] for i in range(50)]
			negPoints_self = [[randrange(100,200),randrange(100,200)] for i in range(50)]
			self.points = posPoints_self + negPoints_self
			self.boundary = len(posPoints_self)
			self.label = [1 if i < self.boundary else -1 for i in range(len(self.points))]
			self.pointsNums = len(self.points)
			self.alpha = [0 for i in range(pointsNums)]
			self.w = [0 for i in range(pointsNums)]
			self.dimesion = len(self.points[0])

	def __linearKernelFun(self,attr1,attr2):
		return sum([a*b for a,b in zip(attr1,attr2)])

	def __gaussKernelFun(self,attr1,attr2):
		return math.exp(sum([a*a for a in[item1-item2 for item1,item2 in zip(attr1,attr2)]]) * (-1) / 2)

	def __constructKernelMatrix(self,kernelFun):
		self.kernelMatrix = [[kernelFun(self.points[i],self.points[index]) for index in range(self.pointsNums)] for i in range(self.pointsNums)]

	def __constructW(self):
		self.w = [sum([self.alpah[i]*self.label[i]*self.points[i][dim] for i in range(self.pointsNums)])for dim in range(self.dimesion)]
		
	def __constructErrorCache(self):
				


	