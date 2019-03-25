import java.util.ArrayList;
import java.util.List;

public class Agent extends FabulaElement{
	public List<Integer> internals;
	public List<Integer> actions;
	public Agent() {
		internals = new ArrayList<Integer>();
		actions = new ArrayList<Integer>();
	}
}
