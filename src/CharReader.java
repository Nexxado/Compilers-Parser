import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;


public class CharReader {
	
	private LineNumberReader lineReader;
	
	public CharReader(String filename) throws FileNotFoundException {
		
		lineReader = new LineNumberReader(new FileReader(filename));
	}

	//Check if return value is -1
	public char getChar() throws IOException {
		return (char)lineReader.read();
	}
	
	public int getLine() {
		return lineReader.getLineNumber();
	}
	
}
