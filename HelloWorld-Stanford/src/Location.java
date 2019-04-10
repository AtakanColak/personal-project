import java.util.ArrayList;
import java.util.List;

public class Location extends Identifier{
	List<Integer> connected;
	public Location(int i, String n) {
		id = i;
		name = n;
		connected = new ArrayList<Integer>();
	}
}
