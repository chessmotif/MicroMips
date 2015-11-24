
public class Instruction {
	// INST R1,R2,R3
	
	public String op = "";
	public String args = "";
	
	public String label = "";
	public int opcode;
	
	public Instruction(String label, String op, String args) throws Exception {
		String type = InstructionFormat.getType(op);
		switch(type) {
			case "R-type":
				opcode = OpcodeGenerator.generateRTypeOpcode(op, args);
				break;
			case "Extended R-type":
				opcode = OpcodeGenerator.generateExtRTypeOpcode(op, args);
				break;
			case "I-type":
				opcode = OpcodeGenerator.generateITypeOpcode(op, args);
				break;
			case "J-type":
				opcode = OpcodeGenerator.generateJTypeOpcode(op, args);
				break;
			case "Shift-type":
				opcode = OpcodeGenerator.generateShiftTypeOpcode(op, args);
				break;
			case "Mem-type":
				opcode = OpcodeGenerator.generateMemTypeOpcode(op, args);
				break;
		}
		
	}
	
	public String toString() {
		return ((label.length() == 0)? "" : (label+": ")) + op + " " + args;
	}
}


class OpcodeGenerator {
	public static int generateRTypeOpcode(String op, String args) throws Exception {
		int output = 0;
		int rs, rt, rd;
		
		String[] arr = args.split(",");
		for (int i = 0; i < arr.length; i++)
			arr[i] = arr[i].trim();
		
		// TODO - throw exceptions when 1) wrong register type or 2) wrong register count
		if (op.equals("DMULT")) {
			rs = Integer.parseInt(arr[0].substring(1));
			rt = Integer.parseInt(arr[1].substring(1));
			rd = 0;
		}
		else {
			rs = Integer.parseInt(arr[1].substring(1));
			rt = Integer.parseInt(arr[2].substring(1));
			rd = Integer.parseInt(arr[0].substring(1));
		}
		
		output |= rt;
		
		output <<= 5;
		output |= rs;
		
		output <<= 5;
		output |= rd;

		// 0
		output <<= 5;
		
		output <<= 6;
		output |= InstructionFormat.getFunc(op);
		return output;

	}
	
	public static int generateExtRTypeOpcode(String op, String args) throws Exception {
		int output = 0;
		int rs, rt, rd;
		
		String[] arr = args.split(",");
		for (int i = 0; i < arr.length; i++)
			arr[i] = arr[i].trim();

		rd = Integer.parseInt(arr[2].substring(1));
		rs = Integer.parseInt(arr[1].substring(1));
		rt = Integer.parseInt(arr[0].substring(1));
		
		
		output |= InstructionFormat.getOpcode(op);
		
		output <<= 6;
		output |= InstructionFormat.getExtOpcode(op);
		
		output <<= 5;
		output |= rd;
		
		output <<= 5;
		output |= rs;
		
		output <<= 5;
		output |= rt;

		output <<= 6;
		output |= InstructionFormat.getFunc(op);
		
		return output;
	}
	
	public static int generateITypeOpcode(String op, String args) throws Exception {
		
		String[] arr = args.split(",");
		for (int i = 0; i < arr.length; i++)
			arr[i] = arr[i].trim();

		int output = 0;
		
		output |= InstructionFormat.getOpcode(op);
		
		output <<= 5;
		output |= Integer.parseInt(arr[1].substring(1));
		
		output <<= 5;
		output |= Integer.parseInt(arr[0].substring(1));
		
		output <<= 16;
		output |= Integer.parseInt(arr[2]);
		
		return output;
	}

	public static int generateMemTypeOpcode(String op, String args) throws Exception {
		
		String a, b, imm;
		
		String[] lbl = args.split(",");
		b = lbl[0].trim(); // R5, 2000(R0) -> R5 AND 2000(R0)
		
		lbl = lbl[1].split("[()]"); // 2000(R0) -> 2000 AND R0 AND ''
		a = lbl[0].trim();
		imm = lbl[1].trim();
		
		int output = 0;
		
		output |= InstructionFormat.getOpcode(op);
		
		output <<= 5;
		output |= Integer.parseInt(a.substring(1));
		
		output <<= 5;
		output |= Integer.parseInt(b.substring(1));
		
		output <<= 16;
		output |= Integer.parseInt(imm);
		
		return output;
	}

	public static int generateJTypeOpcode(String op, String args) {
		// labels are address numbers only for now
		int output = 0;
		
		output |= InstructionFormat.getOpcode(op);
		
		output <<= 26;
		// this sucks and is wrong, consult memory first for the label
		output |= Integer.parseInt(args) >> 2;
		
		return output;
	}
	
	public static int generateShiftTypeOpcode(String op, String args) {
		String[] arr = args.split(",");
		for (int i = 0; i < arr.length; i++)
			arr[i] = arr[i].trim();

		int output = 0;
		
		output |= Integer.parseInt(arr[1].substring(1));
		
		output <<= 5;
		output |= Integer.parseInt(arr[0].substring(1));
		
		output <<= 5;
		output |= Integer.parseInt(arr[2].substring(1));
		
		output <<= 6;
		output |= InstructionFormat.getFunc(op);

		return output;
	}
}
