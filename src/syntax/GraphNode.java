package syntax;

public class GraphNode {

	private String from, to;
	private int id, modifier;
	
	public GraphNode(String from, String to, int id, int modifier) {
		this.from = from;
		this.to = to;
		this.id = id;
		this.modifier = modifier;
	}
	
	@Override
	public String toString() {
		return from + "_" + id + " -> " + to + "_" + (id + modifier);
	}

}
