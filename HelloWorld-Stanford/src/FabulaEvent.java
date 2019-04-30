import java.util.ArrayList;
import java.util.List;

public class FabulaEvent extends Identifier {
	public Integer story_id;
	public List<Integer> subject_agent_ids;
	public Integer action_id;
	public FabulaEvent(Integer sid) {
		story_id = sid;
		subject_agent_ids = new ArrayList<Integer>();
	}
}
