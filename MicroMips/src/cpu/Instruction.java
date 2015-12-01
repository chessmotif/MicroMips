package cpu;

public class Instruction {
	// INST R1,R2,R3
	
	public String op = "";
	public String args = "";
	
	public String label = "";
	public int opcode;
	public int add;
	
	public Instruction(String label, String op, String args, int add) {
		this.op = op;
		this.args = args;
		this.label = label;
		this.add = add;
	}
	
	public void generateOpcode() throws Exception {
		String type = InstructionFormat.getType(op);
		switch(type) {
			case "R-type":
				opcode = OpcodeGenerator.generateRTypeOpcode(op, args);
				break;
			case "Extended R-type":
				opcode = OpcodeGenerator.generateExtRTypeOpcode(op, args);
				break;
			case "I-type":
				opcode = OpcodeGenerator.generateITypeOpcode(add, op, args);
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
		return op + " " + args;
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
		
		output |= rs;
		
		output <<= 5;
		output |= rt;
		
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
		
		output <<= 5;
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
	
	public static int generateITypeOpcode(int startAdd, String op, String args) throws Exception {
		String hexConvert = "0123456789ABCDEF";
		String isHex = "[0-9A-Fa-f]+";
		
		String[] arr = args.split(",");
		for (int i = 0; i < arr.length; i++)
			arr[i] = arr[i].trim();
		
		int output = 0;
		
		if (InstructionFormat.getOpcode(op) != 4) {
			String add = arr[2];
			if (add.length() > 4) {
				add = add.substring(add.length()-4);
			}
			else if (add.length() < 4) {
				String z = "0000";
				add = add.length() < 4 ? z.substring(add.length()) + add : add;
			}
			
			output |= InstructionFormat.getOpcode(op);
			
			output <<= 5;
			output |= Integer.parseInt(arr[1].substring(1));
			
			output <<= 5;
			output |= Integer.parseInt(arr[0].substring(1));
	
			for (int i = 0; i < 4; i++) {
				output <<= 4;
				output |= hexConvert.indexOf(add.charAt(i));
			}
		}
		else {
			output |= InstructionFormat.getOpcode(op);
			
			output <<= 5;
			output |= Integer.parseInt(arr[0].substring(1));
			
			output <<= 5;
			output |= Integer.parseInt(arr[1].substring(1));
			
			String add = arr[2];
			boolean hex = add.matches(isHex);
			
			if (hex) {
				if (add.length() > 4) {
					add = add.substring(add.length()-4);
				}
				else if (add.length() < 4) {
					String z = "0000";
					add = add.length() < 4 ? z.substring(add.length()) + add : add;
				}
	
				for (int i = 0; i < 4; i++) {
					output <<= 4;
					output |= hexConvert.indexOf(add.charAt(i));
				}
			}
			else {
				Long a = PipelinedCPU.labelMap.get(add);
				
				if (a == null)
					throw new Exception("label not found");
				
				a = a - (startAdd + 4);
				a >>= 2;

				output <<= 16;
				
				output |= a & 0xFFFFL;
			}
		}
		
		return output;
	}

	public static int generateMemTypeOpcode(String op, String args) throws Exception {
		String hexConvert = "0123456789ABCDEF";
		String a, b;
		String add;
		
		String[] lbl = args.split(",");
		b = lbl[0].trim(); // R5, 2000(R0) -> R5 AND 2000(R0)
		
		lbl = lbl[1].split("[()]"); // 2000(R0) -> 2000 AND R0 AND ''
		a = lbl[1].trim();
		add = lbl[0].trim();
		if (add.length() > 4) {
			add = add.substring(add.length()-4);
		}
		else if (add.length() < 4) {
			String z = "0000";
			add = add.length() < 4 ? z.substring(add.length()) + add : add;
		}
	
		int output = 0;
		
		output |= InstructionFormat.getOpcode(op);
		
		output <<= 5;
		output |= Integer.parseInt(a.substring(1));
		
		output <<= 5;
		output |= Integer.parseInt(b.substring(1));
		
		for (int i = 0; i < 4; i++) {
			output <<= 4;
			output |= hexConvert.indexOf(add.charAt(i));
		}
		
		return output;
	}

	public static int generateJTypeOpcode(String op, String args) throws Exception {
		String hexConvert = "0123456789ABCDEF";
		String isHex = "[0-9A-Fa-f]+";
		int output = 0;
		
		output |= InstructionFormat.getOpcode(op);
		
		if (args.matches(isHex)) {
			if (args.length() > 4) {
				args = args.substring(args.length()-4);
			}
			else if (args.length() < 4) {
				String z = "0000";
				args = args.length() < 4 ? z.substring(args.length()) + args : args;
			}

			int jumpAdd = 0;
			for (int i = 0; i < 4; i++) {
				jumpAdd <<= 4;
				jumpAdd |= hexConvert.indexOf(args.charAt(i));
			}
			
			output <<= 26;
			output |= jumpAdd >> 2;
		}
		else {
			Long a = PipelinedCPU.labelMap.get(args);
			
			if (a == null)
				throw new Exception("label not found");
			
			a >>= 2;

			output <<= 26;
			
			output |= a & 0xFFFFL;
		}
		
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
		output |= Integer.parseInt(arr[2]);
		
		output <<= 6;
		output |= InstructionFormat.getFunc(op);

		return output;
	}
}
