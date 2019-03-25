
public class AgentPointer {
	public Integer agent_index;
	public Integer sentence_index;
	public Integer sentence_depth;
	public AgentPointer(Integer i, Integer si, Integer sd) {
		agent_index = i;
		sentence_index = si;
		sentence_depth = sd;
	}
}
