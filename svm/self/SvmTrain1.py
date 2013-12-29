import jieba
import os
import os.path
import codecs
import pickle
import math
from random import randrange

class SvmTrain1:
	
	def __init__(self,posSamplePointList,negSamplePointList,C):
		self.posSamplePointList = posSamplePointList
		self.negSamplePointList = negSamplePointList
		self.C=C

	def __linearKernelFun(self,attr1,attr2):
		return sum([a*b for a,b in zip(attr1,attr2)])

	def __gaussKernelFun(self,attr1,attr2):
		return math.exp(sum([a*a for a in[item1-item2 for item1,item2 in zip(attr1,attr2)]]) * (-1) / 2)

	def __constructKernelMatrix(self,kernelFun):
		samplePoint = self.posSamplePointList + self.negSamplePointList
		self.kernelMatrix = [[kernelFun(samplePoint[i],samplePoint[index]) for index in range(len(samplePoint))] for i in range(len(samplePoint))]
	
	def __constructAlpahAndY(self):
		samplePoint = self.posSamplePointList + self.negSamplePointList
		self.alpha = [0 for i in range(len(samplePoint))]
		self.y =[1 if index<len(self.posSamplePointList) else 0 for index in range(len(samplePoint))]
		self.b = 0

	def __constructW(self):
		samplePoint = self.posSamplePointList + self.negSamplePointList
		self.w = [sum( [self.alpha[j]*1*samplePoint[j][i] if j<len(self.posSamplePointList) else self.alpha[j]*(-1)*samplePoint[j][i] for j in range(len(samplePoint))] ) for i in range(len(self.posSamplePointList[0]))]

	def __constructErrorCache(self):
		samplePoint = self.posSamplePointList + self.negSamplePointList
		self.E = [(sum([wi*xji for wi,xji in zip(self.w,samplePoint[j])])-1) if j<len(self.posSamplePointList) else (sum([wi*xji for wi,xji in zip(self.w,samplePoint[j])])+1) for j in range(len(samplePoint))]

	def __computeLAndH(self,index_1,index_2):
		a1 = self.alpha[index_1]
		a2 = self.alpha[index_2]
		y1 = self.y[index_1]
		y2 = self.y[index_2]
		C = self.C
		if y1!=y2:
			L = max(0,a2-a1)
			H = min(C,C+a2-a1)
		else:
			L = max(0,a1+a2-C)
			H = min(C,a1+a2)
		return L,H

	def __updateThreshold_b(self,index_1,index_2,a1_old,a1_new,a2_old,a2_new_cliped,L,H):
		b_old = self.b
		E1 = self.E[index_1]
		E2 = self.E[index_2]
		k11 = self.kernelMatrix[index_1][index_1]
		k12 = self.kernelMatrix[index_1][index_2]
		k22 = self.kernelMatrix[index_2][index_2]
		y1 = self.y[index_1]
		y2 = self.y[index_2]

		b1 = E1 + y1 *(a1_new-a1_old)*k11 + y2 *(a2_new_cliped-a2_old) * k12 + b_old
		b2 = E2 + y1 *(a1_new-a1_old)*k12 + y2 *(a2_new_cliped-a2_old) * k22 + b_old
		if a1_new >0 and a1_new <C:
			self.b = b1
		elif a2_new_cliped >0 and a2_new_cliped<C:
			self.b = b2
		else:
			self.b = (b1+b2)/2 

	def __updateE(self,nonBoundSet,index_1,index_2,a1_old,a1_new,a2_old,a2_new_cliped,b_old,b_new):
		y1 = self.y[index_1]
		y2 = self.y[index_2]

		for index,E_index in enumerate(self.E):
			if index not in nonBoundSet:
				continue
			else:
				if index==index_1 or index==index_2:
					self.E[index] = 0
				else:
					E_index_new = E_index + y1*(a1_new-a1_old)*self.kernelMatrix[index][index_1] + y2 *(a2_new_cliped - a2_old)*self.kernelMatrix[index][index_2] + b_old - b_new
					self.E[index] = E_index_new


	'''ObjectiveFun is w(a)'''
	def __computeObjectiveFun(self,E1,E2,k11,k12,k22,a1_old,a1_new,a2_old,a2_new_cliped,y1,y2,b_old):
		return a1_new+a2_new_cliped-0.5*k11*a1_new**2-0.5*k22*a2_new_cliped**2-y1*y2*k12*a1_new*a2_new_cliped-a1_new*y1*(y1+E1-a1_old*y1*k11-a2_old*y2*k12-b_old) -a2_new_cliped*y2*(y2+E2-a1_old*y1*k12-a2_old*y2*k22-b_old)



	def init(self):
		self.__constructAlpahAndY()
		self.__constructW()
		self.__constructErrorCache()
		self.__constructKernelMatrix(self.__linearKernelFun)

	def takestep(self,index_1,index_2,nonBoundSet):
		if index_1 == index_2: return 0
		a1_old = self.alpha[index_1]
		a2_old = self.alpha[index_2]
		y1 = self.y[index_1]
		y2 = self.y[index_2]
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
		self.__updateE(nonBoundSet,index_1,index_2,a1_old,a1_new,a2_old,a2_new_cliped,b_old,b_new)
		self.alpha[index_1] = a1_new
		self.alpha[index_2] = a2_new_cliped
		return 1

	def examineExample(self,index_2,nonBound):
		y2 = self.y[index_2]
		a2 = self.alpha[index_2]
		E2 = self.E[index_2]
		r2 = E2 * y2
		C = self.C

		if (r2<0 and a2<C) or (r2>0 and a2>0):
			if len(nonBound)>1:
				maxValue =-1
				maxIndex =-1
				for index in nonBound:
					temp =abs(self.E[index]-E2)
					if temp > maxValue:
						maxValue = temp
						maxIndex = index
				if self.takestep(index,index_2,nonBound):return 1

			if len(nonBound)!=0:
				begin = randrange(len(nonBound))
				for index in nonBound[begin:]:
					if self.takestep(index,index_2,nonBound):return 1 
				for index in nonBound[:begin]:
					if self.takestep(index,index_2,nonBound):return 1 
			else:
				begin = randrange(len(self.alpha))
				indexs = [i for i in range(len(self.alpha))]
				for index in indexs[begin:]:
					if self.takestep(index,index_2,nonBound):return 1 
				for index in indexs[:begin]:
					if self.takestep(index,index_2,nonBound):return 1 

		return 0

	def routine(self):
		numChanged = 0
		examimeAll = 1
		allPoint = [i for i in range(len(self.posSamplePointList + self.negSamplePointList))]
		nonBound = allPoint
		iterateTime = 1
		C = self.C
		while(not (numChanged==0 and examimeAll==0)):
			numChanged = 0
			if examimeAll:
				for index_2 in allPoint:
					numChanged +=self.examineExample(index_2,nonBound)
			else:
				for index_2 in nonBound:
					numChanged +=self.examineExample(index_2,nonBound)

			if examimeAll:
				examimeAll = 0
			elif numChanged==0:
				examimeAll = 1
			
			nonBound = [index for index in range(len(self.posSamplePointList + self.negSamplePointList)) if self.alpha[index] != C and self.alpha[index]!=0]	
			

			print("iterateTime in routine: ",iterateTime," numChanged: ",numChanged,"nonBound size: ",len(nonBound))
			print("alpha",self.alpha)
			iterateTime=iterateTime+1

			if iterateTime>1000: 
				self.__constructW()
				break
			


def test():
	posSamplePointList = [[randrange(50),randrange(50),randrange(50)] for i in range(3)]
	negSamplePointList = [[randrange(50,100),randrange(50,100),randrange(50,100)] for i in range(3)]
	trainObj = SvmTrain1(posSamplePointList,negSamplePointList,0.1)
	trainObj.init()
	print("pos",trainObj.posSamplePointList)
	print("neg",trainObj.negSamplePointList)
	print("alpha",trainObj.alpha)
	print("y",trainObj.y)
	print("b",trainObj.b)
	print("w",trainObj.w)
	print("e",trainObj.E)
	print("kernelMatrix",trainObj.kernelMatrix)
	print("**"*30)

	trainObj.routine()
	print("alpha",trainObj.alpha)
	print("y",trainObj.y)
	print("b",trainObj.b)
	print("w",trainObj.w)
	print("e",trainObj.E)


def test2():
	pos=[[44, 23, 5], [25, 23, 1], [23, 2, 4]]
	neg=[[54, 88, 79], [87, 66, 54], [69, 67, 99]]
	trainObj = SvmTrain1(pos,neg,0.1)
	trainObj.init()
	print("pos",trainObj.posSamplePointList)
	print("neg",trainObj.negSamplePointList)
	print("alpha",trainObj.alpha)
	print("y",trainObj.y)
	print("b",trainObj.b)
	print("w",trainObj.w)
	print("e",trainObj.E)
	print("kernelMatrix",trainObj.kernelMatrix)
	print("**"*30)

	trainObj.routine()
	print("alpha",trainObj.alpha)
	print("y",trainObj.y)
	print("b",trainObj.b)
	print("w",trainObj.w)
	print("e",trainObj.E)


def main():
	test2()


if __name__=='__main__':
	main()