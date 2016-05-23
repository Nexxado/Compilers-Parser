package syntax;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class SyntaxMain {
	
	
	public static void main(String[] args) {
		
		
		if(args == null || args.length < 2 || args[0] == null || args[1] == null) {
			System.err.println("[ERROR] Must pass config file followed by input file");
			return;
		}
		
		String configFilename = args[0];
		String inputFilename = args[1];
		
		Parser parser = new Parser(configFilename, inputFilename);
		TreeNode root = parser.yyLL1parse();
		
		if(root == null)
			return;
		
		try {

			
			//Write graph string to output file
			String outputFilename = inputFilename.substring(0, inputFilename.lastIndexOf('.')) + ".ptree";
			File outputFile = new File(outputFilename);
			
			FileWriter writer = new FileWriter(outputFile);
			writer.write(generateGraph(root));
			writer.close();
//			System.out.println("Graph data can be found in \"" + outputFile + "\", use webgraphviz.com to view."); //TODO DEBUG
			
			
		} catch (FileNotFoundException e) {
			System.err.println("[ERROR] Input File not Found: " + e.toString());
			return;
		} catch (IOException e) {
			System.err.println("[ERROR] Failed writing to output file: " + e.toString());
			return;
		}
		
		
	}
	
	/**
	 * @param root : TreeNode representing the Parse Tree's root node
	 * @return A String representing the parse tree as a graph, according to http://www.webgraphviz.com/
	 */
	private static String generateGraph(TreeNode root) {
		StringBuilder builder = new StringBuilder();
		builder.append("digraph G {" + System.lineSeparator());
		generateTree(root, builder);
		
		builder.append("}");
		return builder.toString();
	}

	
	private static void generateTree(TreeNode node, StringBuilder builder) {
		if(node == null)
			return;
			
//		System.err.println(node);
		for(int i = node.getChildren().size() - 1; i >= 0; i--) {
			String childName = node.getChildren().get(i).getName();
			builder.append(new GraphNode(node.getName(), childName) + System.lineSeparator());
		}
			
		for(int i = 0; i < node.getChildren().size(); i++) {
			generateTree(node.getChildren().get(i), builder);
		}
	}
	
	

}
