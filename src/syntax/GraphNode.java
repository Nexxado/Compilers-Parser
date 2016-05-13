package syntax;

public class GraphNode {

	private String from, to;
	
	public GraphNode(String from, String to) {
		this.from = from;
		this.to = to;
	}
	
	@Override
	public String toString() {
		return from + " -> " + to;
	}

}
