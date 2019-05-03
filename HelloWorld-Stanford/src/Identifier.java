import java.io.Serializable;

public abstract class Identifier implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8128130742469578972L;
	public int id;
	public Integer story_id;
	public String name;

	public String toString(String typename) {
		StringBuilder sb = new StringBuilder();
		sb.append(typename + "[" + this.id + "] : " + this.name);
		return sb.toString();
	}
}
