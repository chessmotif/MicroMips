import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;

public class InstructionParser {
	public static ArrayList<Instruction> parse(File f) throws Exception {
		Scanner io = new Scanner(f);
		ArrayList<Instruction> opcodes = new ArrayList<Instruction>();
		
		while(io.hasNextLine()) {
			char[] in = io.nextLine().trim().toCharArray();
			String instruction = "";
			
			for (int i = 0; i < in.length; i++) {
				// first thing in a line is the instruction
				// mamaya na si label, smartass
				while (i < in.length && in[i] != ' ' && in[i] != ';') {
					instruction += in[i];
					i++;
				}
				
				// skip all white space between instruction and the first arg
				while (i < in.length && in[i] == ' ')
					i++;
				
				// next are the arguments
				int argCount = InstructionFormat.getArgCount(instruction);
				char regType = InstructionFormat.getRegisterType(instruction);
				int regCount = InstructionFormat.getRegisterCount(instruction);
				
				if (InstructionFormat.isLoadStore(instruction)) {
					// expect a register
					if (in[i] != regType)
						throw new Exception(regType + "x register expected at col" + i);
					
					String rd = "";
					while (i < in.length && in[i] != ' ' && in[i] != ';' && in[i] != ',') {
						rd += in[i];
						i++;
					}

					// skip white space
					while (i < in.length && in[i] == ' ')
						i++;
					
					// get an immediate
					String imm = "";
					while (i < in.length && in[i] != ' ' && in[i] != ';' && in[i] != '(') {
						imm += in[i];
						i++;
					}
					
					// skip white space
					while (i < in.length && in[i] == ' ')
						i++;
					
					// expect a register
					if (in[i] != regType)
						throw new Exception(regType + "x register expected at col" + i);

					String rs = "";
					while (i < in.length && in[i] != ' ' && in[i] != ';' && in[i] != ',') {
						rd += in[i];
						i++;
					}
				}
				else {
					instruction += " ";
				
					while (i < in.length && argCount != 0) {
						if (regCount > 0) {
							if (in[i] != regType)
								throw new Exception(regType + "x register expected at col" + i);
							
							while (i < in.length && in[i] != ' ' && in[i] != ';' && in[i] != ',') {
								instruction += in[i];
								i++;
							}
							
							// skip white space
							while (i < in.length && in[i] == ' ')
								i++;
							
							// skip one comma and all white space after it
							if (in[i] == ',') {
								i++;
								while (i < in.length && in[i] == ' ')
									i++;
							}

							argCount--;
							regCount--;
						}
						else {
							// expecting an immediate
							
							while (i < in.length && in[i] != ' ' && in[i] != ';' && in[i] != ',') {
								instruction += in[i];
								i++;
							}
							
							argCount--;
						}
					}
				}
			}
		}
		
		io.close();
		return opcodes;
	}
	
	public static int parseInstruction(String in) {
		String[] arr = in.split("[ ,]"); // assume INST Rx,Ry,Rz muna
		String type = InstructionFormat.getType(arr[0]);
		
		switch(type) {
			case "R-type":
				return generateRTypeOpcode(arr);
			case "Extended R-type":
				return generateExtRTypeOpcode(arr);
			case "I-type":
				return generateITypeOpcode(arr);
			case "J-type":
				return generateJTypeOpcode(arr);
		}
		
		return 0;
	}
	
	private static int generateRTypeOpcode(String[] arr) {
		int output = 0;
		int rs, rt, rd;
		
		if (arr[0].equals("DMULT")) {
			rs = Integer.parseInt(arr[1].substring(1));
			rt = Integer.parseInt(arr[2].substring(1));
			rd = 0;
		}
		else {
			rs = Integer.parseInt(arr[2].substring(1));
			rt = Integer.parseInt(arr[3].substring(1));
			rd = Integer.parseInt(arr[1].substring(1));
		}
		
		output |= rt;
		
		output <<= 5;
		output |= rs;
		
		output <<= 5;
		output |= rd;

		// 0
		output <<= 5;
		
		output <<= 6;
		output |= InstructionFormat.getFunc(arr[0]);
		return output;
	}

	private static int generateExtRTypeOpcode(String[] arr) {
		int output = 0;
		int rs, rt, rd;
		
		rd = Integer.parseInt(arr[3].substring(1));
		rs = Integer.parseInt(arr[2].substring(1));
		rt = Integer.parseInt(arr[1].substring(1));
		
		
		output |= InstructionFormat.getOpcode(arr[0]);
		
		output <<= 6;
		output |= InstructionFormat.getExtOpcode(arr[0]);
		
		output <<= 5;
		output |= rd;
		
		output <<= 5;
		output |= rs;
		
		output <<= 5;
		output |= rt;

		output <<= 6;
		output |= InstructionFormat.getFunc(arr[0]);
		
		return output;
	}

	private static int generateITypeOpcode(String[] arr) {
		// wrong opcode for branch
		// cannot handle load/store
		
		int output = 0;
		
		output |= InstructionFormat.getOpcode(arr[0]);
		
		output <<= 5;
		output |= Integer.parseInt(arr[2].substring(1));
		
		output <<= 5;
		output |= Integer.parseInt(arr[1].substring(1));
		
		output <<= 16;
		output |= Integer.parseInt(arr[3]);
		
		return output;
	}

	private static int generateJTypeOpcode(String[] arr) {
		// labels are address numbers only
		int output = 0;
		
		output |= InstructionFormat.getOpcode(arr[0]);
		
		output <<= 26;
		output |= Integer.parseInt(arr[1]) >> 2;
		
		return output;
	}
}