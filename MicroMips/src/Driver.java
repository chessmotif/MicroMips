import java.io.File;
import java.io.FileNotFoundException;

import ui.MipsFrame;
import cpu.Instruction;
import cpu.InstructionParser;
import cpu.PipelinedCPU;

public class Driver {
	
	public static void main(String args[]) throws FileNotFoundException {	
		MipsFrame window = new MipsFrame("NanoMips");
//
//		for (int i = 0; i < instSet.length; i++)
//			printInstructionOpcode(instSet[i]);
//		System.out.println();
//		
//		PipelinedCPU.runAllInstructions();
//		
//		System.out.println();
	}
	
	public static void printInstructionOpcode(Instruction inst) {
//		System.out.print(inst);
//		System.out.printf(" - %08x\n", inst.opcode);
//
//		String s = Integer.toBinaryString(inst.opcode);
//
//		s = s.length() < 32 ? ZEROES.substring(s.length()) + s : s;
//		
//		System.out.print(s.substring(0, 6));
//		System.out.print(" " + s.substring(6, 11));
//		System.out.print(" " + s.substring(11, 16));
//		System.out.print(" " + s.substring(16, 21));
//		System.out.print(" " + s.substring(21, 26));
//		System.out.println(" " + s.substring(26));
	}
}
