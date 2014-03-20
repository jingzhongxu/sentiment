def prepare(fileInName,fileOutName):
	with open(fileInName,"r") as fr,open(fileOutName,"w") as fw:
		[writePosFile(fw,line) if line[0]=="+" else writeNegFile(fw,line) for index,line in enumerate(fr)]

def writePosFile(fw,line):
	fw.write(line.strip()[4:]+"\n")
	fw.write(str(1)+"\n")

def writeNegFile(fw,line):
	fw.write(line.strip()[4:]+"\n")
	fw.write(str(0)+"\n")


def main():
	prepare("../trainFormat.out","./train.out")
	prepare("../testFormat.out","./test.out")

if __name__=="__main__":
	main()