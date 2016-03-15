
import java.util.*;
import java.io.*;

class Process implements Comparable<Process>{
	public int Arrival;
	public int CPU_burst;
	public int CPU_needed;
	public int IO_time;
	public int Turnaround;
	public int Finish;
	public int tWaiting;
	public int tIO;
	public String State;
	public Process(){}
	public void addA(int arrival){
		this.Arrival=arrival;
	}
	public void addB(int burst){
		this.CPU_burst=burst;
	}
	public void addC(int cpu){
		this.CPU_needed=cpu;
	}
	public void addIO(int io){
		this.IO_time=io;
	}
	public int getArrival(){
		return this.Arrival;
	}
	public int randomOS(int time) throws FileNotFoundException{
		Random rand=new Random();
		List<Integer> randnumlist=new ArrayList<Integer>();
		String rand_num_source="/Users/senongtor/Documents/random.txt";
		Scanner randnumreader = new Scanner(new FileInputStream(rand_num_source));
		while(randnumreader.hasNextLine()){
			String currnum=randnumreader.nextLine();
			randnumlist.add(Integer.parseInt(currnum));
		}
		randnumreader.close();
		int randomIndex = rand.nextInt(randnumlist.size());
		return  randnumlist.get(randomIndex)%time;
	}
	public int getCPU() throws FileNotFoundException{
		return randomOS(CPU_burst);
	}
	public int getIO() throws FileNotFoundException{
		return randomOS(IO_time);
	}
	public String toString(){
		return " "+Integer.toString(this.Arrival)+" "+Integer.toString(this.CPU_burst)+" "
	+Integer.toString(this.CPU_needed)+" "+Integer.toString(this.IO_time)+" ";
	}
	@Override
	public int compareTo(Process o) {
		return this.Arrival-o.Arrival;
	}
	public String printresult(){
		String result="";
//		result+="(A,B,C,IO) = "+ String.format("(%s,%s,%s,%s\n)", Arrival,);
		result+=String.format("Finishing time: %d\n", Finish);
		result+=String.format("Turnaround time: %d\n", Turnaround);
		result+=String.format("I/O time: %d\n", tIO);
		result+=String.format("Waiting time: %d\n", tWaiting);
		return result;
	}
}
public class Scheduler {
	
//	public void FCFS(){
//		Queue<Processor> readyqueue=new LinkedList<Processor>();
//		Queue<Processor> blockqueue=new LinkedList<Processor>();
//		Queue<Processor> runingqueue=new LinkedList<Processor>();
//	}
	public void printsimple(String algo,Queue<Process> unstarted){
		System.out.format("The scheduling algorithm used was %s\n",algo);
		int index=0;
		Iterator<Process> it=unstarted.iterator();
		
		while(!it.hasNext()){
			Process curr=it.next();
			
			System.out.print(curr);
			index+=1;
		}
	}
	
	public void printverbose(){
		System.out.println("This detailed printout gives the state and remaining burst for each process");
	}
	//Random number generator
	
	public static void main(String[] args) throws FileNotFoundException{
		String input="/Users/senongtor/Documents/input4.txt";
		PriorityQueue<Process> prcslist=new PriorityQueue<>();
		Queue<Process> unstarted=new LinkedList<Process>();
		Scheduler s=new Scheduler();
		Scanner reader = new Scanner(new FileInputStream(input));
		int num_prcsr=0;
		System.out.print("The original input was: ");
		//Take in num_prcsr and all the time values, print in order and 
		//put them in a priorityqueue for sorting and printing purpose.
		while(reader.hasNext()){
			num_prcsr = Integer.parseInt(reader.next());
			System.out.print(num_prcsr);
			while(num_prcsr>0){
				Process p=new Process();
				String curr=reader.next();
				System.out.print("  "+curr);
				p.addA(Integer.parseInt(curr));
				curr=reader.next();
				System.out.print(" "+curr);
				p.addB(Integer.parseInt(curr));
				curr=reader.next();
				System.out.print(" "+curr);
				p.addC(Integer.parseInt(curr));
				curr=reader.next();
				System.out.print(" "+curr);
				p.addIO(Integer.parseInt(curr));
				
				prcslist.add(p);	
				num_prcsr--;
			}
		}
		System.out.println();
		System.out.format("%s:  %d ", "The (sorted) input is", prcslist.size());
		while(!prcslist.isEmpty()){
			Process tmp=prcslist.poll();
			unstarted.offer(tmp);
			System.out.print(tmp);		
		}
		System.out.println();
		//----End of preprocessing.
		
		
		reader.close();
		//Start processing here with regard to the processlist from here!
		
		//Printing schema below
//		  if(args[0].equals("--verbose")){
//			  s.printverbose();
//		  }
		  s.printsimple("First Come First Serve", unstarted);
	}
}
