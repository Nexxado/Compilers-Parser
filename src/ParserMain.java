import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;


public class ParserMain {



	public static void main(String[] args) {

		if(args == null || args.length == 0 || args[0] == null) {
			System.out.println("[ERROR] No filename passed");
			return;
		}

		Scanner sc;
		String tokenFilename;
		File tokenFile;
		FileWriter writer;
		TokenInfo token;

		try {

			sc = new Scanner(args[0]);
			tokenFilename = args[0].substring(0, args[0].lastIndexOf('.')) + ".token";
			tokenFile = new File(tokenFilename);
			writer = new FileWriter(tokenFile);
			
			
			do {

				token = sc.yylex();
				if(token.getType() != TokenTypeEnum.WHITE && token.getType() != TokenTypeEnum.CMMNT && token.getType() != TokenTypeEnum.EOF) {
					
//					System.out.println(token); //TODO DEBUG
					writer.write(token.toString() + "\n");
				}

			} while(token.getType() != TokenTypeEnum.EOF);

			writer.close();

			
			
		} catch (FileNotFoundException e) {

			System.out.println("[ERROR] File not Found: " + e.toString());
			return;
			
		} catch (IOException e) {
			
			System.out.println("[ERROR] Could not open file: " + e.toString());
			return;
		}

	}





}
