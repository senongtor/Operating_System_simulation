To run Scheduler.java, simply run the program via the terminal with "--verbose" (optional, will print out details of each process at each iteration), the path of the input file as args. You can only test one input file at one time and for each input file the order of printing out the result is FCFS, Round Robbin, LCFS, HPRN. Different results for each algorithm are seperated by "========". Also, in order to read the random numbers from the random.txt, please change the path of s.rand_num_source on line 526 to the path where random.txt is at on your machine.
	
Ex(On Unix), 
   javac Scheduler.java
   java mylinker /Users/usersname/Downloads/input1.txt OR
   java mylinker --verbose /Users/usersname/Downloads/input1.txt 

And the result will be printed!