import java.util.ArrayList;
import java.util.List;

public class FabulaEvent extends Identifier {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3355941175096977525L;
	public List<Integer> subject_agent_ids;
	public Integer action_id;
	public FabulaEvent(Integer sid) {
		story_id = sid;
		subject_agent_ids = new ArrayList<Integer>();
	}
	
	public String toString(List<Agent> ag, List<FabulaElement> ac) {
		StringBuilder sb = new StringBuilder();
		sb.append("Event[" + this.id + "] : " + ac.get(action_id).name + " by ");
		for(Integer i : this.subject_agent_ids) 
			sb.append(ag.get(i).name + " ");
		return sb.toString();
	}
}
