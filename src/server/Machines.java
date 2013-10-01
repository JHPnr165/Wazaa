package server;

import java.util.ArrayList;
import java.util.List;

public class Machines {
	public String name;
	public List<Machine> machines = new ArrayList<Machine>();
//	public Machines() {}

	public String toString() {
		return name + machines;
	}
}
