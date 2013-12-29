from ProcessCorpus import ProcessCorpus
import pickle

def main():
	with open("./pickle/fre4train.pickle","rb") as fr:
		obj = pickle.load(fr)
	print(len(obj.posSamplePointList))

if __name__=='__main__':
	main()