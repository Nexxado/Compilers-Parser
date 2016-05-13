package syntax;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;

import lexcial.Scanner;
import lexcial.TokenInfo;
import lexcial.TokenTypeEnum;

public class SyntaxMain {
	
	final static String TERMINALS_ID = "terminals";
	final static String EPSILON_ID = "eps";
	final static String END_OF_INPUT_ID = "$";
	static ArrayList<String> terminals;
	static ArrayList<String> non_terminals = new ArrayList<String>();

	
	public static void main(String[] args) {
		
		
		if(args == null || args.length < 2 || args[0] == null || args[1] == null) {
			System.err.println("[ERROR] Must pass config file followed by input file");
			return;
		}
		
		String configFilename = args[0];
		String inputFilename = args[1];
		
		HashMap<String, HashMap<String, String>> parse_table = parseConfigFile(configFilename);
		
		if(parse_table == null)
			return;
		
		//Initialize Stack
		int graphNodeId = 0;
		Stack<String> parse_stack = new Stack<String>();
		parse_stack.push(END_OF_INPUT_ID);
		parse_stack.push(non_terminals.get(0) + "_" + graphNodeId++);
		
		System.out.println("non_terminals: " + non_terminals); //TODO DEBUG
		System.out.println("parse table: " + parse_table); //TODO DEBUG
		System.out.println("parse stack: " + parse_stack); //TODO DEBUG
		
		Scanner sc;
		String outputFilename;
		File outputFile;
		FileWriter writer;
		TokenInfo token;
		
		StringBuilder builder = new StringBuilder();
		builder.append("digraph G {" + System.lineSeparator()); //System.lineSeparator() = get system-dependent new line char
		
		try {
			
			sc = new Scanner(inputFilename);
			
			do {

				//Get next token
				token = sc.yylex();
				String tokenTypeString = token.getType().toString();
				System.out.println("token: " + token); //TODO DEBUG
				
				//Ignore whitespace and comments
				if(token.getType() == TokenTypeEnum.WHITE || token.getType() == TokenTypeEnum.CMMNT) 
					continue;
				
				//Check if token type isn't part of LL(1) table
				if(!terminals.contains(tokenTypeString))
					throw new LL1Exception();
				
				String check = parse_stack.pop();
				String check_noid = check.substring(0, check.indexOf('_'));
				System.out.println("check 1: " + check); //TODO DEBUG
				
				//While stack top is non_terminal
				while(non_terminals.contains(check_noid)) {
					String tab = parse_table.get(check_noid).get(terminals.get(terminals.indexOf(tokenTypeString)));
					
					//Check if empty tab
					if(tab == null || tab.isEmpty() || tab.equals(""))
						throw new LL1Exception();
					
					String[] tab_tokens = tab.split(";");
					System.out.println("tab " + Arrays.toString(tab_tokens)); //TODO DEBUG
					
					ArrayList<GraphNode> parse_sub_tree = new ArrayList<GraphNode>();
					
					//Insert Grammar rule to stack and graph (in reverse order)
					for(int i = tab_tokens.length - 1; i >= 0; i--) {
						parse_stack.push(tab_tokens[i] + "_" + graphNodeId);
						parse_sub_tree.add(new GraphNode(check, tab_tokens[i] + "_" + graphNodeId++));
					}
					
					Collections.reverse(parse_sub_tree);
					for(int i = 0; i < parse_sub_tree.size(); i++) {
						builder.append(parse_sub_tree.get(i) + System.lineSeparator());
					}
					
					System.out.println("stack: " + parse_stack); //TODO DEBUG
					System.out.println("******* \ngraph: " + builder.toString() + "********"); //TODO DEBUG
					check = parse_stack.pop();
					check_noid = check.substring(0, check.indexOf('_'));
					
					//If Epsilon, discard it and continue
					if(check_noid.equals(EPSILON_ID)) {
						check = parse_stack.pop();
						check_noid = check.substring(0, check.indexOf('_'));
						continue;
					}
				}
				
				System.out.println("check 2: " + check); //TODO DEBUG
				System.out.println("stack: " + parse_stack); //TODO DEBUG
				
				//Terminal from stack isn't equal to Terminal from input
				if(!check_noid.equals(tokenTypeString))
					throw new LL1Exception();
				
				// Insert ID attributes to graph
				if(token.getType() == TokenTypeEnum.ID) 
					builder.append(new GraphNode(check, token.getAttribute() + "_" + graphNodeId++)  + System.lineSeparator());

			} while(token.getType() != TokenTypeEnum.EOF);
			
			
			//FIXME depend on EOF token or on "$"?
			if(parse_stack.empty() || !parse_stack.peek().equals(END_OF_INPUT_ID))
				throw new LL1Exception();

			builder.append("}");
			
			//Write graph string to output file
			outputFilename = inputFilename.substring(0, inputFilename.lastIndexOf('.')) + ".ptree";
			outputFile = new File(outputFilename);
			
			writer = new FileWriter(outputFile);
			writer.write(builder.toString());
			writer.close();
			
			
		} catch (FileNotFoundException e) {
			System.err.println("[ERROR] Input File not Found: " + e.toString());
			return;
		} catch (IOException e) {
			System.err.println("[ERROR] Failed writing to output file: " + e.toString());
			return;
		} catch (LL1Exception e) {
			System.err.println("input is wrong according to LL(1) table");
		}
		
		
	}

	
	
	
	/**
	 * @param configFilename : Name of config file describing LL(1) table.
	 * @return HashMap of HashMaps defining each tab in the LL(1) table.
	 */
	private static HashMap<String, HashMap<String, String>> parseConfigFile(String configFilename) {
		
		System.out.println("Parsing Config File"); //TODO DEBUG
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
			
//			System.out.println("line read: " + line); //TODO DEBUG
			terminals = new ArrayList<String>(Arrays.asList(line.substring(line.indexOf('=') + 1, line.length()).split(",")));
			System.out.println("terminals: " + terminals); //TODO DEBUG
			
			
			while((line = reader.readLine()) != null) {
				
//				System.out.println("line read: " + line); //TODO DEBUG
				//Ignore comments in config file
				if(line.charAt(0) == '#')
					continue; 
				
				String non_terminal = line.substring(0, line.indexOf('='));
//				System.out.println("non_terminal: " + non_terminal); //TODO DEBUG
				non_terminals.add(non_terminal);
				
				
				String[] products = line.substring(line.indexOf('=') + 1, line.length()).split(",", -1);
//				System.out.println("products: " + Arrays.toString(products)); //TODO DEBUG
				
				HashMap<String, String> map = new HashMap<String, String>();
				
				for(int i = 0; i < products.length; i++) {
					map.put(terminals.get(i), products[i]);
				}
				
//				System.out.println(non_terminal + " map: " + map); //TODO DEBUG
				
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
		
		
		return table;
	}

}
