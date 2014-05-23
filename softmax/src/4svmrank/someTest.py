import pymongo
from pymongo import MongoClient

# 1.这是我测试下，我在mongo中查询出来的Object在python是什么类型，结果就是类型是dict
# 2.同时也测试了这个dict类型的obj的key和value的type都是str的
def test():
	client = MongoClient('localhost',44444)
	db = client['trainData2']
	collection1 = db['collection1']
	obj = collection1.find_one()

	print(type(obj))
	print('ddd')
	print(obj['2'])
	print(type(obj['2']))
	print(int(obj['2']))

if __name__=='__main__':
	test()