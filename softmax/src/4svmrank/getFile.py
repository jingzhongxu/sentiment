import pymongo
from pymongo import MongoClient

def mongo2File(filename):
	# 初始化mongo
	client = MongoClient("localhost",44444)
	db = client['trainData2']
	collection1_trainData_2 = db['collection1']
	collection2_trainData_2 = db['collection2']
	collection3_trainData_2 = db['collection3']
	collection4_trainData_2 = db['collection4']
	collection5_trainData_2 = db['collection5']

	with open(filename,"w") as fw:
		work4EachColl(fw,collection1_trainData_2,500,1)
		work4EachColl(fw,collection2_trainData_2,500,2)
		work4EachColl(fw,collection3_trainData_2,500,3)
		work4EachColl(fw,collection4_trainData_2,500,4)
		work4EachColl(fw,collection5_trainData_2,500,5)

def work4EachColl(fw,collection,nums,label):
	cursor = collection.find()
	for obj in cursor:
		fw.write(str(label) + " " + "qid:1 ")
		[fw.write(str(i+1) + ":" + obj[str(i+1)] + " ") for i in range(nums) if int(obj[str(i+1)]) != 0]
		fw.write("\n")


if __name__ == '__main__':
	mongo2File('trainFile.txt')


