import java.util.ArrayList;
import java.util.List;

public class Location extends Identifier{
	/**
	 * 
	 */
	private static final long serialVersionUID = 534694564191741451L;
	List<Integer> connected;
	public Location(int i, String n) {
		id = i;
		name = n;
		connected = new ArrayList<Integer>();
	}
}
