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

class SvmTrain:
	def __init__(self,C=10,posPoints=None,negPoints=None):
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
			self.alpha = [0 for i in range(self.pointsNums)]
			self.w = [0 for i in range(self.pointsNums)]
			self.dimesion = len(self.points[0])

	def __linearKernelFun(self,attr1,attr2):
		return sum([a*b for a,b in zip(attr1,attr2)])

	def __gaussKernelFun(self,attr1,attr2):
		return math.exp(sum([a*a for a in[item1-item2 for item1,item2 in zip(attr1,attr2)]]) * (-1) / 2)

	def __constructKernelMatrix(self,kernelFun):
		self.kernelMatrix = [[kernelFun(self.points[i],self.points[index]) for index in range(self.pointsNums)] for i in range(self.pointsNums)]

	def __constructW(self):
		self.w = [sum([self.alpha[i]*self.label[i]*self.points[i][dim] for i in range(self.pointsNums)])for dim in range(self.dimesion)]
		
	def __constructErrorCache(self):
		self.E = [(sum([wi*xji for wi,xji in zip(self.w,self.points[j])])-1) if j<self.boundary else (sum([wi*xji for wi,xji in zip(self.w,self.points[j])])+1) for j in range(self.pointsNums)]		

	def __computeLAndH(self,index_1,index_2):
		a1 = self.alpha[index_1]
		a2 = self.alpha[index_2]
		y1 = self.label[index_1]
		y2 = self.label[index_2]
		C = self.C
		if y1!=y2:
			L = max(0,a2-a1)
			H = min(C,C+a2-a1)
		else:
			L = max(0,a1+a2-C)
			H = min(C,a1+a2)
		return L,H

	def __computeErrorSingle(self,index):
		return sum([wi*xi for wi,xi in zip(self.w,self.points[index])])-self.label[index]

	def __updateThreshold_b(self,index_1,index_2,a1_old,a1_new,a2_old,a2_new_cliped,L,H):
		b_old = self.b
		E1 = self.E[index_1]
		E2 = self.E[index_2]
		k11 = self.kernelMatrix[index_1][index_1]
		k12 = self.kernelMatrix[index_1][index_2]
		k22 = self.kernelMatrix[index_2][index_2]
		y1 = self.label[index_1]
		y2 = self.label[index_2]
		C = self.C

		b1 =  b_old - E1 - y1 *(a1_new-a1_old)*k11 - y2 *(a2_new_cliped-a2_old) * k12 
		b2 =  b_old - E2 - y1 *(a1_new-a1_old)*k12 - y2 *(a2_new_cliped-a2_old) * k22 
		if a1_new >0 and a1_new <C:
			self.b = b1
		elif a2_new_cliped >0 and a2_new_cliped<C:
			self.b = b2
		else:
			self.b = (b1+b2)/2

	def __updateE(self,index_1,index_2,a1_old,a1_new,a2_old,a2_new_cliped,b_old,b_new):
		y1 = self.label[index_1]
		y2 = self.label[index_2]

		for index,E_index in enumerate(self.E):
			E_index_new = E_index + y1*(a1_new-a1_old)*self.kernelMatrix[index][index_1] + y2 *(a2_new_cliped - a2_old)*self.kernelMatrix[index][index_2] - b_old + b_new
			self.E[index] = E_index_new 

	'''ObjectiveFun is w(a)'''
	def __computeObjectiveFun(self,E1,E2,k11,k12,k22,a1_old,a1_new,a2_old,a2_new_cliped,y1,y2,b_old):
		return a1_new+a2_new_cliped-0.5*k11*a1_new**2-0.5*k22*a2_new_cliped**2-y1*y2*k12*a1_new*a2_new_cliped-a1_new*y1*(y1+E1-a1_old*y1*k11-a2_old*y2*k12-b_old) -a2_new_cliped*y2*(y2+E2-a1_old*y1*k12-a2_old*y2*k22-b_old)

	def __svmTestOne(self,index_x):
		f = sum([self.alpha[i]*self.label[i]*self.kernelMatrix[i][index_x] for i in self.l])
		return f+self.b	



	'''now the program has begin to core function'''
	def takestep(self,index_1,index_2):
		if index_1 == index_2: return 0
		a1_old = self.alpha[index_1]
		a2_old = self.alpha[index_2]
		y1 = self.label[index_1]
		y2 = self.label[index_2]
		E1 = self.E[index_1]
		E2 = self.E[index_2]
		b_old = self.b
		s = y1*y2
		C = self.C
		L,H = self.__computeLAndH(index_1,index_2)
		if L==H: return 0
		k11 = self.kernelMatrix[index_1][index_1]
		k12 = self.kernelMatrix[index_1][index_2]
		k22 = self.kernelMatrix[index_2][index_2]
		eta = 2*k12 - k11 - k22

		if(eta<0):
			a2_new = a2_old - y2*(E1-E2)/eta
			if a2_new < L: a2_new_cliped = L
			elif a2_new >H: a2_new_cliped = H
			else: a2_new_cliped = a2_new
		else:
			print('eta >0 occaus')
			a2_tempL = L
			a1_tempL = a1_old + s*(a2_old-a2_tempL)
			a2_tempH = H
			a1_tempH = a1_old + s*(a2_Old -a2_tempH)
			Lobj = self.__computeObjectiveFun(E1,E2,k11,k12,k22,a1_old,a1_tempL,a2_old,a2_tempL,y1,y2,b_old)
			Hobj = self.__computeObjectiveFun(E1,E2,k11,k12,k22,a1_old,a1_tempH,a2_old,a2_tempH,y1,y2,b_old)
			if Lobj > Hobj: a2_new_cliped=L
			elif Lobj<Hobj: a2_new_cliped=H
			else:a2_new_cliped=a2_old
			
		a1_new = a1_old + s*(a2_old-a2_new_cliped)
		self.__updateThreshold_b(index_1,index_2,a1_old,a1_new,a2_old,a2_new_cliped,L,H)
		b_new = self.b
		self.__updateE(index_1,index_2,a1_old,a1_new,a2_old,a2_new_cliped,b_old,b_new)
		self.alpha[index_1] = a1_new
		self.alpha[index_2] = a2_new_cliped
		return 1

	def examineExample(self,index_2):
		y2 = self.label[index_2]
		a2 = self.alpha[index_2]
		E2 = self.E[index_2]
		r2 = E2 * y2
		C = self.C
		tol = self.tol
		if (r2<(-1)*tol and a2<C) or (r2>tol and a2>0):
			exist = False
			for i in range(self.pointsNums):
				if self.alpha[i]>0 and self.alpha[i]<C:
					exist = True
					break
			if exist:
				#second choice
				maxind = 0
				maxvla=abs(self.E[maxind]-E2)
				for i in range(1,self.pointsNums):
					if abs(self.E[i]-E2)>maxvla:
						maxind = i
						maxvla = abs(self.E[i]-E2)
				if self.takestep(maxind,index_2): return 1

			#loop over non-zero & non-C alpha, starting at a random point:
			k = randrange(self.pointsNums)
			for num in range(self.pointsNums):
				index = (num+k)%self.pointsNums
				if self.alpha[index]>0 and self.alpha[index]<C:
					if self.takestep(index,index_2):return 1

			 #loop over all i, starting at a random point
			k = randrange(self.pointsNums)
			for num in range(self.pointsNums):
				index = (num+k)%self.pointsNums
				if self.takestep(index,index_2):return 1
		return 0

	def routine(self,show=False):
		numChanged = 0
		examimeAll = 1
		iterateTime = 0
		C = self.C

		if show==True : self.__work4Draw()

		while numChanged>0 or examimeAll==1:
			numChanged = 0
			if examimeAll:
				for index_2 in range(self.pointsNums):
					numChanged +=self.examineExample(index_2)
			else:
				for index_2 in range(self.pointsNums):
					if self.alpha[index_2]>0 and self.alpha[index_2]<C:
						numChanged +=self.examineExample(index_2)

			if examimeAll:
				examimeAll = 0
			elif numChanged==0:
				examimeAll = 1
			
			iterateTime=iterateTime+1


			if iterateTime%1000==0:
				print("iterateTime in routine: ",iterateTime," numChanged: ",numChanged)
				if self.__judegRoutineStop() : break


			if iterateTime>50000: 
				self.__constructW()
				break	
		
		if show==True: self.__draw()
		self.__judegRoutineStop()


	def __judegRoutineStop(self,erorrRate=0.1):
		self.__constructW()
		result_row = [sum([w_i*x_i for w_i,x_i in zip(self.w,points_item)])+self.b for points_item in self.points]
		result = [np.sign(resultValue)==self.label[index] for index,resultValue in enumerate(result_row)]
		error = result.count(False)/self.pointsNums
		print(error)
		if error<erorrRate: return True
		else: return False


	def __draw(self):
		X = np.arange(-10,210,10)
		Y = (-1)*(self.w[0]*X +self.b)/self.w[1]
		Y1 = (-1)*(self.w[0]*X +self.b +1)/self.w[1]
		Y2 = (-1)*(self.w[0]*X +self.b -1)/self.w[1]
		plt.axis([-10,210,-10,210])		
		plt.scatter(self.points_x_pos,self.points_y_pos,marker='o')
		plt.scatter(self.points_x_neg,self.points_y_neg,marker='x')
		plt.plot(X,Y,'b')
		plt.plot(X,Y1,'r')
		plt.plot(X,Y2,'r')
		plt.show()

	def __work4Draw(self):
		self.points_x_pos=[pointsItem[0] for index,pointsItem in enumerate(self.points) if index<self.boundary]
		self.points_y_pos=[pointsItem[1] for index,pointsItem in enumerate(self.points) if index<self.boundary]
		self.points_x_neg=[pointsItem[0] for index,pointsItem in enumerate(self.points) if index>=self.boundary]
		self.points_y_neg=[pointsItem[1] for index,pointsItem in enumerate(self.points) if index>=self.boundary]

	def init(self):
		self.__constructW()
		self.__constructErrorCache()
		self.__constructKernelMatrix(self.__linearKernelFun)


def test():
	obj = SvmTrain()
	obj.init()
	obj.routine(show=True)

if __name__=='__main__':
	test()

