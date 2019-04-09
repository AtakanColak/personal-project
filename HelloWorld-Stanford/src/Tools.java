import java.util.List;

public class Tools {
	public static Integer ElementListIndex(List<FabulaElement> list, String name) {
		for (int i = 0; i < list.size(); ++i)
			if (list.get(i).name.equals(name))
				return i;
		return -1;
	}
	
	public static Integer AgentIndex(List<Agent> list, String name) {
		for (int i = 0; i < list.size(); ++i)
			if (list.get(i).name.equals(name))
				return i;
		return -1;
	}
	
	public static void PrintFabulaList(List<FabulaElement> list, String name) {
		for (int i = 0; i < list.size(); ++i) {
			FabulaElement a = list.get(i);
			StringBuilder sb = new StringBuilder();
			sb.append(name);
			sb.append("[");
			sb.append(i);
			sb.append("] : ");
			sb.append(a.name);
			System.out.println(sb.toString());
		}
	}
	
	public static void PrintAgents(List<Agent> agents, List<FabulaElement> actions) {
		for (int i = 0; i < agents.size(); ++i) {
			Agent a = agents.get(i);
			StringBuilder sb = new StringBuilder();
			sb.append("agents[");
			sb.append(i);
			sb.append("] : ");
			sb.append(a.name);
			for(int j =0; j < a.actions.size(); ++j) {
				sb.append(", ");
				sb.append(actions.get(a.actions.get(j)).name);
			}
			System.out.println(sb.toString());
		}
	}
	
	public static void PrintStack(List<AgentPointer> agent_stack, List<Agent> agents) {
		for (int i = 0; i < agent_stack.size(); ++i) {
			AgentPointer cur = agent_stack.get(i);
			StringBuilder sb = new StringBuilder();
			sb.append("agent_stack[");
			sb.append(i);
			sb.append("] : (");
			sb.append(cur.sentence_index);
			sb.append(",");
			sb.append(cur.sentence_depth);
			sb.append(",[");
			sb.append(agents.get(cur.agent_index.get(0)).name);
			for (int j = 1; j < cur.agent_index.size(); ++j) {
				sb.append(",");
				sb.append(agents.get(cur.agent_index.get(j)).name);
			}
			sb.append("])");
			System.out.println(sb.toString());
		}
	}
}
