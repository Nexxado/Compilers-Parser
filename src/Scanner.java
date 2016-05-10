import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;


public class Scanner {

	private CharReader charReader;
	private char c;
	private Map<String, TokenTypeEnum> reserved;

	public Scanner(String filename) throws FileNotFoundException {

		charReader = new CharReader(filename);
		reserved = new LinkedHashMap<String, TokenTypeEnum>();

		//save reserved words in Map
		reserved.put("int", TokenTypeEnum.INT);
		reserved.put("func", TokenTypeEnum.FUNC);
		reserved.put("main", TokenTypeEnum.MAIN);
		reserved.put("IF", TokenTypeEnum.IF);
		reserved.put("then", TokenTypeEnum.THEN);
		reserved.put("else", TokenTypeEnum.ELSE);
		
		try {
			//get first char
			c = charReader.getChar();
			
		} catch (IOException e) {
			System.out.println("[ERROR] Couldn't read file: " + e.toString());
			e.printStackTrace();
		}
	}



	public TokenInfo yylex() {

		int line = charReader.getLine();


		try {

			if(c == (char)-1) {
				return new TokenInfo(TokenTypeEnum.EOF, "", line);
			}


			switch(c) {

			case '(':
				c = charReader.getChar();
				return new TokenInfo(TokenTypeEnum.LP, "", line);

			case ')':
				c = charReader.getChar();
				return new TokenInfo(TokenTypeEnum.RP, "", line);

			case '{':
				c = charReader.getChar();
				return new TokenInfo(TokenTypeEnum.LC, "", line);

			case '}':
				c = charReader.getChar();
				return new TokenInfo(TokenTypeEnum.RC, "", line);

			case ';':
				c = charReader.getChar();
				return new TokenInfo(TokenTypeEnum.SC, "", line);

			case '+':
				c = charReader.getChar();
				return new TokenInfo(TokenTypeEnum.PLUS, "", line);

			case '-':
				c = charReader.getChar();
				return new TokenInfo(TokenTypeEnum.MINUS, "", line);

			case '*':
				c = charReader.getChar();
				return new TokenInfo(TokenTypeEnum.MULT, "", line);

			case '/':

				c = charReader.getChar();

				if(c == '/') {
					
					while(c != '\n') {
						c = charReader.getChar();
					}
					
					return new TokenInfo(TokenTypeEnum.CMMNT, "", line);

				} else if(c == '*') {
					
					while(c != (char)-1) {
						c = charReader.getChar();
						
						if(c == '*') {
							c = charReader.getChar();
							if(c == '/') {
								c = charReader.getChar();
								return new TokenInfo(TokenTypeEnum.CMMNT, "", line);
							}
						}
					}
					
					return new TokenInfo(TokenTypeEnum.CMMNT, "", line); //FIXME handle error with /* comment

				} else {
					
					return new TokenInfo(TokenTypeEnum.DIV, "", line);	
				}

			case '=':
				c = charReader.getChar();
				
				if(c == '=') {
					c = charReader.getChar();
					return new TokenInfo(TokenTypeEnum.REL, "==", line);
				
				} else {
					return new TokenInfo(TokenTypeEnum.ASSIGN, "", line);					
				}

			case '<':
			case '>':
				String attrib = "" + c;
				c = charReader.getChar();
				
				if(c == '=') {
					attrib += c;
					c = charReader.getChar();
				}
				
				return new TokenInfo(TokenTypeEnum.REL, attrib, line);
				
			case '!':
				c = charReader.getChar();
				
				if(c == '=') {
					c = charReader.getChar();
					return new TokenInfo(TokenTypeEnum.REL, "!=", line);
				
				} else {
					return new TokenInfo(TokenTypeEnum.ERROR, "", line);
				}
				
			}

			
			StringBuilder builder = new StringBuilder();
			
			//Check if FID or ID or Reserved word
			if(Character.isLetter(c)) {
				
				boolean isFid = Character.getType(c) == Character.UPPERCASE_LETTER;
				
				while(Character.isLetter(c) || Character.isDigit(c)) {
					builder.append(c);
					c = charReader.getChar();
				}
				
				TokenTypeEnum tokenType = reserved.get(builder.toString());
				if(tokenType != null) {
					return new TokenInfo(tokenType, "", line);
				
				} else {
					tokenType = isFid ? TokenTypeEnum.FID : TokenTypeEnum.ID; 
					return new TokenInfo(tokenType, builder.toString(), line);
				}
				
			//Check if Num
			} else if(Character.isDigit(c)) {
				
				while(Character.isDigit(c) || c == '.') {
					builder.append(c);
					c = charReader.getChar();
				}
				return new TokenInfo(TokenTypeEnum.NUM, builder.toString(), line);
			
			//Check if Whitespace
			} else if (Character.isWhitespace(c)) {
				
				while(Character.isWhitespace(c)) {
					c = charReader.getChar();
				}
				
				return new TokenInfo(TokenTypeEnum.WHITE, "", line);
			}


		} catch (IOException e) {
			System.out.println("[ERROR] Couldn't read file: " + e.toString());
			e.printStackTrace();
		}

		
		//Doesn't match any pattern, return ERROR token
		try {
			c = charReader.getChar();
		} catch (IOException e) {
			System.out.println("[ERROR] Couldn't read file: " + e.toString());
			e.printStackTrace();
		}
		return new TokenInfo(TokenTypeEnum.ERROR, "", line);
	}

}
