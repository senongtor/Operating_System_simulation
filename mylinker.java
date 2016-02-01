package imple;

import java.util.*;
import java.io.*;

class Pair {
	private String first;// first member of pair
	private int second;// second member of pair

	public Pair(String first, int second) {
		this.first = first;
		this.second = second;
	}

	public void setFirst(String first) {
		this.first = first;
	}

	public void setSecond(int second) {
		this.second = second;
	}

	public String getFirst() {
		return this.first;
	}

	public int getSecond() {
		return this.second;
	}
}

public class mylinker {
	public static void printresult(Map<String, Integer> symbolTable, Map<String, String> symboltableerror,
			Map<Integer, String> memorymap, List<String> warninglist) {
		System.out.println("Symbol Table");
		for (Map.Entry<String, Integer> entry : symbolTable.entrySet()) {
			if (symboltableerror.containsKey(entry.getKey())) {
				System.out.printf("%s=%d %s\n", entry.getKey(), entry.getValue(), symboltableerror.get(entry.getKey()));
			} else {
				System.out.printf("%s=%d\n", entry.getKey(), entry.getValue());
			}
		}
		System.out.println();
		System.out.println("Memory Map");
		for (Map.Entry<Integer, String> entry : memorymap.entrySet()) {
			System.out.printf("%d: %3s\n", entry.getKey(), entry.getValue());
		}
		System.out.println();
		Iterator<String> it = warninglist.iterator();
		while (it.hasNext()) {
			String curr = it.next();
			System.out.println(curr);
		}
	}

	public static void main(String[] args) throws IOException {
		Map<String, Integer> symbolTable = new LinkedHashMap<String, Integer>();
		Map<Integer, String> memorymap = new LinkedHashMap<Integer, String>();
		Map<String, String> symboltableerror = new HashMap<String, String>();
		Map<String, Integer> defmodulemap = new HashMap<String, Integer>();
		// Recording a map between definition string and the module it's in so
		// we can generate
		// warning regarding a specific module
		List<String> warninglist = new LinkedList<String>();
		List<Pair> uselist = new LinkedList<Pair>();
		List<Pair> addrlist = new LinkedList<Pair>();
		int memorymapindex = 0; // for final recording of memory map.
		int countofmodule = 0;
		// int numofmodule=0;
		// count the lines it should have if the input is strucuted:
		// def/use/addr
		int totalspace = 0; // count the total number of addresses pairs we have
		// first pass, handle multiple definition, calculate relative address,
		// map symbols

		// 改输入方式！！！！
		String input = "/Users/senongtor/Documents/NYU_CS/OS/Lab1/input3.txt";
		Scanner reader = new Scanner(new FileInputStream(input));

		while (reader.hasNext()) {
			ArrayList<Pair> modulesymbolpair = new ArrayList<Pair>();
			int defsize = 0;
			int usesize = 0;
			int addrsize = 0;
			// Definition line
			defsize = Integer.parseInt(reader.next());
			int defsizeloop = defsize;
			while (defsizeloop > 0) {
				String symbol = reader.next();
				String val = reader.next();
				if (symbolTable.containsKey(symbol)) {
					symboltableerror.put(symbol, "Error: This variable is multiply defined; first value used.");
				} else {
					symbolTable.put(symbol, Integer.parseInt(val) + totalspace);
					modulesymbolpair.add(new Pair(symbol, Integer.parseInt(val)));
					defmodulemap.put(symbol, countofmodule);
				}
				defsizeloop--;
			}
			// Use line
			usesize = Integer.parseInt(reader.next());
			int usesizeloop = usesize;
			while (usesizeloop > 0) {
				String use = reader.next();
				String uval = reader.next();

				uselist.add(new Pair(use, Integer.parseInt(uval)));
				usesizeloop--;
			}

			// Address line
			addrsize = Integer.parseInt(reader.next());
			int addrsizeloop = addrsize;
			while (addrsizeloop > 0) {
				String mark = reader.next();
				String addr = reader.next();

				addrlist.add(new Pair(mark, Integer.parseInt(addr)));
				totalspace++;
				addrsizeloop--;
			}
			countofmodule++;

			for (int i = 0; i < modulesymbolpair.size(); i++) {
				Pair defpairinmodule = modulesymbolpair.get(i);
				if (defpairinmodule.getSecond() > addrsize) {
					String s = String.format("Error: The value of %s is outside module %d; zero (relative) used.\n",
							defpairinmodule.getFirst(), defmodulemap.get(defpairinmodule.getFirst()));
					symboltableerror.put(defpairinmodule.getFirst(), s);
					symbolTable.put(defpairinmodule.getFirst(),
							symbolTable.get(defpairinmodule.getFirst()) - defpairinmodule.getSecond());
				}
			}
		}

		Iterator<Pair> useit = uselist.iterator();
		List<String> usesymbollist = new LinkedList<String>();
		while (useit.hasNext()) {
			Pair curr = useit.next();
			usesymbollist.add(curr.getFirst());
		}
		for (String s : symbolTable.keySet()) {
			if (!usesymbollist.contains(s)) {
				String msg = String.format("Warning: %s was defined in module %d but never used.", s,
						defmodulemap.get(s));
				warninglist.add(msg);
			}
		}
		// Second pass must check if a use of addr is defined, if a add is used
		// not defined,
		// If an address appearing in a use list exceeds the size of the module.
		// If an address on a use list is not type E, print an error message and
		// treat the address as type E. If a type E address is not on a use
		// list, print an error message and treat the address as type I
		totalspace = 0;
		Scanner reader2 = new Scanner(new FileInputStream(input));
		String errormsg = "";
		while (reader2.hasNext()) {
			ArrayList<Pair> uselist2 = new ArrayList<Pair>();
			ArrayList<Pair> addr = new ArrayList<Pair>();

			int defsize = 0;
			int usesize = 0;
			int addrsize = 0;
			// Skipping the definition line
			defsize = Integer.parseInt(reader2.next());
			int defsizeloop = defsize;
			while (defsizeloop > 0) {
				String symbol = reader2.next();
				String val = reader2.next();
				defsizeloop--;
			}
			// Use line
			usesize = Integer.parseInt(reader2.next());
			int usesizeloop = usesize;
			while (usesizeloop > 0) {
				String use = reader2.next();
				String uval = reader2.next();
				uselist2.add(new Pair(use, Integer.parseInt(uval)));
				usesizeloop--;
			}

			// Address line
			addrsize = Integer.parseInt(reader2.next());
			int addrsizeloop = addrsize;

			while (addrsizeloop > 0) {
				String mark = reader2.next();
				String add = reader2.next();

				addr.add(new Pair(mark, Integer.parseInt(add)));
				totalspace++;
				addrsizeloop--;
			}
			countofmodule++;

			String[] addrerrormsg = new String[addrsize];
			Arrays.fill(addrerrormsg, "");
			for (int i = 0; i < uselist2.size(); i++) {
				Pair currpair = uselist2.get(i);
				int init = currpair.getSecond();
				// handle use address exceeding the size of addr list

				int absval = 0;
				if (!symbolTable.containsKey(currpair.getFirst())) {
					for (int k = 0; k < addrsize; k++) {
						String s = String.format("   Error: %s is not defined; zero used.", currpair.getFirst());
						absval = 0;
						addrerrormsg[k] = s;
					}
				} else {

					absval = symbolTable.get(currpair.getFirst());
				}
				int nextindex = addr.get(init).getSecond() % 1000;
				if (nextindex >= addrsize &&nextindex!=777) {
					addrerrormsg[init] = "   Error: Pointer in use chain exceeds module size; chain terminated.";
					addr.set(init, new Pair("Finished", (addr.get(init).getSecond() / 1000) * 1000 + absval));
					break;
				}
				if (!addr.get(init).getFirst().equals("E")) {
					String s = String.format("   Error: %s type address on use chain; treated as E type.",
							addr.get(init).getFirst());
					addrerrormsg[init] = s;
				}
				addr.set(init, new Pair("Finished", (addr.get(init).getSecond() / 1000) * 1000 + absval));
				while (nextindex != 777) {
					int nextnextindex = addr.get(nextindex).getSecond() % 1000;
					if (nextnextindex >= addrsize&&nextnextindex!=777) {
						addrerrormsg[nextindex] = "   Error: Pointer in use chain exceeds module size; chain terminated.";
						addr.set(nextindex,
								new Pair("Finished", (addr.get(nextindex).getSecond() / 1000) * 1000 + absval));
						break;
					}
					if (!addr.get(nextindex).getFirst().equals("E")) {
						String s = String.format("   Error: %s type address on use chain; treated as E type.",
								addr.get(nextindex).getFirst());
						addrerrormsg[nextindex] = s;
					}

					addr.set(nextindex, new Pair("Finished", (addr.get(nextindex).getSecond() / 1000) * 1000 + absval));
					nextindex = nextnextindex;
				}
			}
			// Check if an address is of 'R'(Relative), if it's the case,
			// calculate it's absolute address and relocate
			// Also check if all the 'E' type are captured.
			for (int i = 0; i < addrsize; i++) {
				if (addr.get(i).getFirst().equals("R")) {
					addr.set(i, new Pair("R", addr.get(i).getSecond() + totalspace - addrsize));
				}
				if (addr.get(i).getFirst().equals("E")) {
					addr.set(i, new Pair("I", addr.get(i).getSecond()));
					addrerrormsg[i] = "   Error: E type address not on use chain; treated as I type.";
				}
			}

			for (int i = 0; i < addrsize; i++) {
				memorymap.put(i + totalspace - addrsize, Integer.toString(addr.get(i).getSecond()) + addrerrormsg[i]);
			}
		}
		printresult(symbolTable, symboltableerror, memorymap, warninglist);
	}
}
