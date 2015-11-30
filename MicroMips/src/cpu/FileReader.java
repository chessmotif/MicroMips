package cpu;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileReader {
	public static String[] getInstructionFile(File f) throws FileNotFoundException {
		Scanner io = new Scanner(f);
		ArrayList<String> instSet = new ArrayList<String>();
		
		while (io.hasNextLine()) {
			String line = io.nextLine().trim();
			if (line.length() == 0) {
				continue;
			}
			
			instSet.add(line);
		}
		
		io.close();
		
		int size = instSet.size();
		String[] out = new String[size];
		
		return instSet.toArray(out);
	}
}
