package syntax;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

import lexcial.Scanner;
import lexcial.TokenInfo;
import lexcial.TokenTypeEnum;

public class Parser {
	
	private final String TERMINALS_ID = "terminals";
	private final String EPSILON_ID = "eps";
	private final String END_OF_INPUT_ID = "$";
	private HashMap<String, HashMap<String, String>> parse_table;
	private ArrayList<String> terminals;
	private ArrayList<String> non_terminals = new ArrayList<String>();
	private Stack<TreeNode> parse_stack = new Stack<TreeNode>();
	private String input;
	private String config;
	
	
	public Parser(String configFilename, String inputFilename) {
		config = configFilename;
		input = inputFilename;
	}
	
	
	public TreeNode yyLL1Parse() {
		
		
		parse_table = parseConfigFile(config);
		if(parse_table == null)
			return null;
		
		System.out.println("Parsing input..."); //TODO DEBUG
		
		//Initialize Stack
		int nodeId = 0;
		TreeNode root = new TreeNode(non_terminals.get(0) + "_" + nodeId++);
		parse_stack.push(new TreeNode(END_OF_INPUT_ID));
		parse_stack.push(root);
		
//		System.out.println("non_terminals: " + non_terminals); //TODO DEBUG
//		System.out.println("parse table: " + parse_table); //TODO DEBUG
//		System.out.println("parse stack: " + parse_stack); //TODO DEBUG
		
		
		Scanner sc;
		TokenInfo token;

		
		try {
			
			sc = new Scanner(input);
			
			do {

				//Get next token
				token = sc.yylex();
				String tokenTypeString = token.getType().toString();
//				System.out.println("token: " + token); //TODO DEBUG
				
				//Ignore whitespace and comments
				if(token.getType() == TokenTypeEnum.WHITE || token.getType() == TokenTypeEnum.CMMNT) 
					continue;
				
				//Check if token type isn't part of LL(1) table
				if(!terminals.contains(tokenTypeString))
					throw new LL1Exception();
				
				TreeNode check = parse_stack.pop();
				String check_noid = check.getName().substring(0, check.getName().indexOf('_'));
//				System.out.println("check 1: " + check); //TODO DEBUG
				
				//While stack top is non_terminal
				while(non_terminals.contains(check_noid)) {
					String tab = parse_table.get(check_noid).get(terminals.get(terminals.indexOf(tokenTypeString)));
					
					//Check if empty tab
					if(tab == null || tab.isEmpty() || tab.equals(""))
						throw new LL1Exception();
					
					String[] tab_tokens = tab.split(";");
//					System.out.println("tab " + Arrays.toString(tab_tokens)); //TODO DEBUG
					
					
					//Insert Grammar rule to stack (in reverse order)
					for(int i = tab_tokens.length - 1; i >= 0; i--) {
						TreeNode node = new TreeNode(tab_tokens[i] + "_" + nodeId++);
						parse_stack.push(node);
						check.addChild(node);
					}
					
					
//					System.out.println("stack: " + parse_stack); //TODO DEBUG
					check = parse_stack.pop();
					check_noid = check.getName().substring(0, check.getName().indexOf('_'));
					
					//If Epsilon, discard it and continue
					if(check_noid.equals(EPSILON_ID)) {
						check = parse_stack.pop();
						check_noid = check.getName().substring(0, check.getName().indexOf('_'));
						continue;
					}
				}
				
//				System.out.println("check 2: " + check); //TODO DEBUG
//				System.out.println("stack: " + parse_stack); //TODO DEBUG
				
				//Terminal from stack isn't equal to Terminal from input
				if(!check_noid.equals(tokenTypeString))
					throw new LL1Exception();
				
				// Insert ID attributes to graph
				if(token.getType() == TokenTypeEnum.ID)
					check.addChild(new TreeNode(token.getAttribute() + "_" + nodeId++));

			} while(token.getType() != TokenTypeEnum.EOF);
			
			
			//FIXME depend on EOF token or on "$"?
			if(parse_stack.empty() || !parse_stack.peek().getName().equals(END_OF_INPUT_ID))
				throw new LL1Exception();

			System.out.println("Done."); //TODO DEBUG
			return root;
			
			
		} catch (FileNotFoundException e) {
			System.err.println("[ERROR] Input File not Found: " + e.toString());
			return null;
		} catch (LL1Exception e) {
			System.err.println("input is wrong according to LL(1) table");
			return null;
		}
	}
	
	
	/**
	 * @param configFilename : Name of config file describing LL(1) table.
	 * @return HashMap of HashMaps defining each tab in the LL(1) table.
	 */
	private HashMap<String, HashMap<String, String>> parseConfigFile(String configFilename) {
		
		System.out.println("Parsing Config File..."); //TODO DEBUG
		HashMap<String, HashMap<String, String>> table = new HashMap<String, HashMap<String,String>>();
		BufferedReader reader;
		String line;
		
		try {
			
			reader = new BufferedReader(new FileReader(configFilename));
			
			//Handle Terminals - must be first row
			line = reader.readLine();
			
			if(line == null || !line.substring(0, line.indexOf('=')).equals(TERMINALS_ID)) {
				System.err.println("[ERROR] Invalid config file");
				reader.close();
				return null;
			}
			
			terminals = new ArrayList<String>(Arrays.asList(line.substring(line.indexOf('=') + 1, line.length()).split(",")));
//			System.out.println("terminals: " + terminals); //TODO DEBUG
			
			
			while((line = reader.readLine()) != null) {
				
				//Ignore comments in config file
				if(line.charAt(0) == '#')
					continue; 
				
				String non_terminal = line.substring(0, line.indexOf('='));
				non_terminals.add(non_terminal);
				
				
				String[] products = line.substring(line.indexOf('=') + 1, line.length()).split(",", -1);
				
				HashMap<String, String> map = new HashMap<String, String>();
				
				for(int i = 0; i < products.length; i++) {
					map.put(terminals.get(i), products[i]);
				}
				
				
				table.put(non_terminal, map);
			}

			reader.close();
			
		} catch (FileNotFoundException e) {
			System.err.println("[ERROR] Config File not Found: " + e.toString());
			return null;
		} catch (IOException e) {
			System.err.println("[ERROR] Failed reading from config file" + e.toString());
			return null;
		}
		
		System.out.println("Successfuly Generated LL1 Table."); //TODO DEBUG
		return table;
	}

}
