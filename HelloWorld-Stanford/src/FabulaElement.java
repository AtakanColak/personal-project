
public class FabulaElement extends Identifier{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6225267093905498492L;
	public ElementType type;
	
	public enum ElementType {
		Goal, Action, Internal;
	}
}
