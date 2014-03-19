import prepare
import math
import datetime
import pickle

def init(testRoot,sentimentFile):
	posParagraphList,negParagraphList,sentimentWord = prepare.readCorpus(testRoot,sentimentFile)
	allFeatures,boundryTwoClass= prepare.word2Feature(posParagraphList,negParagraphList,sentimentWord)
	return sentimentWord,allFeatures,boundryTwoClass


def __consturctParameters(length):
	return [0 for i in range(length*2)]


def __computeC(allFeatures):
	temp = [sum(features) for features in allFeatures]
	return max(temp)


def __work4ComputePx(features_x,features_y):
	flag = True
	for x,y in zip(features_x,features_y):
		if x==y:
			continue
		else:
			flag = False
			break
	if flag : return True
	else: return False

# compute prior probablity of p(x).Return the result of all sample points in a list,every element is p for the same index of sample point
def __computePx(allFeatures):
	N = len(allFeatures)
	temp = [[__work4ComputePx(features_x,features_y) for features_y in allFeatures] for features_x in allFeatures]
	result = [t.count(True) for t in temp]
	return [result_ele/N for result_ele in result]




def __work4ComputePxy(features_x,label_x,features_y,label_y):
	if label_x!=label_y: return False
	else:
		return __work4ComputePx(features_x,features_y)

#compute prior probablity of p(x,y).Return the result of all sample points in a list,every element is p for the same index of sample point
def __computePxy(allFeatures,boundryTwoClass):
	temp_out=[]
	N = len(allFeatures)
	for index1,features_x in enumerate(allFeatures):
		if index1 < boundryTwoClass: label_x=1
		else: label_x=0
		temp_in=[]
		for index2,features_y in enumerate(allFeatures):
			if index2 < boundryTwoClass: label_y=1
			else:label_y=0
			temp_in.append(__work4ComputePxy(features_x,label_x,features_y,label_y))
		temp_out.append(temp_in)		
	result = [t.count(True) for t in temp_out]
	return [result_ele/N for result_ele in result]


# compute Epfi(empirical expected). Return the ith features Epf(x,y). Epfi(x,y)=sum(p(x,y)*fi(x,y)) for all x,y 
def __computeEpfi(index_f,allFeatures,pxy):
	return sum([pxy_xy*features[index_f]for pxy_xy,features in zip(pxy,allFeatures)])

# compute Z(x).It is used in compute p(y|x) in every iterator step
def __computeZx(parameters,features_pos,features_neg):
	posResult = math.exp(sum([parameter*oneFeature for parameter,oneFeature in zip(parameters,features_pos)]))
	negResult = math.exp(sum([parameter*oneFeature for parameter,oneFeature in zip(parameters,features_neg)]))
	return posResult+negResult

# compute Epfi for the constraint which value must equals to Epfi(empirical expected)
def __computeEpfiConstraint(index_f,allFeatures,px,boundryTwoClass,sentimentWordLength,parameters,pYGivenX_all):
	# if index_f%1000==0:print("index_f",index_f)
	EpfiList = []
	for index,features in enumerate(allFeatures):
		px_x = px[index]
		py1,py0 = pYGivenX_all[index]
		if index<boundryTwoClass:
			# py1,py0 = __computePYGivenXInTrain(parameters,features,1,sentimentWordLength)
			EpfiList.append(px_x*py1*features[index_f])
			# print("index<boundryTwoClass: ",py1,py0)
		else:
			# py1,py0 = __computePYGivenXInTrain(parameters,features,0,sentimentWordLength)
			EpfiList.append(px_x*py0*features[index_f])
			# print("index>boundryTwoClass: ",py1,py0)
	return sum(EpfiList)


# compute p(y|x).In training process
def __computePYGivenXInTrain(parameters,features,label,sentimentWordLength):
	if label == 1:
		pos = features
		neg = features[sentimentWordLength:] + features[:sentimentWordLength]
	elif label == 0:
		neg =features
		pos = features[sentimentWordLength:] + features[:sentimentWordLength]
	# Z = __computeZx(parameters,pos,neg)
	py1 = math.exp(sum([parameter*oneFeature for parameter,oneFeature in zip(parameters,pos)])) 
	py0 = math.exp(sum([parameter*oneFeature for parameter,oneFeature in zip(parameters,neg)])) 
	Z = py1+py0
	return py1/Z,py0/Z

# if parameters updated,then we can compute the p(y|x) for all samples
def __computePYGivenXInTrain4AllSample(parameters,allFeatures,sentimentWordLength,boundryTwoClass):
	result = [__computePYGivenXInTrain(parameters,features,1,sentimentWordLength) if index<boundryTwoClass else __computePYGivenXInTrain(parameters,features,0,sentimentWordLength) for index,features in enumerate(allFeatures)]
	return result

def __computeEpfConstraint(allFeatures,px,boundryTwoClass,sentimentWordLength,parameters,pYGivenX_all):
	Epf_constraint = [__computeEpfiConstraint(index_f,allFeatures,px,boundryTwoClass,sentimentWordLength,parameters,pYGivenX_all) for index_f in range(len(parameters))]
	return Epf_constraint



def maxEntTrain(sentimentWord,allFeatures,boundryTwoClass,iterNums):
	px = __computePx(allFeatures)
	pxy = __computePxy(allFeatures,boundryTwoClass)
	C = __computeC(allFeatures)
	parameters = __consturctParameters(len(sentimentWord))
	Epf_empricalExpected = [__computeEpfi(index,allFeatures,pxy) for index in range(len(allFeatures[0]))]

	iters = 1
	while iters<iterNums:
		print("iterator ",iters)
		print("time: ",datetime.datetime.now())
		pYGivenX_all = __computePYGivenXInTrain4AllSample(parameters,allFeatures,len(sentimentWord),boundryTwoClass)
		Epf_constraint = __computeEpfConstraint(allFeatures,px,boundryTwoClass,len(sentimentWord),parameters,pYGivenX_all)
		for index,parameter in enumerate(parameters):
			if Epf_empricalExpected[index]!=0: parameters[index] = parameter + (1/C)*math.log(Epf_empricalExpected[index] / Epf_constraint[index])
		iters = iters+1
	return parameters

def pickleParameters(filename,parameters):
	with open(filename,"wb") as fwb:
		pickle.dump(parameters,fwb)


def work(iterNums):
	print("init work! ",datetime.datetime.now())
	sentimentWord,allFeatures,boundryTwoClass = init("./corpus/train","./corpus/fre4train.txt")
	print("prepare work finished ",datetime.datetime.now())
	print("training model... ", datetime.datetime.now())
	parameters = maxEntTrain(sentimentWord,allFeatures,boundryTwoClass,iterNums)
	print("training finished!.. ",datetime.datetime.now())
	print("pickled parameters.. ",datetime.datetime.now())
	pickleParameters("./trainResult",parameters)
	print("pickleParameters finished ",datetime.datetime.now())

def __testComputePx():
	print("computePx worked!")
	feature1 = [[x for x in range(3)] for y in range(3)]
	feature2 = [[x+1 for x in range(3)] for y in range(3)]	
	feature3 = [[x+2 for x in range(3)] for y in range(2)]
	print(feature1)
	print(feature2)
	print(feature3)
	features = feature1+feature2 + feature3
	print(__computePx(features))

def __testComputePxy():
	print("computePxy worked!")
	feature1 = [[x for x in range(3)] for y in range(3)]
	feature2 = [[x+1 for x in range(3)] for y in range(3)]	
	feature3 = [[x+2 for x in range(3)] for y in range(2)]
	print(feature1)
	print(feature2)
	print(feature3)
	features = feature1+feature2 + feature3
	print("boundryTwoClass value: ",1)
	print(__computePxy(features,1))

def __testComputeEpfExpect():
	allFeatures = [[1,0,0],[0,1,0],[1,0,1],[1,0,0]]
	pxy=__computePxy(allFeatures,0)
	print(pxy)
	Epf = [__computeEpfi(index,allFeatures,pxy) for index in range(len(allFeatures[0]))]
	print(Epf)




def __testAll():
	sentimentWord,allFeatures,boundryTwoClass = init("./corpus/all1","./corpus/fre4train.txt")
	print("allFeatures length: ",len(allFeatures))
	px = __computePx(allFeatures)
	print("px length: ",len(px))
	print(px)

	pxy = __computePxy(allFeatures,boundryTwoClass)
	print("pxy length: ",len(pxy))
	print(pxy)

	C = __computeC(allFeatures)
	print("C valuse: ",C)

	Epi_empricalExpected = [__computeEpfi(i,allFeatures,pxy) for i in range(len(allFeatures[0]))]
	print("Epi_empricalExpected length: ",len(Epi_empricalExpected))
	print("Epi_empricalExpected non zero: ",[index for index in range(len(Epi_empricalExpected)) if Epi_empricalExpected[index]!=0])
	print("allFeatures non zero: ",[[index_2 for index_2 in range(len(allFeatures[index_1])) if allFeatures[index_1][index_2]!=0] for index_1 in range(len(allFeatures))])

	parameters = __consturctParameters(len(sentimentWord))
	print("parameters length: ",len(parameters))

	pYGivenX_all = __computePYGivenXInTrain4AllSample(parameters,allFeatures,len(sentimentWord),boundryTwoClass)

	Epf_constraint = __computeEpfConstraint(allFeatures,px,boundryTwoClass,len(sentimentWord),parameters,pYGivenX_all)
	print("Epf_constraint length: ",len(Epf_constraint))	
	print("Epf_constraint non zero: ",[index for index in range(len(Epf_constraint)) if Epf_constraint[index]!=0])



def main():
	# __testComputePx()
	# print("***"*20)
	# __testComputePxy()
	# print("***"*20)
	__testComputeEpfExpect()
	print("***"*20)

	__testAll()

if __name__=='__main__':
	work(30)