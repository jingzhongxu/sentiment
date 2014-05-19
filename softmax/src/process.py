# 这个脚本是我将数据库重新处理的，由于在训练数据的时候，如果是顺序的，那就数据训练出来之后就会偏向最后一个lable，所以我要随机化处理，
# 但随机化处理需要各类别样本数量大概相等，没有办法向不随进一样控制数量，所以我建立一个新的数据库，这个数据库的样本数量是固定的

import pymongo
from pymongo import MongoClient


def process():
	client = MongoClient('localhost',44444)
	trainData = client['trainData']
	collection1 = trainData['collection1']
	collection2 = trainData['collection2']
	collection3 = trainData['collection3']
	collection4 = trainData['collection4']
	collection5 = trainData['collection5']
	# print(collection1.count())

	trainData_2 = client['trainData2']
	collection1_trainData_2 = trainData_2['collection1']
	collection2_trainData_2 = trainData_2['collection2']
	collection3_trainData_2 = trainData_2['collection3']
	collection4_trainData_2 = trainData_2['collection4']
	collection5_trainData_2 = trainData_2['collection5']
	processOne(collection1,collection1_trainData_2,50000,1)
	processOne(collection2,collection2_trainData_2,50000,2)
	processOne(collection3,collection3_trainData_2,50000,3)
	processOne(collection4,collection4_trainData_2,50000,4)
	processOne(collection5,collection5_trainData_2,50000,5)


def processOne(collectionGet,collectionSave,nums,lable):
	cursor = collectionGet.find()
	for num,obj in enumerate(cursor):
		if num < 50000:
			collectionSave.insert(obj)
			print('process collection{0}  {1} lines'.format(lable,num),end='\r\t')
		else:
			break;
	print('finish single collection{}  process'.format(lable))			



if __name__ == '__main__':
	process()


