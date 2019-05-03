import java.util.ArrayList;
import java.util.List;

public class Agent extends Identifier {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6655912864605319986L;
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

	public String toString() {return this.name;}
	
	public <E extends Identifier,L extends Identifier> String toString(List<E> internals, List<E> actions, List<L> locations) {
		StringBuilder sb = new StringBuilder();
		sb.append("Agent[" + this.id + "] : " + this.name);
		sb.append("\n\n");
//		if (this.internals.size() != 0)
//			sb.append("\n");//Internal Elements : \n");
		for(Integer i : this.internals) 
			sb.append("\tInternalElement[" + i + "] : " + internals.get(i).name + "\n");
		if (this.internals.size() != 0)
			sb.append("\n");//Actions : \n");
//		sb.append("\n");
		for(Integer i : this.actions) 
			sb.append("\tAction[" + i + "] : " + actions.get(i).name + "\n");
		sb.append("\n");
		sb.append("\tLocation : " + locations.get(location).name + "\n");
		sb.append("\n");
		return sb.toString();
	}

}
