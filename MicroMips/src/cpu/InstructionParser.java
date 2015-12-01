package cpu;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class InstructionParser {
	public static Instruction[] parseFile(File f) throws FileNotFoundException {
		String[] set = FileReader.getInstructionFile(new File("src/test.txt"));
		Instruction[] out = new Instruction[set.length];
		
		for (int i = 0; i < set.length; i++) {
			out[i] = parse(i,set[i]);
		}

		return out;
	}
	
	public static Instruction parse(int index, String s) {
		String label = "", op, args;
		if (s.contains(":")) {
			String[] lbl = s.split(":");
			label = lbl[0].trim();
			s = lbl[1].trim();
		}
		
		if (s.contains(";")) {
			String[] lbl = s.split(";");
			s = lbl[0];
		}
				
		if (s.trim().length() == 0)
			return null;
		
		Scanner io = new Scanner(s);
		
		op = io.next();
		args = io.nextLine().trim();
		
		io.close();
		Instruction newInst = null;
		
		try {
			newInst = new Instruction(label, op, args, index << 2);
		} catch (Exception e) {
			System.err.println("lol it broke");
			e.printStackTrace();
		}
		
		return newInst;
	}
}