import java.util.ArrayList;
import java.util.List;

public class AgentPointer {
	public List<Integer> agent_index;
	public Integer sentence_index;
	public Integer sentence_depth;
	public AgentPointer(Integer i, Integer si, Integer sd) {
		agent_index = new ArrayList<Integer>();
		agent_index.add(i);
		sentence_index = si;
		sentence_depth = sd;
	}
	public AgentPointer() {
		agent_index = new ArrayList<Integer>();
	}
	
}
