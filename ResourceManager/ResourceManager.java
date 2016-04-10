import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * <P>The Task Class</p>
 * It contains the initialclaim array and total requested array.
 * They are represented in array so I will know which {@link Resource} it is refering to
 * It also has total waiting and total time taken field along with a status
 * variable indicating if it is aborted. 
 * @author Hongtao
 *
 */
class Task {
	int[] iniclaimperresource;
	int[] requestedperresource;
	int[] needed;
	int waitingtime;
	int timetaken;
	boolean aborted;

	public Task(int num_resources) {
		aborted = false;
		iniclaimperresource = new int[num_resources];
		requestedperresource = new int[num_resources];
		needed=new int[num_resources];
		Arrays.fill(requestedperresource, 0);
	}

	// For the purpose of printing the total units available next cycle after aborting
	public int gettotalrequested() {
		int sum = 0;
		for (int a : requestedperresource) {
			sum += a;
		}
		return sum;
	}

	public String toString() {
		// ******possible that timetakan will be zero.(Not handled)*********
		// String version of statistics of a task for output
		if (!aborted) {
			String re = String.format("%5s %d  %d  %.0f%s", " ", timetaken, waitingtime,
					waitingtime * 100.0 / timetaken, "%");
			return re;
		} else {
			// if it was aborted, the output will only showing "aborted".
			String re = String.format("%5s %s", " ", "aborted");
			return re;
		}
	}
}
/**
 * Resource class representing a resource dispatcher
 * It has totalunits it has upon reading of the input file
 * and unitsleft updated dynamically as well as 
 * the received field containing the released units it receives
 * in this cycle. 
 * @author Hongtao
 */
class Resource {
	int totalunits;
	int unitsleft;
	int received;

	public Resource() {
	}

	public Resource(int iniunits) {
		totalunits = iniunits;
		unitsleft = iniunits;
		received = 0;
	}
	//Update the units left for the usage of next cycle
	public void update() {
		unitsleft += received;
		received = 0;
	}

	public String toString() {
		return "Total units currently: " + unitsleft;
	}
}
/**
 * Action class representing a request or release or initialize or terminate
 * of a {@link Task}. It contains all the data read from the text file.
 * @author senongtor
 *
 */
class Action {
	// Action name: Eg,"request".
	String actionname;
	// Task index: Eg task 1.
	int tasknum;
	// Resource index: Eg resource 1.
	int resourcenum;
	// Delay
	int delay;
	// How many units of resource we want to operate on.
	int actionamount;

	public Action() {
	}

	public Action(String state, int tasknum, int delay, int resourcenum, int actionamout) {
		this.actionname = state;
		this.tasknum = tasknum;
		this.delay = delay;
		this.resourcenum = resourcenum;
		this.actionamount = actionamout;

	}

	public String toString() {
		String re = String.format("%s%4s%d %d %d %d", actionname, " ", tasknum, delay, resourcenum, actionamount);
		return re;
	}
}

public class ResourceManager {

	/**
	 *  Function to check if for an request action, its request can be proved 
	 *  by the corresponding resource
	 * @param action
	 * @param resourceslist
	 * @see Action
	 * @see Resource 
	 * @return
	 */
	public boolean requestacceptable(Action a, List<Resource> resourceslist) {
		if (a.actionamount <= resourceslist.get(a.resourcenum).unitsleft) {
			return true;
		}
		return false;
	}

	public void returnresources(List<Resource> resourceslist, Task finishedtask) {
		for (int l = 0; l < finishedtask.requestedperresource.length; l++) {
			resourceslist.get(l).received += finishedtask.requestedperresource[l];
		}
	}

	/**
	 *  If deadlock occurs, then abort the task with lowest index using the
	 *  deadrequestlist(indices are ordered from small to big in the list)
	 *  After releasing its units back to its corresponding resource, then clear
	 *  its actionlist and remove it from the deadrequestlist.
	 *  Because we want to know how mant deadlocked tasks are aborted so we can
	 *  know the alive tasks, the method returns the number of aborted tasks.
	 * @param deadrequestlist
	 * @param resourceslist
	 * @param actiontable
	 * @param tasklist
	 * @param indexmap
	 * @see ResourceManager#deadlocked(List, List, LinkedList)
	 * @return
	 */
	public int handledeadlock(LinkedList<Integer> deadrequestlist, List<Resource> resourceslist,
			List<LinkedList<Action>> actiontable, List<Task> tasklist, List<Integer> indexmap) {
		//We want to know which of the indices will be removed in the indexmap 
		//after the correponding tasks will be aborted
		Set<Integer> aborted = new HashSet<Integer>();
		int num_aborted = 0;
		while (deadlocked(actiontable, resourceslist, deadrequestlist)) {
			int leastindex = deadrequestlist.getFirst();

			aborted.add(leastindex);

			Action firstaction = actiontable.get(leastindex).getFirst();
			resourceslist.get(firstaction.resourcenum).received += tasklist
					.get(leastindex).requestedperresource[firstaction.resourcenum];
			tasklist.get(leastindex).aborted = true;
			actiontable.get(leastindex).clear();
			deadrequestlist.removeFirst();
			num_aborted++;
		}
		Iterator<Integer> it = indexmap.iterator();
		while (it.hasNext()) {
			if (aborted.contains(it.next())) {
				it.remove();
			}
		}
		return num_aborted;
	}

	/**
	 *  To check if deadlock is still there by looping over the deadrequests
	 *  and check if its amount of request to a resource can be satisfied 
	 *  (less or equal than the units left of the resource plus the release 
	 *  it received from other aborted )
	 * @param actionlist
	 * @param resourceslist
	 * @param deadrequestlist
	 * @return
	 */
	public boolean deadlocked(List<LinkedList<Action>> actionlist, List<Resource> resourceslist,
			LinkedList<Integer> deadrequestlist) {
		for (int i = 0; i < deadrequestlist.size(); i++) {
			int index = deadrequestlist.get(i);
			// Get the dead request action for task[index]
			Action curraction = actionlist.get(index).getFirst();
			// if this request now can be satisfied, then deadlock is unlocked.
			if (curraction.actionamount <= resourceslist.get(curraction.resourcenum).unitsleft
					+ resourceslist.get(curraction.resourcenum).received) {
				return false;
			}
		}
		return true;
	}

    /**
     * <p>Fifo algorithm.</p>
     * The way I am running the algorithm is that if a action can be accepted,
     * then it's removed from the head of linkedlist it is in. If a action
     * cannot be accepted,then keep it there.
     * Each time I am looping over the range of number of tasks, 
     * reading itsactionlist and try to execute each currernt action(which is the head of this linkedlist).
     * And for simplicity, I also count terminate as one seperate action 
     * so I don't need to end a task when I see a release followed by termination
     * The timetaken and waiting field in {@link ResourceManager#Task}task
     * @see Task
     * @see Resource
     * @see Action
     * @param resourceslist
     * @param tasklist
     * @param actiontable
     */
	public void optmanager(List<Resource> resourceslist, List<Task> tasklist, List<LinkedList<Action>> actiontable) {
		int cycle = 0;
		int num_tasks = tasklist.size();
		int num_resources = resourceslist.size();
		int num_termin = 0;
		int num_livetasks = num_tasks;
		LinkedList<Integer> deadrequestlist = new LinkedList<Integer>();
		// Indexmap to decide the order of execution of the loop,
		// i.e. which task's action should we consider first.
		List<Integer> indexmap = new ArrayList<Integer>();
		while (num_termin < num_tasks) {
			int deadrequest = 0;

			for (int k = 0; k < num_tasks; k++) {
				if (!indexmap.contains(k) && !(actiontable.get(k).isEmpty())) {
					indexmap.add(k);
					// add non blocked action indices to the indexmaplist.
					// Since in the previous round, we've added all the blocked
					// indices
					// and those that are still blocked stayed in the list, so
					// the
					// order of executing is what we want.
				}
			}
			
			//Update every resource's unitsleft given the received from previous cycle
			for (int m = 0; m < num_resources; m++) {
				resourceslist.get(m).update();
			}
			//Update the live tasks we are looking at
			num_livetasks = num_tasks - num_termin;
			
			for (int j = 0; j < num_livetasks; j++) {
				int i = indexmap.get(0);

				Action curraction = actiontable.get(i).getFirst();

				// The release each resource get is added to the received
				// field to each resource,
				// So at the next cycle, we want to update the unitsleft
				// field of the resource and reset received field to 0.

				if (curraction.delay == 0) {
					if (curraction.actionname.equals("initiate")) {
						actiontable.get(i).removeFirst();
					}
					if (curraction.actionname.equals("request")) {

						if (requestacceptable(curraction, resourceslist)) {

							tasklist.get(i).requestedperresource[curraction.resourcenum] += curraction.actionamount;
							resourceslist.get(curraction.resourcenum).unitsleft -= curraction.actionamount;
							actiontable.get(i).removeFirst();
						} else {
							// if the request can not be accepted, then add the
							// index of the task to
							// deadrequestlist, increment the number of
							// deadrequests.
							deadrequest++;
							deadrequestlist.addLast(i);

							indexmap.add(i);
							

							tasklist.get(i).waitingtime += 1;
						}
					} else if (curraction.actionname.equals("release")) {
					
						// If a task's action is release, then release the units
						// back to corresponding resource,
						// decrement the total request for that resource left in
						// the task object
						// then remove it from actionlist since it can be
						// processed.
						resourceslist.get(curraction.resourcenum).received += curraction.actionamount;
						tasklist.get(i).requestedperresource[curraction.resourcenum] -= curraction.actionamount;
						actiontable.get(i).removeFirst();

					} else if (curraction.actionname.equals("terminate")) {
						// If a task is terminated, we should actually give the
						// total left request amount back to its original resource. 
						// In this lab, the request left will be 0 eventually because
						// the total amount of request and the total amount of
						// release of a task are equal. So I ignored this step
						// because implementing this will require me to terminate a task
						// during the action just ahead of termination.

						actiontable.get(i).clear();
						num_termin++;
						tasklist.get(i).timetaken -= 1;
					}

				} else {
					curraction.delay--;
				}

				tasklist.get(i).timetaken += 1;
				// After the operation on current task, remove its index from
				// the indexmap,
				// so next time we are still getting the head of the indexmap
				// and eventually
				// after looping over all the live tasks indices in the
				// indexmap, we will
				// clear the old map but with the adding of blocked tasks, our
				// indexmap will
				// contain all the live and blocked tasks.
				indexmap.remove(0);

			}

			// Sort the deadrequestlist first so at the stage of aborting
			// tasks, we can follow the order of small task index to large task
			// index.
			Collections.sort(deadrequestlist);

			// If deadlock do happen, then handle the situation, abort some
			// tasks,
			// get rid of thier action list, give back their resource.
			if (deadrequest == num_livetasks && num_livetasks != 0) {
				num_termin += handledeadlock(deadrequestlist, resourceslist, actiontable, tasklist, indexmap);
				num_livetasks -= num_termin;
			}
			// After touching each action for each live task, the dead request
			// list
			// will be cleared for the checking and adding for the next round;
			deadrequestlist.clear();
			cycle++;
		}
	}

	public String handleillegalinitiate(int i, List<Resource> resourceslist, List<Task> tasklist,
			List<LinkedList<Action>> actiontable) {
		String errormsg = "";
		int resourcenum = actiontable.get(i).getFirst().resourcenum;
		int claimamount = actiontable.get(i).getFirst().actionamount;
		tasklist.get(i).aborted = true;
		actiontable.get(i).clear();
		errormsg = String.format(
				"Banker aborts task %d before run begins: \n   claim for resourse %d (%d) exceeds number of units present (%d)",
				i+1, resourcenum+1, claimamount, resourceslist.get(resourcenum).totalunits);

		return errormsg;
	}
	/**
	 * Handle over request, release all of this task's resources, kill it and return an error message
	*/
	public String handleoverrequest(int cycle, int i, List<LinkedList<Action>> actiontable, List<Task> tasklist,
			List<Resource> resourceslist) {
		int currtask = actiontable.get(i).getFirst().tasknum;

		tasklist.get(currtask).aborted = true;
		returnresources(resourceslist, tasklist.get(i));
		actiontable.get(i).clear();
		String errormsg = String.format(
				"During cycle %d-%d of Banker's algorithms \n   Task %d's request exceeds its claim; aborted; %d units available next cycle",
				cycle, cycle + 1, i+1, tasklist.get(i).gettotalrequested());
		return errormsg;
	}
	/**
	 * For checking if a task have entries that are all smaller than 
	 * available resources
	*/
	public boolean locked(List<Integer> needarr, int[] avaiarr){
		for(int i=0;i<needarr.size();i++){
			if(needarr.get(i)>avaiarr[i]){
				return true;
			}
		}
		return false;
	}
	/**
	 * Run some simulation to see if it's a safe state
	 * First of all, find a a row in the needed matrix such that all the entries
	 * are smaller than the available array. Then we give this row(this task) all 
	 * its need and try to end it, and then release all its resources,  continue
	 * the process until we terminate all the tasks. It the above situation is achieved
	 * ie, all the tasks are terminated, then this state is safe, 
	 * we can go back to where we call this method and assign true to {@link ResourceManager#issafe(List, List, Action)} 
	 * @param needed
	 * @param requested
	 * @param available
	 */
	public void checksafe(List<List<Integer>> needed, List<List<Integer>> requested, int[] available){
		int num_res=available.length;
		int num_tasks=needed.size();
		int count=0;
		//Loop over num_tasks times, each time try to find one task to kill
		while(count!=num_tasks){
			int unsuccessnum=0;
			for(int i=0;i<num_tasks;i++){
				//If we can find one task that is still alive and all of its entries are 
				//smaller than their corresponding available resource, then we give it all
				//it still needs to terminate, kill it and release all of its resources
				if(!needed.get(i).isEmpty()&&!locked(needed.get(i),available)){
					for(int j=0;j<num_res;j++){
						available[j]+=requested.get(i).get(j);
					}
					needed.get(i).clear();
					requested.get(i).clear();
					break;
				}
				else{
					//if we cannot find one that can be kill, then increment the count
					//so that if the count is equal to num_tasks then we know we've
					//looped over all the tasks but cannot find one to kill.
					unsuccessnum++;
				}
			}
			if(unsuccessnum==num_tasks){
				
				break;
			}
			count++;
		}
		
	}
	/**
	 * Consider a resource, the safe state happen only when pretending now 
	 * we accept the request and distribute resource accordingly, there is 
	 * a way such that all task can be ended.  
	 * @see ResourceManager#checksafe(List, List, int[])
	 * @param Tasklist
	 * @param resourceslist
	 * @param currrequest
	 * @return True if state is safe
	 */
	public boolean issafe(List<Task> Tasklist, List<Resource> resourceslist, Action currrequest) {
		int num_tasks=Tasklist.size();
		int num_res=resourceslist.size();
		int request=currrequest.actionamount;
		//Requested Matrix, Rows are tasks, Cols are resources
		List<List<Integer>> requested=new ArrayList<List<Integer>>();
		//Needed Matrix, Rows are tasks, Cols are resources
		List<List<Integer>> needed=new ArrayList<List<Integer>>();
		//Available array with all the available units of every resource 
        int[] available=new int[num_res];
        //Input data to two matrices
		for (int i = 0; i < num_tasks; i++) {
			//If a task is already aborted, skip it.
			if(Tasklist.get(i).aborted){
				continue;
			}
			Task task = Tasklist.get(i);
			
			List<Integer> req=new ArrayList<Integer>();
			List<Integer> ned=new ArrayList<Integer>();
			for(int j=0;j<num_res;j++){
				if(i==currrequest.tasknum&&j==currrequest.resourcenum){
					//Pretend that we give the resource the curr request needed
					//So requested get incremented,and neede get decremented 
					//accordingly
					req.add(task.requestedperresource[j]+request);
					ned.add(task.needed[j]-request);
				}
				else{
					req.add(task.requestedperresource[j]);
					ned.add(task.needed[j]);
				}
			}
			requested.add(req);
			needed.add(ned);
		}
		int livetasknum=requested.size();
		//Get the data of available resources
		for(int j=0;j<num_res;j++){
			available[j]=resourceslist.get(j).unitsleft;
		}
		//Pretend that we give the resource the curr request needed
		available[currrequest.resourcenum]-=request;
		
		checksafe( needed, requested, available);
		// If Not all of the task entry is empty, then we know that 
		// there isn't a path that you can end all the tasks. 
		for(int t=0;t<livetasknum;t++){
			if(!needed.get(t).isEmpty()){
				return false;
			}
		}
		//Otherwise, if all the tasks are terminated(there entries are emptied)
		//return true, and this state is safe given the assumption.
		return true;
	}

	/**
	 * Banker's algorithm.
	 * See {@link ResourceManager#checksafe} and {@link ResourceManager#issafe} for how to know
	 * if a state is safe and the request can be accepted
	 * @param errmsg
	 * @param resourceslist
	 * @param tasklist
	 * @param actiontable
	 */
	public void banker(String[] errmsg, List<Resource> resourceslist, List<Task> tasklist,
			List<LinkedList<Action>> actiontable) {

		int cycle = 0;
		int safenum=0;
		int num_tasks = tasklist.size();
		int num_resources = resourceslist.size();
		int num_termin = 0;
		int num_livetasks = num_tasks;
		// Indexmap to decide the order of execution of the loop,
		// i.e. which task's action should we consider first.
		List<Integer> indexmap = new ArrayList<Integer>();
		while (num_termin < num_tasks) {

			for (int k = 0; k < num_tasks; k++) {
				if (!indexmap.contains(k) && !(actiontable.get(k).isEmpty())) {
					indexmap.add(k);
					// add non blocked action indices to the indexmaplist.
					// Since in the previous round, we've added all the blocked
					// indices
					// and those that are still blocked stayed in the list, so
					// the
					// order of executing is what we want.
				}
			}

			//Update every resource's unitsleft given the received from previous cycle
			for (int m = 0; m < num_resources; m++) {
				resourceslist.get(m).update();
			}
			//Update the live tasks we are looking at
			num_livetasks = num_tasks - num_termin;
			
			for (int j = 0; j < num_livetasks; j++) {
				int i = indexmap.get(0);

				Action curraction = actiontable.get(i).getFirst();

				// The release each resource get is added to the received
				// field to each resource,
				// So at the next cycle, we want to update the unitsleft
				// field of the resource and reset received field to 0.

				if (curraction.delay == 0) {
					
					if (curraction.actionname.equals("initiate")) {
						if (curraction.actionamount > resourceslist.get(curraction.resourcenum).totalunits) {
							errmsg[0] = handleillegalinitiate(i, resourceslist, tasklist, actiontable) + "\n";
							num_termin++;
						}
						// For a task, its initial claim for resource is stored
						// in
						// the right index of initialclaimperresource array in
						// task
						// object
						else {
							tasklist.get(i).iniclaimperresource[curraction.resourcenum] = curraction.actionamount;
							tasklist.get(i).needed[curraction.resourcenum] = curraction.actionamount;
							actiontable.get(i).removeFirst();
						}
					}

					else{
						
					}
					if (curraction.actionname.equals("request")) {
						// If a task request a resource of an amount exceeding 
						// it's max needs then we need to abort the task requesting this,
						// release all of its resource units back to all the resources, 
						// clear its actionlist.
						if (curraction.actionamount> tasklist.get(i).needed[curraction.resourcenum]) {
							tasklist.get(i).aborted=true;				
							errmsg[0] += handleoverrequest(cycle, i, actiontable, tasklist, resourceslist) + "\n";
							actiontable.get(i).clear();							
							num_termin++;
						}
						else{
							if (requestacceptable(curraction, resourceslist)
									&& issafe(tasklist, resourceslist, curraction)) {
								tasklist.get(i).requestedperresource[curraction.resourcenum] += curraction.actionamount;
								tasklist.get(i).needed[curraction.resourcenum] -= curraction.actionamount;
								resourceslist.get(curraction.resourcenum).unitsleft -= curraction.actionamount;
								actiontable.get(i).removeFirst();
							} else {
								// if the request can not be accepted, then block it,
								// Add the indices of the blocked tasks so next time
								// it will be checked first.
								indexmap.add(i);
								tasklist.get(i).waitingtime += 1;
							}
						}
						
					} else if (curraction.actionname.equals("release")) {
					
						// If a task's action is release, then release the units
						// back to corresponding resource,
						// decrement the total request for that resource left in
						// the task object
						// then remove it from actionlist since it can be
						// processed.
						resourceslist.get(curraction.resourcenum).received += curraction.actionamount;
						tasklist.get(i).requestedperresource[curraction.resourcenum] -= curraction.actionamount;
						tasklist.get(i).needed[curraction.resourcenum]+= curraction.actionamount;
						actiontable.get(i).removeFirst();

					} else if (curraction.actionname.equals("terminate")) {
						// If a task is terminated, we should actually give the
						// total
						// left request amount back to
						// its original resource. In this lab, the request left
						// will be 0 eventually because
						// the total amount of request and the total amount of
						// release of a task are equal. So I ignored this step
						// because implementing this will require me to
						// terminate a task
						// during the action just ahead of termination.

						actiontable.get(i).clear();
						num_termin++;
						tasklist.get(i).timetaken -= 1;
					}

				} else {
					curraction.delay--;
				}

				tasklist.get(i).timetaken += 1;
				// After the operation on current task, remove its index from
				// the indexmap,
				// so next time we are still getting the head of the indexmap
				// and eventually
				// after looping over all the live tasks indices in the
				// indexmap, we will
				// clear the old map but with the adding of blocked tasks, our
				// indexmap will
				// contain all the live and blocked tasks.
				indexmap.remove(0);

			}

			cycle++;
		}
		
	}

	// Get the string version of the all the statistics we desired,
	// store them into a string list for further output.
	public void updateresult(List<String> resultlist, List<Task> tasklist) {
		int totalused = 0;
		int totalwaiting = 0;
		// Add the total time taken,total time waited for each task orderly to
		// the list
		// and also add the "total" result to end of resultlist after we looped
		// over all the tasks
		// and add the string version of their statistics to resultlist.
		for (int i = 0; i < tasklist.size(); i++) {
			String str = String.format("Task %d %s", i + 1, tasklist.get(i).toString());
			resultlist.add(str);
			if (!tasklist.get(i).aborted) {
				totalused += tasklist.get(i).timetaken;
				totalwaiting += tasklist.get(i).waitingtime;
			}
		}
		// "total" row in the final result
		String total = String.format("total %5s %d  %d  %.0f%s", " ", totalused, totalwaiting,
				totalwaiting * 100.0 / totalused, "%");
		resultlist.add(total);
	}

	// Print the final result to the console, conforming the example format,
	// which is fifo on the left, banker on the right.
	public void printeverything(String[] errmsg, List<String> fiforesult, List<String> bankerresult) {
		// If there is error message, print it out first.
		if (!errmsg[0].equals("")) {
			System.out.println(errmsg[0]);
		}
		// Print out the header
		String header = String.format("%10s %s %20s %s %10s", " ", "FIFO", " ", "BANKER'S", " ");
		System.out.println(header);
		// Print out the result, one fifo, one banker per line.
		for (int i = 0; i < fiforesult.size(); i++) {
			String tmp = String.format("%5s %s %7s %s", " ", fiforesult.get(i), " ", bankerresult.get(i));
			System.out.println(tmp);
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		// Input source, from stdin.
		String input = args[0];
		// List of tasks to track and record waiting time/finish time of each
		// task.
		List<Task> tasklist = new ArrayList<Task>();
		List<Task> tasklistbanker=new ArrayList<Task>();
		// List of resources, for the purpose of managing available resource
		// units for each resource.
		List<Resource> resourceslist = new ArrayList<Resource>();
		List<Resource> resourceslistbanker = new ArrayList<Resource>();
		// List of a list of actions. This list is indiced by task number,
		// that meaning, actiontable[i] is the action list for task i,
		// in other words, actiontable[i] contains all the upcoming action
		// for task i.
		List<LinkedList<Action>> actiontable = new ArrayList<LinkedList<Action>>();
		List<LinkedList<Action>> actiontablebanker = new ArrayList<LinkedList<Action>>();
		ResourceManager manager = new ResourceManager();
		Scanner reader = new Scanner(new FileInputStream(input));

		int num_tasks = reader.nextInt();
		int num_resources = reader.nextInt();
		// I set a limit for number of tasks and the number of available
		// resources.
		// They are 100 respectivly.
		if (num_tasks > 100 || num_resources > 100) {
			System.exit(0);
		}

		// Add empty tasks to tasklist for further operation on each task.
		// In the future, we can refer the task we want use the indices in
		// tasklist.
		for (int i = 0; i < num_tasks; i++) {
			tasklist.add(new Task(num_resources));
			tasklistbanker.add(new Task(num_resources));
			actiontable.add(new LinkedList<Action>());
			actiontablebanker.add(new LinkedList<Action>());
		}

		// Add resource(with its totalunits passed upon creation) to
		// resourceslist
		for (int i = 0; i < num_resources; i++) {
			int tmp=reader.nextInt();
			resourceslist.add(new Resource(tmp));
			resourceslistbanker.add(new Resource(tmp));
		}

		// Read the following lines of all the action into the actiontable wrt
		// each task.
		//High level idea, add all following actions to the corresponding task,
		//and loop over all the tasks everytime, if an action can be accepted,
		//remove it from the list, otherwise block it, keep it there.
		while (reader.hasNext()) {
			String state = reader.next();
			int tasknum = reader.nextInt() - 1;
			int delay = reader.nextInt();
			int resourcenum = reader.nextInt() - 1;
			int actionamout = reader.nextInt();
			// For each line, wrap it as a new Action object.
			Action curraction = new Action(state, tasknum, delay, resourcenum, actionamout);
			Action curractionb=new Action(state, tasknum, delay, resourcenum, actionamout);
			actiontable.get(tasknum).add(curraction);
			actiontablebanker.get(tasknum).add(curractionb);
		}
		// Run the fifo algorithm.
		manager.optmanager(resourceslist, tasklist, actiontable);
		// Run the bankers algorithm.
		String[] errormsg = new String[1];
		errormsg[0]="";
		manager.banker(errormsg, resourceslistbanker, tasklistbanker, actiontablebanker);
		// Create lists of string for the output and format them into a string list
		List<String> fiforesult = new ArrayList<String>();
		manager.updateresult(fiforesult, tasklist);
		 
		List<String> bankerresult = new ArrayList<String>();
		//Update result to a string list for concatenated display
		manager.updateresult(bankerresult, tasklistbanker);
		manager.printeverything(errormsg,fiforesult, bankerresult);

		reader.close();
	}
}
