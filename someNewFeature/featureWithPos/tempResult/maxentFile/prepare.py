
def prepare(fileInName,fileOutName):
	with open(fileInName,"r") as fr,open(fileOutName,"w") as fw:
		[writeFile(fw,line) for line in fr]

def writeFile(fw,line):
	fw.write("T"+line.strip()[3:]+"\n")


def main():
	prepare("../trainFormat.out","./train.out")
	prepare("../testFormat.out","./test.out")

if __name__=="__main__":
	main()