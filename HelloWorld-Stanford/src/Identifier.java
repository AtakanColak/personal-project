public abstract class Identifier {
	public int id;
	public Integer story_id;
	public String name;

	public String toString(String typename) {
		StringBuilder sb = new StringBuilder();
		sb.append(typename + "[" + this.id + "] : " + this.name);
		return sb.toString();
	}
}
