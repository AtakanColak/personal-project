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

	public AgentPointer(Integer si, Integer sd) {
		agent_index = new ArrayList<Integer>();
		sentence_index = si;
		sentence_depth = sd;
	}

	public AgentPointer() {
		agent_index = new ArrayList<Integer>();
	}

	public String toString(List<Agent> agents) {
//		System.out.println(agents);
//		System.out.println(agent_index);
		StringBuilder sb = new StringBuilder();
		sb.append("Agent Pointer (" + this.sentence_index + "," + this.sentence_depth + ") : | ");
		for (Integer i : this.agent_index) {
			try {
			sb.append(agents.get(i).name + " | ");
			}
			catch (Exception e){
//				sb.append(agents.get(this.agent_index.get(i - 1)).name + " | ");
				sb.append(i + " | ");
			}
			finally {}
		}
		return sb.toString();
	}

}
