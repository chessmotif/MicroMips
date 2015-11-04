import java.util.Scanner;

public class Driver {
	private final static String ZEROES = "00000000000000000000000000000000";

	public static void main(String args[]) {
		Scanner io = new Scanner(System.in);
		
		while (io.hasNextLine()) {
			int opcode = InstructionParser.parseInstruction(io.nextLine());
			System.out.printf("0x%08x    ", opcode);
			String s = Integer.toBinaryString(opcode);

			s = s.length() < 32 ? ZEROES.substring(s.length()) + s : s;
			
			System.out.print(s.substring(0, 6));
			System.out.print(" " + s.substring(6, 11));
			System.out.print(" " + s.substring(11, 16));
			System.out.print(" " + s.substring(16, 20));
			System.out.print(" " + s.substring(20, 24));
			System.out.print(" " + s.substring(24, 28));
			System.out.println(" " + s.substring(28));
		}
		
		io.close();
	}
}
