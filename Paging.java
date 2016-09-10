package DemandPaging;

import java.util.*;
import java.io.*;
import java.text.*;

/**
 * Created by Hongtao on 4/18/16.
 */

/**
 * Process class.
 * 
 * @author Hongtao
 *
 */
class Process {
	int size;
	int numfaults;
	int residency;
	int nextref;
	int num_evict;
	int num_ref;
	int currrand;

	public Process() {
	}

	public Process(int size, int prcsnum, int num_ref) {
		this.size = size;
		nextref = 111 * prcsnum % size;
		this.num_ref = num_ref;
		this.num_evict = 0;

	}

	public void findnextref(double A, double B, double C, Scanner random) {
		currrand = random.nextInt();
		double y = currrand / (1d + Integer.MAX_VALUE);
		if (y < A) {
			nextref = (nextref + 1) % size;
		} else if (y < A + B) {
			nextref = (nextref - 5 + size) % size;
		} else if (y < A + B + C) {
			nextref = (nextref + 4) % size;
		} else {
			nextref = random.nextInt() % size;
		}

	}

	public void getrandnum() {
		System.out.format("I use random number: %d\n", currrand);
	}

	public int getnextpage(int pagesize) {
		return nextref / pagesize;
	}

	public int getoffset(int pagesize) {
		return nextref % pagesize;
	}

	public double getavgres() {
		return residency * 1.0 / num_evict;
	}
}

class Frame {
	int pagenums;
	int prcsnum;
	int loadtime;
	int touchtime;
	int num;

	public Frame() {
	}

	public Frame(int pagenums, int prcsnum, int time, int num) {
		this.pagenums = pagenums;
		this.prcsnum = prcsnum;
		this.loadtime = time;
		this.touchtime = time;
		this.num = num;
	}

	public String toString() {
		String str = String.format("Pagenum: %d,Prcsnum: %d, Loadtime: %d, Touchtime: %d,Framenum: %d", pagenums,
				prcsnum, loadtime, touchtime, num);
		return str;
	}
}

public class Paging {
	// Define Quantum
	public static final int Q = 3;

	public boolean hasfault(List<Frame> framelist, int pagenum, int prcsnum) {
		for (int i = 0; i < framelist.size(); i++) {
			if (framelist.get(i).pagenums == pagenum && framelist.get(i).prcsnum == prcsnum) {
				return false;
			}
		}
		return true;
	}

	public void updatetouchtime(List<Frame> framelist, int pagenum, int prcsnum, int time) {
		for (int i = 0; i < framelist.size(); i++) {
			if (framelist.get(i).pagenums == pagenum && framelist.get(i).prcsnum == prcsnum) {
				framelist.get(i).touchtime = time;
			}
		}
	}

	public boolean allfinished(List<Process> prcslist) {
		for (int i = 0; i < prcslist.size(); i++) {
			if (prcslist.get(i).num_ref != 0) {
				return false;
			}
		}
		return true;
	}

	public void runner(int num_ref, int pagesize, List<Process> prcslist, Scanner scanner, int Q, int num_frames,
			double[] A, double[] B, double[] C, String algo) {
		int pagetoref = 0;
		int time = 1;
		List<Frame> inframelist = new ArrayList<Frame>();
		// Tracking the available frame number.
		LinkedList<Integer> freeframenumlist = new LinkedList<Integer>();

		// Add the initial available frame numbers in descending order. (Eg.
		// 9,8,7,6...0).
		for (int k = num_frames; k >= 1; k--) {
			freeframenumlist.addLast(k - 1);
		}
		
		while (!allfinished(prcslist)) {
			for (int i = 0; i < prcslist.size(); i++) {
				//If for a process, there is less than a Quantum references left,
				//adjust its reference time.
				int upper = 0;
				if (prcslist.get(i).num_ref >= Q) {
					upper = Q;
				} else {
					upper = prcslist.get(i).num_ref;
				}
				for (int j = 0; j < upper; j++) {
					//Obtain the next page to refer.
					pagetoref = prcslist.get(i).getnextpage(pagesize);
					if (hasfault(inframelist, pagetoref, i)) {
						if (!freeframenumlist.isEmpty()) {
							//If we still have free frames, we can load them into memory.
							int framenum = freeframenumlist.removeFirst();
							Frame tmpfr = new Frame(pagetoref, i, time, framenum);

							inframelist.add(tmpfr);

						} else {
							if (algo.equals("lru")) {
								//Sort the in memory frame list by its touch time
								Collections.sort(inframelist, new Comparator<Frame>() {
									@Override
									public int compare(Frame o1, Frame o2) {
										return o1.touchtime - o2.touchtime;
									}
								});

								//Now that the frame with smallest touch time is at head,
								//evict its page of its corresponding process, update new 
								//process and its page
								prcslist.get(inframelist.get(0).prcsnum).residency += time
										- inframelist.get(0).loadtime;

								prcslist.get(inframelist.get(0).prcsnum).num_evict++;

								inframelist.get(0).pagenums = pagetoref;
								inframelist.get(0).loadtime = time;
								inframelist.get(0).touchtime = time;
								inframelist.get(0).prcsnum = i;
							} else if (algo.equals("fifo")) {
								
								prcslist.get(inframelist.get(0).prcsnum).residency += time
										- inframelist.get(0).loadtime;

								prcslist.get(inframelist.get(0).prcsnum).num_evict++;
								inframelist.get(0).pagenums = pagetoref;
								inframelist.get(0).loadtime = time;
								inframelist.get(0).touchtime = time;
								inframelist.get(0).prcsnum = i;
								Frame tmplastframe = inframelist.get(0);
								inframelist.remove(0);
								inframelist.add(tmplastframe);
							}

							else if (algo.equals("random")) {
								Collections.sort(inframelist, new Comparator<Frame>() {
									public int compare(Frame a, Frame b) {
										return a.num - b.num;
									}
								});
								//Pick a random one in the in memory frame list.
								//Then find its corresponding process/page.
								int randnow = scanner.nextInt();
								int randindex = randnow % num_frames;

								prcslist.get(inframelist.get(randindex).prcsnum).residency += time
										- inframelist.get(randindex).loadtime;

								prcslist.get(inframelist.get(randindex).prcsnum).num_evict++;

								inframelist.get(randindex).pagenums = pagetoref;
								inframelist.get(randindex).loadtime = time;
								inframelist.get(randindex).touchtime = time;
								inframelist.get(randindex).prcsnum = i;
								Frame tmplastframe = inframelist.get(randindex);
								inframelist.remove(randindex);
								inframelist.add(tmplastframe);
							}
						}
						prcslist.get(i).numfaults += 1;
					} else {
						updatetouchtime(inframelist, pagetoref, i, time);
					}
					time++;
					prcslist.get(i).num_ref--;
					prcslist.get(i).findnextref(A[i], B[i], C[i], scanner);
				}
			}

		}
	}

	/**
	 * Load ABC for each process.
	 * @param A
	 * @param B
	 * @param C
	 * @param num
	 * @param J
	 */
	public void loadabc(double[] A, double[] B, double[] C, int num, int J) {
		if (J == 1 || J == 2 || J == 3) {
			for (int i = 0; i < num; i++) {

				B[i] = 0;
				C[i] = 0;

				A[i] = J == 3 ? 0 : 1;
			}
		} else {
			A[0] = 0.75;
			B[0] = 0.25;
			C[0] = 0;
			A[1] = 0.75;
			B[1] = 0;
			C[1] = 0.25;
			A[2] = 0.75;
			B[2] = 0.125;
			C[2] = 0.125;
			A[3] = 0.5;
			B[3] = 0.125;
			C[3] = 0.125;
		}

	}

	/**
	 * To format an calculation result.
	 * @param A Number
	 * @return
	 */
	public String fmt(double num) {
		DecimalFormat df = new java.text.DecimalFormat(".###############");
		String s = df.format(num);
		return s;
	}
    /**
     * Print final result.
     * @param prcslist
     */
	public void printresult(List<Process> prcslist) {
		int totalfaults = 0;
		int totalresidency = 0;
		int totalevict = 0;
		double avgtotalresidency = 0;
		String str = "";
		String residence = "";

		for (int i = 0; i < prcslist.size(); i++) {
			if (prcslist.get(i).num_evict != 0) {
				totalresidency += prcslist.get(i).residency;
				totalevict += prcslist.get(i).num_evict;
				str = String.format("%s %s", "and", fmt(prcslist.get(i).getavgres()));
				residence = "";
			} else {
				str = "\n    With no evictions, the";
				residence = " is undefined";
			}
			System.out.format("Process %d had %d faults %s average residence%s.\n", i + 1, prcslist.get(i).numfaults,
					str, residence);
			totalfaults += prcslist.get(i).numfaults;
		}
		System.out.println();
		//If there exists evictions, print out residency
		if (totalresidency > 0) {
			avgtotalresidency = 1.0 * totalresidency / totalevict;
			System.out.format("The total number of faults is %d and the overall average residence is %s.", totalfaults,
					fmt(avgtotalresidency));
		} else {
			System.out.format(
					"The total number of faults is %d.\n With no evictions, the overall average residence is undefined.",
					totalfaults);

		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		Paging p = new Paging();
		int num_prcs = 0;
		Scanner scanner = new Scanner(new FileInputStream("/Users/Hongtao/Documents/NYU/OS/RandomNumber.txt"));
		Scanner inputreader = new Scanner(new FileInputStream("/Users/Hongtao/Documents/NYU/OS/input.txt"));
		int line = 0;
		String[] input = new String[7];
		while (inputreader.hasNext()) {
			String tmp = inputreader.nextLine();
			if (line == 9) {
				input = tmp.split(" ");
			}
			line++;
		}
		//Process list.
		List<Process> prcslist = new ArrayList<Process>();
		int machsize = Integer.parseInt(input[0]);
		int pagesize = Integer.parseInt(input[1]);
		int prcssize = Integer.parseInt(input[2]);
		int jobmix = Integer.parseInt(input[3]);
		int numrefperprcs = Integer.parseInt(input[4]);
		String algo = input[5];
		int leveldebugout = Integer.parseInt(input[6]);
		int num_frames = machsize / pagesize;
		if (jobmix == 1) {
			num_prcs = 1;
		} else if (jobmix == 2 || jobmix == 3 || jobmix == 4) {
			num_prcs = 4;
		}
		//For each process, there is a corresponding A,B and C. 
		//So I build 3 arrays to contains their A,B,C values.
		double A[] = new double[num_prcs];
		double B[] = new double[num_prcs];
		double C[] = new double[num_prcs];
		
		p.loadabc(A, B, C, num_prcs, jobmix);
		for (int i = 0; i < num_prcs; i++) {
			prcslist.add(new Process(prcssize, i + 1, numrefperprcs));
		}
		System.out.format("The machine size is %d\n", machsize);
		System.out.format("The page size is %d\n", pagesize);
		System.out.format("The process size is %d\n", prcssize);
		System.out.format("The job mix number is %d\n", jobmix);
		System.out.format("The number of reference per process is %d\n", numrefperprcs);
		System.out.format("The relpacement algorithm is %s\n", algo);
		System.out.format("The level of debugging out is %d\n\n", leveldebugout);

		p.runner(numrefperprcs, pagesize, prcslist, scanner, Q, num_frames, A, B, C, algo);
		p.printresult(prcslist);

	}
}
