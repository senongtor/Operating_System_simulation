/*
  Synopsis: A simulation of simplified scheduler.
  Class: Graduate Operating System 
  @author: Hongtao Cheng
  @Student ID: N15547756
  @Net ID: hc1817
*/
import java.util.*;
import java.io.*;

class Process implements Comparable<Process> {
	public int ID;
	public int Arrival;
	public int CPU_burst;
	public int CPU_needed;
	public int IO_time;
	public int Finishtime;
	public int tWaiting;
	public int tIO;
	public int trunning;
	public int Remaining;
	public int haverun;
	public int diffcurrcpu;
	public int totalin;
	public String State;
	public int currcpuleft;
	public int currioleft;

	public void addA(int arrival) {
		this.Arrival = arrival;
	}

	public void addB(int burst) {
		this.CPU_burst = burst;
	}

	public void addC(int cpu) {
		this.CPU_needed = cpu;
		this.Remaining = cpu;
	}

	public void addIO(int io) {
		this.IO_time = io;
	}

	public void setCPU(int currcpu) {
		currcpuleft = Math.min(currcpu, Remaining);
		if (currcpu > Remaining) {
			diffcurrcpu = currcpu - Remaining;
		}
	}

	public void setIO(int currio) {
		currioleft = currio;

	}

	public void setresumeCPU() {

	}

	public String toString() {
		return Integer.toString(this.Arrival) + " " + Integer.toString(this.CPU_burst) + " "
				+ Integer.toString(this.CPU_needed) + " " + Integer.toString(this.IO_time) + " ";
	}

	@Override
	public int compareTo(Process p) {
		if (this.Arrival - p.Arrival == 0) {
			return this.ID - p.ID;
		} else {
			return this.Arrival - p.Arrival;
		}
	}

	public int comparereverseto(Process p) {
		return -compareTo(p);
	}

	public int comparepenaltyratio(Process b) {
		double aa = (this.totalin) * 1.0 / Math.max(this.trunning, 1);
		double bb = (b.totalin) * 1.0 / Math.max(b.trunning, 1);
		if (Math.abs(aa - bb) < 0.0001) {
			return this.compareTo(b);
		} else {
			return (int) Math.signum(bb - aa);
		}
	}

	public void prcsdetailedresult(char ch) {
		String result = "";
		if (State.equals("running")) {
			if(ch=='r'){
				result += String.format("%3s %s  %d", " ", State, Math.min(2-haverun, currcpuleft));
			}
			else{
			result += String.format("%3s %s  %d", " ", State, currcpuleft + diffcurrcpu);
			}
		} else if (State.equals("blocked")) {
			result += String.format("%3s %s  %d", " ", State, currioleft);
		} else if (State.equals("ready") || State.equals("preempted")) {
			result += String.format("%5s %s  0", " ", "ready");
		} else if (State.equals("unstarted")) {
			result += String.format("  %s  0", State);
		} else if (State.equals("terminated")) {
			result += String.format(" %s  0", "terminated");
		}
		System.out.print(result);
	}

	public String printresult() {
		String result = "";
		result += String.format("%7s (A,B,C,IO) = (%s,%s,%s,%s)\n", " ", Arrival, CPU_burst, CPU_needed, IO_time);
		result += String.format("%7s Finishing time: %d\n", " ", Finishtime);
		result += String.format("%7s Turnaround time: %d\n", " ", Finishtime - Arrival);
		result += String.format("%7s I/O time: %d\n", " ", tIO);
		result += String.format("%7s Waiting time: %d\n", " ", tWaiting);
		return result;
	}
}

public class Scheduler {

	Scanner randnumreader;
	public String rand_num_source;
	public String currnum;

	public Scheduler() {
	}

	public int RandomOS(int time) {
		int number = 0;
		if (randnumreader.hasNextLine()) {
			currnum = randnumreader.nextLine();
			number = Integer.parseInt(currnum);
		}
		return 1 + number % time;
	}

	public void reset(LinkedList<Process> plist, Queue<Process> unstarted) {
		Iterator<Process> it = plist.iterator();
		while (it.hasNext()) {
			Process curr = it.next();
			curr.State = "unstarted";
			curr.Remaining = curr.CPU_needed;
			curr.tIO = 0;
			curr.tWaiting = 0;
			curr.Finishtime = 0;
			curr.trunning = 0;
			curr.totalin = 0;
			curr.haverun=0;
			
			unstarted.offer(curr);
		}

	}

	public void do_blocked(List<Process> blockqueue, LinkedList<Process> readyqueue, Comparator<Process> breaktie) {
		PriorityQueue<Process> pq = new PriorityQueue<Process>(10, breaktie);
		if (!blockqueue.isEmpty()) {
			Iterator<Process> it = blockqueue.iterator();
			while (it.hasNext()) {
				Process curr = it.next();
				// Had an issue removing an element while iterating the list
				// resolve by using it.remove instead of blockqueue.remove(curr)
				if (curr.currioleft == 1) {
					curr.tIO += 1;
					curr.State = "ready";
					pq.add(curr);
					it.remove();
				} else {
					curr.currioleft -= 1;
					curr.tIO += 1;
				}

				curr.Finishtime += 1;
			}
			while (!pq.isEmpty()) {
				readyqueue.addLast(pq.poll());
			}
		}
	}

	public void do_blocked_rr(List<Process> blockqueue, Comparator<Process> breaktie,
			PriorityQueue<Process> tempreadyqueue) {
		PriorityQueue<Process> pq = new PriorityQueue<Process>(10, breaktie);
		if (!blockqueue.isEmpty()) {
			Iterator<Process> it = blockqueue.iterator();
			while (it.hasNext()) {
				Process curr = it.next();
				// Had an issue removing an element while iterating the list
				// resolve by using it.remove instead of blockqueue.remove(curr)
				if (curr.currioleft == 1) {
					curr.tIO += 1;
					curr.State = "ready";
					pq.add(curr);
					it.remove();
				} else {
					curr.currioleft -= 1;
					curr.tIO += 1;
				}

				curr.Finishtime += 1;
			}
			while (!pq.isEmpty()) {
				tempreadyqueue.offer(pq.poll());
			}
		}
	}

	public void do_running(List<Process> blockqueue, Queue<Process> running, Queue<Process> terminated)
			throws FileNotFoundException {
		if (!running.isEmpty()) {

			if (running.peek().currcpuleft > 1) {
				running.peek().currcpuleft -= 1;
				running.peek().Remaining -= 1;
				running.peek().Finishtime += 1;
				running.peek().trunning += 1;
			} else {
				Process tmp = running.poll();
				if (tmp.Remaining == 1) {
					tmp.State = "terminated";
					tmp.Remaining -= 1;
					tmp.trunning += 1;
					tmp.haverun = 0;
					terminated.offer(tmp);
				} else {
					tmp.Remaining -= 1;
					tmp.trunning += 1;
					tmp.haverun = 0;
					tmp.setIO(RandomOS(tmp.IO_time));
					tmp.State = "blocked";
					blockqueue.add(tmp);
				}

				tmp.Finishtime += 1;
			}
		}

	}

	public void do_running_rr(List<Process> blockqueue, Queue<Process> running, Queue<Process> terminated,
			PriorityQueue<Process> tmpreadyqueue) throws FileNotFoundException {
		if (!running.isEmpty()) {
			running.peek().haverun += 1;
			if (running.peek().currcpuleft > 1) {
				running.peek().currcpuleft -= 1;
				running.peek().Remaining -= 1;
				running.peek().Finishtime += 1;
				if (running.peek().haverun == 2) {
					preempt(running, tmpreadyqueue);
				}
			} else {
				Process tmp = running.poll();
				if (tmp.Remaining == 1) {
					tmp.State = "terminated";
					tmp.Remaining -= 1;
					terminated.offer(tmp);
				} else {
					tmp.Remaining -= 1;
					tmp.setIO(RandomOS(tmp.IO_time));

					tmp.State = "blocked";
					blockqueue.add(tmp);
				}
				tmp.Finishtime += 1;
			}
		}
	}

	public void do_ready_rr(LinkedList<Process> readyqueue, Queue<Process> runningqueue,
			PriorityQueue<Process> tmpreadyqueue) throws FileNotFoundException {
		while (!tmpreadyqueue.isEmpty()) {
			readyqueue.addLast(tmpreadyqueue.poll());
		}
		if (!readyqueue.isEmpty()) {
			if (runningqueue.isEmpty()) {
				Process p = readyqueue.removeFirst();
				p.haverun = 0;
				if (p.State.equals("ready")) {
					p.setCPU(RandomOS(p.CPU_burst));
				}
				p.State = "running";

				runningqueue.offer(p);
			}

			Iterator<Process> it = readyqueue.iterator();
			while (it.hasNext()) {
				Process tmp = it.next();
				tmp.tWaiting += 1;

				tmp.Finishtime += 1;
			}
		}
	}

	public void do_arriving(int time, Queue<Process> unstarted, LinkedList<Process> readyqueue,
			Comparator<Process> breaktie) {
		PriorityQueue<Process> pq = new PriorityQueue<Process>(10, breaktie);
		while (!unstarted.isEmpty() && time == unstarted.peek().Arrival) {
			Process p = unstarted.poll();
			p.State = "ready";
			pq.add(p);
		}
		while (!pq.isEmpty()) {
			readyqueue.addLast(pq.poll());
		}
		Iterator<Process> it = unstarted.iterator();
		while (it.hasNext()) {
			Process tmp = it.next();
			tmp.Finishtime += 1;

		}
	}
    public void do_arriving_rr(int time, Queue<Process> unstarted, 
			Comparator<Process> breaktie,PriorityQueue<Process> tmpreadyqueue) {
		PriorityQueue<Process> pq = new PriorityQueue<Process>(10, breaktie);
		while (!unstarted.isEmpty() && time == unstarted.peek().Arrival) {
			Process p = unstarted.poll();
			p.State = "ready";
			pq.add(p);
		}
		while (!pq.isEmpty()) {
			tmpreadyqueue.add(pq.poll());
		}
		Iterator<Process> it = unstarted.iterator();
		while (it.hasNext()) {
			Process tmp = it.next();
			tmp.Finishtime += 1;

		}
	}
	public void do_ready(LinkedList<Process> readyqueue, Queue<Process> runningqueue, char datastruct, boolean isHPRN)
			throws FileNotFoundException {
		if (isHPRN) {
			Collections.sort(readyqueue, new Comparator<Process>() {
				public int compare(Process a, Process b) {
					return a.comparepenaltyratio(b);
				}
			});
		}
		if (!readyqueue.isEmpty()) {
			if (runningqueue.isEmpty()) {
				Process p = new Process();
				if (datastruct == 'q') {
					p = readyqueue.removeFirst();
				}
				if (datastruct == 's') {
					p = readyqueue.removeLast();
				}
				if (p.State.equals("ready")) {
					int num = RandomOS(p.CPU_burst);

					p.setCPU(num);
				}

				p.State = "running";

				runningqueue.offer(p);
			}

			Iterator<Process> it = readyqueue.iterator();
			while (it.hasNext()) {
				Process tmp = it.next();
				tmp.tWaiting += 1;
				tmp.Finishtime += 1;
			}
		}
	}

	public void preempt(Queue<Process> running, PriorityQueue<Process> tmpreadyqueue) {
		if (!running.isEmpty()) {
			Process tmp = running.poll();
			tmp.State = "preempted";
			tmp.haverun = 0;
			tmpreadyqueue.add(tmp);
		}
	}

	// FCFS and LCFS
	public void Driver(boolean simpleout, Queue<Process> unstarted, char ch) throws FileNotFoundException {
		double CPUutil = 0;
		double IOutil = 0;
		char datastruct = 'q';
		boolean isHPRN = false;
		String algo = "First in first out";
		Comparator<Process> comparator = new Comparator<Process>() {
			public int compare(Process a, Process b) {
				return a.compareTo(b);
			}
		};
		PriorityQueue<Process> tmpreadyqueue = new PriorityQueue<Process>(10, comparator);
		if (ch == 'l') {
			datastruct = 's';
			algo = "Last in first out";
			comparator = new Comparator<Process>() {
				public int compare(Process a, Process b) {
					return a.comparereverseto(b);
				}
			};
		}
		if (ch == 'h') {
			algo = "Highest Penalty Ratio Next";
			isHPRN = true;
			comparator = new Comparator<Process>() {
				public int compare(Process a, Process b) {
					return a.comparepenaltyratio(b);
				}
			};
		}
		if (ch == 'r') {
			algo = "Round Robbin";
		}
		// for printing summary
		Process[] prcslist = new Process[unstarted.size()];
		Iterator<Process> it = unstarted.iterator();
		int index = 0;
		int cycle = 0;
		while (it.hasNext()) {
			prcslist[index++] = it.next();
		}
		LinkedList<Process> readyqueue = new LinkedList<Process>();
		List<Process> blockqueue = new LinkedList<Process>();
		Queue<Process> runningqueue = new LinkedList<Process>();
		Queue<Process> terminated = new LinkedList<Process>();
		if (!simpleout) {
			System.out.println("This detailed printout gives the state and remaining burst for each process\n");
		}
		while (!readyqueue.isEmpty() || !blockqueue.isEmpty() || !runningqueue.isEmpty()
				|| !unstarted.isEmpty() && terminated.size() != prcslist.length) {

			if (!simpleout) {
				printverbose(prcslist, cycle, ch);
			}

			for (Process p : prcslist) {
				if (p.State.equals("ready") || p.State.equals("running") || p.State.equals("blocked")) {
					p.totalin += 1;
				}
			}

			if (ch == 'r') {
				do_blocked_rr(blockqueue, comparator, tmpreadyqueue);
				do_running_rr(blockqueue, runningqueue, terminated, tmpreadyqueue);
				do_arriving_rr(cycle, unstarted,  comparator,tmpreadyqueue);
				do_ready_rr(readyqueue, runningqueue, tmpreadyqueue);
			} else {
				do_blocked(blockqueue, readyqueue, comparator);
				do_running(blockqueue, runningqueue, terminated);
				do_arriving(cycle, unstarted, readyqueue, comparator);
				do_ready(readyqueue, runningqueue, datastruct, isHPRN);
			}

			
			if (!runningqueue.isEmpty()) {
				CPUutil++;
			}

			if (!blockqueue.isEmpty()) {
				IOutil++;
			}
			cycle++;
		}
		// actual cycle times
		cycle = cycle - 1;
		printsimple(algo, prcslist, CPUutil * 1.0 / cycle, IOutil * 1.0 / cycle, cycle);
	}


	public void printsimple(String algo, Process[] prcslist, double CPUutil, double IOutil, int cycle) {

		int num_prcs = prcslist.length;
		int sumtnard = 0;
		int sumwt = 0;

		System.out.println();
		System.out.format("The scheduling algorithm used was %s\n", algo);
		System.out.println();

		for (int i = 0; i < prcslist.length; i++) {
			System.out.format("Process %d:\n", i);
			System.out.print(prcslist[i].printresult());
			sumtnard += prcslist[i].Finishtime - prcslist[i].Arrival;
			sumwt += prcslist[i].tWaiting;
			System.out.println();
		}

		System.out.println("Summary Data:");
		System.out.format("%7s Finishing time: %d\n", " ", cycle);
		System.out.format("%7s CPU Utilization: %f\n", " ", CPUutil);
		System.out.format("	I/O Utilization: %f\n", IOutil);
		System.out.format("	Throughput: %f processes per hundred cycles\n", 100.0 * num_prcs / cycle);
		System.out.format("	Average turnaround time: %f\n", sumtnard * 1.0 / num_prcs);
		System.out.format("	Average waiting time: %f\n", sumwt * 1.0 / num_prcs);
		System.out.println("===============================================================================");
	}

	public void printverbose(Process[] prcslist, int cycle, char ch) {
		System.out.format("Before Cycle   %2d: ", cycle);
		for (Process p : prcslist) {
			p.prcsdetailedresult(ch);
		}
		System.out.print(".\n");
	}

	public static void main(String[] args) throws FileNotFoundException {

		// CHANGE!!-------------------------
		boolean simpleout = true;
		String input="";
		if(args[0].equals("--verbose")){
		simpleout=false;
		input=args[1];
		}
		else{
			input=args[0];
		}

		PriorityQueue<Process> prcslist = new PriorityQueue<>();
		LinkedList<Process> ordered = new LinkedList<Process>();
		Scheduler s = new Scheduler();
		//-----------------------------------
		//Please change the path to where the random number text is.
		s.rand_num_source= "/Users/XXXXXXXXXXX/Downloads/Lab2/random.txt";
		Scanner reader = new Scanner(new FileInputStream(input));
		int num_prcsr = 0;
		System.out.print("The original input was: ");
		// Take in num_prcsr and all the time values, print in order and
		// put them in a priorityqueue for sorting and printing purpose.
		while (reader.hasNext()) {
			num_prcsr = Integer.parseInt(reader.next());
			System.out.print(num_prcsr);
			int index = 0;
			while (num_prcsr > 0) {
				Process p = new Process();
				p.ID = index++;
				String curr = reader.next();
				System.out.print("  " + curr);
				p.addA(Integer.parseInt(curr));
				curr = reader.next();
				System.out.print(" " + curr);
				p.addB(Integer.parseInt(curr));
				curr = reader.next();
				System.out.print(" " + curr);
				p.addC(Integer.parseInt(curr));
				curr = reader.next();
				System.out.print(" " + curr);
				p.addIO(Integer.parseInt(curr));

				prcslist.add(p);
				num_prcsr--;
			}
		}
		System.out.println();
		System.out.format("%s:  %d ", "The (sorted) input is", prcslist.size());
		int index = 0;
		while (!prcslist.isEmpty()) {
			Process tmp = prcslist.poll();
			tmp.State = "unstarted";
			tmp.ID = index++;
			tmp.Finishtime = 0;
			ordered.addLast(tmp);
			System.out.print(" ");
			System.out.print(tmp);
		}

		Queue<Process> unstarted = new LinkedList<Process>(ordered);
		System.out.println("\n");
		reader.close();
		// End of preprocessing.

		char[] mapper = new char[] {'f','r','l','h' };
		for (char ch : mapper) {		
			s.randnumreader = new Scanner(new FileInputStream(s.rand_num_source));
			s.Driver(simpleout, unstarted, ch);
			s.randnumreader.close();
			s.reset(ordered, unstarted);
		}

	}
}
