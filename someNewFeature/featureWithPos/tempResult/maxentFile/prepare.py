
def prepare(fileInName,fileOutName):
	with open(fileInName,"r") as fr,open(fileOutName,"w") as fw:
		[writeFile(fw,line) for line in fr]

def writeFile(fw,line):
	if line[0]=="+":
		fw.write("T ")
		[fw.write(str(index)+"_"+token+" ") for index,token in enumerate((line.strip()[4:]).split()) if token!='0']
		# fw.write("T"+line.strip()[3:]+"\n")
		fw.write("\n")
	else:
		fw.write("F ")
		[fw.write(str(index)+"_"+token+" ") for index,token in enumerate((line.strip()[4:]).split()) if token!='0']
		# fw.write("T"+line.strip()[3:]+"\n")
		fw.write("\n")		

def main():
	prepare("../trainFormat.out","./train.out")
	prepare("../testFormat.out","./test.out")

if __name__=="__main__":
	main()