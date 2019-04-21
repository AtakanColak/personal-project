import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.trees.Tree;

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
	
	public static <E extends Identifier> Integer IndexAtListThatExtendsIdentifier(List<E> list, String name) {
		for (int i = 0; i < list.size(); ++i)
			if (list.get(i).name.equals(name))
				return i;
		return -1;
	}

	public static <E extends Identifier> Integer AddToListThatExtendsIdentifier(List<E> list, E elem) {
		Integer index = IndexAtListThatExtendsIdentifier(list, elem.name);
		if(index == -1) {
			elem.id = list.size();
			index = elem.id;
			list.add(elem);
		}
		return index;
	}
	
	public static <E extends Identifier> void PrintListThatExtendsIdentifier(List<E> list, String listname) {
		for (int i = 0; i < list.size(); ++i) { 
			StringBuilder sb = new StringBuilder();
			sb.append(listname);
			sb.append("[" + i + "] : ");
			sb.append(list.get(i).name);
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
			for (int j = 0; j < a.actions.size(); ++j) {
				sb.append(", ");
				sb.append(actions.get(a.actions.get(j)).name);
			}
			sb.append(", at location ");
			sb.append(a.location);
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

	public static Integer AddFabulaElement(List<FabulaElement> list, FabulaElement.ElementType etype, String name) {
		Integer index = Tools.ElementListIndex(list, name);
		if (index == -1) {
			FabulaElement e = new FabulaElement();
			e.name = name;
			e.id = list.size();
			e.type = etype;
			list.add(e);
			index = e.id;
		}
		return index;
	}

	public static boolean HasPhrase(Tree t, String p) {
		for (int i = 0; i < t.children().length; ++i) {
			if (p.equals(t.children()[i].label().toString()))
				return true;
		}
		return false;
	}

	public static void GetPhrase(List<Tree> phrases, Tree t, String[] p) {
		Tree[] children = t.children();
		for (int i = 0; i < t.children().length; ++i) {
			for (int j = 0; j < p.length; ++j) {
				if (p[j].equals(children[i].label().toString()))
					phrases.add(children[i]);
			}
		}
	}
	
	public static List<Integer> AgentIndicesAtLocation(List<Agent> agents, Integer location_id) {
		List<Integer> indices = new ArrayList<Integer>();
		for(Agent a  : agents) {
			if(a.location == location_id)
				indices.add(a.id);
		}
		return indices;
	}
	
	private static Integer IsAgentObserver(FabulaEvent event, Integer agentID) {
		for(Integer i : event.subject_agent_ids) {
			if(i == agentID) return -1;
		}
		return agentID;
	}
	
	public static List<Integer> ObserverAgentsAtLocation(List<Agent> agents, FabulaEvent event, Integer location) {
		List<Integer> list = new ArrayList<Integer>();
		for(Agent a : agents) {
			if(a.location == location) {
				Integer r = IsAgentObserver(event, a.id);
				if(r != -1) list.add(r);
			} 
		}
		return list;
	}

	private static Integer LocationIndex(List<Location> locations, String location_name) {
		for (int i = 0; i < locations.size(); ++i)
			if (locations.get(i).name.equals(location_name))
				return i;
		return -1;
	}

	public static Integer AddLocation(List<Location> locations, Integer old_index, String new_location_name) {
		Integer next_index = LocationIndex(locations, new_location_name);
//		System.out.println("old_index is" + old_index + " and new loc name is "+ new_location_name);
		Location new_loc;
		if (next_index == -1) {
			next_index = locations.size();
			new_loc = new Location(next_index, new_location_name);
			new_loc.connected.add(old_index);
			locations.add(new_loc);
		} else {
			new_loc = locations.get(next_index);
			if (!new_loc.connected.contains(old_index))
				new_loc.connected.add(old_index);
		}
		Location old = locations.get(old_index);
		if (!old.connected.contains(next_index))
			old.connected.add(next_index);
		
		return next_index;
	}

	public static void GetPhrase(List<Tree> phrases, Tree t, String p) {
		GetPhrase(phrases, t, new String[] { p });
	}

	public static Tree GetChild(Tree t, Integer i) {
		return t.children()[i];
	}
}
