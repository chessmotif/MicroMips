import java.util.Scanner;

public class Driver {
	private static String ZEROES = "00000000000000000000000000000000";
	public static void main(String args[]) {
		Scanner io = new Scanner(System.in);
		
		while (io.hasNextLine()) {
			Instruction inst = InstructionParser.parse(io.nextLine());

			if (inst == null) {
				System.out.println("NOP - 00000000");
				continue;
			}
				
			
			System.out.printf("%08x\n", inst.opcode);

			String s = Integer.toBinaryString(inst.opcode);

			s = s.length() < 32 ? ZEROES.substring(s.length()) + s : s;
			
			System.out.print(s.substring(0, 6));
			System.out.print(" " + s.substring(6, 11));
			System.out.print(" " + s.substring(11, 16));
			System.out.print(" " + s.substring(16, 21));
			System.out.print(" " + s.substring(21, 26));
			System.out.println(" " + s.substring(26));
		}
		
		io.close();
	}
	
}
