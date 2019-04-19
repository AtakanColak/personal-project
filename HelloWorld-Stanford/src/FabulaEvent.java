import java.util.ArrayList;
import java.util.List;

public class FabulaEvent extends Identifier {
	public List<Integer> subject_agent_ids;
	public Integer action_id;
	public FabulaEvent() {
		subject_agent_ids = new ArrayList<Integer>();
	}
}
