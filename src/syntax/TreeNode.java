package syntax;

import java.util.ArrayList;

public class TreeNode {

	private String name;
	private ArrayList<TreeNode> children;
	
	public TreeNode(String name) {
		this.name = name;
		children = new ArrayList<TreeNode>();
	}
	
	public void addChild(TreeNode child) {
		children.add(child);
	}
	
	public ArrayList<TreeNode> getChildren() {
		return children;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
