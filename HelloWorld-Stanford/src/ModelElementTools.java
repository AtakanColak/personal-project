import java.util.List;

public class ModelElementTools {
	public static <E extends Identifier> Integer IndexByNameEquals(List<E> list, String name) {
		for(E e : list) 
			if(e.name.equals(name))
				return e.id;
		return null;
	}
	
	public static <E extends Identifier> Integer IndexByNameEquals(List<E> list, Integer story_index, String name) {
		for(E e : list) 
			if(e.name.equals(name) && e.story_id == story_index )
				return e.id;
		return null;
	}
	
	public static <E extends Identifier> Integer IndexByNameContains(List<E> list, Integer story_index, String name) {
		for(E e : list) 
			if(e.name.contains(name) && e.story_id == story_index )
				return e.id;
		return null;
	}
	
	public static Integer Identify(List<FabulaElement> list, FabulaElement.ElementType et, String name ) {
		Integer index = IndexByNameEquals(list, name);
		if (index == null) {
			FabulaElement e = new FabulaElement();
			e.id = list.size();
			e.type = et;
			e.name = name;
			e.story_id = -1;
			list.add(e);
			index = e.id;
		}
		return index;
	}
	
	private static void ConnectLocationTo(List<Location> locations, Integer from, Integer to) {
		Location next = locations.get(to);
		if(!next.connected.contains(from)) {
			next.connected.add(from);
			locations.set(to, next);
		}
	}
	
	public static Integer IdentifyLocation(List<Location> locations, Integer story_index, Integer old_index, String new_location_name) {
		Integer next_index = IndexByNameEquals(locations, story_index, new_location_name);
		if(next_index == null) {
			next_index = locations.size();
			Location n = new Location(next_index, new_location_name);
			n.story_id = story_index;
			locations.add(n);
		}
		ConnectLocationTo(locations, next_index, old_index);
		ConnectLocationTo(locations, old_index, next_index);
		return next_index;
//		Location next = locations.get(next_index);
//		if(!next.connected.contains(old_index)) {
//			next.connected.add(old_index);
//			locations.set(next_index, next);
//		}
//		Location old = locations.get(old_index);
//		if(!old.connected.contains(next_index)) {
//			old.connected.add(e_index);
//			locations.set(next_index, next);
//		}
//		System.out.println("old_index is" + old_index + " and new loc name is "+ new_location_name);
//		Location new_loc;
//		if (next_index == -1) {
//			next_index = locations.size();
//			new_loc = new Location(next_index, new_location_name);
//			new_loc.connected.add(old_index);
//			locations.add(new_loc);
//		} else {
//			new_loc = locations.get(next_index);
//			if (!new_loc.connected.contains(old_index))
//				new_loc.connected.add(old_index);
//		}
//		Location old = locations.get(old_index);
//		if (!old.connected.contains(next_index))
//			old.connected.add(next_index);
//
//		return next_index;
	}
}
