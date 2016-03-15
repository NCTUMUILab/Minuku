import os

totalline = 0

for root, dirs, files in os.walk("."):
    for file in files:
        if file.endswith(".java"):

        	f = open (os.path.join(root, file))
        	count = 0

        	for line in f:
        		count +=1
        	
        	totalline += count
        	print (os.path.join(root, file)), count 	


print totalline





