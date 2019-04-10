import java.util.ArrayList;
import java.util.List;

public class Agent extends Identifier{
	public List<Integer> internals;
	public List<Integer> actions;
	public int location;
	public Agent() {
		internals = new ArrayList<Integer>();
		actions = new ArrayList<Integer>();
		location = 0;
	}
	public Agent(int i, String n) {
		internals = new ArrayList<Integer>();
		actions = new ArrayList<Integer>();
		location = 0;
		id = i;
		name = n;
	}
}
