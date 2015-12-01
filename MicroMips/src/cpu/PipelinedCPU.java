package cpu;

import java.math.BigInteger;
import java.util.HashMap;

import ui.MipsFrame;

public class PipelinedCPU {
	// registers
	public static long[] Regs = new long[32];
	public static long[] FRegs = new long[12];
	
	public static InstructionStack opcodeStack = new InstructionStack(0,0x4000);
	public static DataStack dataStack = new DataStack(0x2000,0x4000);
	
	public static long HI = 0, LO = 0;

	// board state
	public static int PC = 0;
	public static State prev = new State();
	public static State curr = new State();
	public static PipelineMap map = new PipelineMap();
	public static HashMap<String, Long> labelMap = new HashMap<String, Long>(); // DO THIS NIGGER
	
	private static boolean stallFlag = false;
	private static boolean jumpFlag = false;
	
	public static void runOneClockCycle(MipsFrame p) {

		if (jumpFlag) {
			prev.IF_ID.IR = 0;
			prev.ID_EX.IR = 0;
			prev.EX_MEM.IR = 0;
		}

		runWB();
		Regs[0] = 0;

		runMEM();
		runEX();
		runFAdd();
		runFMult();
		runID();
		if (!stallFlag)
			runIF();
		
		prev = curr;
		curr.cycle++;
		
		p.updateUITable();
	}
	
	public static void runAllInstructions(MipsFrame p) {
//		printPhases();
		if (keepRunning())
			runOneClockCycle(p);
//		printPhases();
		while (!keepRunning()) {
			runOneClockCycle(p);
//			printPhases();
		}
	}
	
	private static boolean keepRunning() {
		boolean out = curr.IF_ID.IR == 0 && curr.ID_EX.IR == 0 && curr.EX_MEM.IR == 0 && curr.MEM_WB.IR == 0;
		
		for (int i = 0; i < curr.EX_MEM_FAdd.length; i++)
			out = out && (curr.EX_MEM_FAdd[i].IR == 0);
		for (int i = 0; i < curr.EX_MEM_FMult.length; i++)
			out = out && (curr.EX_MEM_FMult[i].IR == 0);

		return out;
	}
	
	private static void runWB() {
		if (prev.MEM_WB.IR == 0) {
			curr.WB_REG.IR = 0;
			curr.WB_REG.add = -1;
			return;
		}
		
		curr.WB_REG.IR = prev.MEM_WB.IR;
		curr.WB_REG.add = prev.MEM_WB.add;
		map.signCycle(curr.WB_REG.add, curr.cycle, "WB");
		
		switch(OpcodeDecoder.getType(prev.MEM_WB.IR)) {
			case "R-type":
			case "Shift-type":
				if (OpcodeDecoder.getRC(prev.MEM_WB.IR) != 0)
					Regs[OpcodeDecoder.getRC(prev.MEM_WB.IR)] = prev.MEM_WB.ALUOutput;
				break;
			case "Extended R-type":
				FRegs[OpcodeDecoder.getRD(prev.MEM_WB.IR)] = prev.MEM_WB.ALUOutput;
				break;
			case "Mem-type":
				switch (OpcodeDecoder.getOp(prev.MEM_WB.IR)) {
					case 35:
						if (OpcodeDecoder.getRB(prev.MEM_WB.IR) != 0) {
							Regs[OpcodeDecoder.getRB(prev.MEM_WB.IR)] = prev.MEM_WB.LMD;
							if ((prev.MEM_WB.LMD & 0x8000000L) != 0)
								Regs[OpcodeDecoder.getRB(prev.MEM_WB.IR)] |= 0xFFFFFFFF00000000L;
						}
						break;
					case 39:
						if (OpcodeDecoder.getRB(prev.MEM_WB.IR) != 0)
							Regs[OpcodeDecoder.getRB(prev.MEM_WB.IR)] = prev.MEM_WB.LMD;
						break;
					case 49:
						FRegs[OpcodeDecoder.getRB(prev.MEM_WB.IR)] = prev.MEM_WB.LMD;
						break;
				}
				break;
			case "I-type":
				if (OpcodeDecoder.getRB(prev.MEM_WB.IR) != 0)
					Regs[OpcodeDecoder.getRB(prev.MEM_WB.IR)] = prev.MEM_WB.ALUOutput;
				break;
		}
		
	}
	
	private static void runMEM() {
		if (prev.EX_MEM.IR != 0) {
			curr.MEM_WB.IR = prev.EX_MEM.IR;
			curr.MEM_WB.add = prev.EX_MEM.add;
			map.signCycle(curr.MEM_WB.add, curr.cycle, "MEM");
			
			switch(OpcodeDecoder.getType(prev.EX_MEM.IR)) {
				case "R-type":
				case "Shift-type":
				case "I-type":
					curr.MEM_WB.ALUOutput = prev.EX_MEM.ALUOutput;
					break;
				case "Mem-type":
					switch (OpcodeDecoder.getOp(prev.EX_MEM.IR)) {
						case 35:	case 39:	case 49: // loads
							curr.MEM_WB.LMD = dataStack.loadData((int)prev.EX_MEM.ALUOutput);
							break;
						case 43:	case 57:
							dataStack.writeData(prev.EX_MEM.B, (int)prev.EX_MEM.ALUOutput);
							break;
					}
					break;
				case "J-type":
					curr.MEM_WB.ALUOutput = prev.EX_MEM.ALUOutput;
					curr.MEM_WB.COND = prev.EX_MEM.COND;
					break;
			}
		}
		else if (prev.EX_MEM_FAdd[3].IR != 0) {
			curr.MEM_WB.IR = prev.EX_MEM_FAdd[3].IR;
			curr.MEM_WB.add = prev.EX_MEM_FAdd[3].add;
			map.signCycle(curr.MEM_WB.add, curr.cycle, "MEM");
			
			curr.MEM_WB.ALUOutput = prev.EX_MEM_FAdd[3].ALUOutput;
		}
		else if (prev.EX_MEM_FMult[5].IR != 0) {
			curr.MEM_WB.IR = prev.EX_MEM_FMult[5].IR;
			curr.MEM_WB.add = prev.EX_MEM_FMult[5].add;
			map.signCycle(curr.MEM_WB.add, curr.cycle, "MEM");
			
			curr.MEM_WB.ALUOutput = prev.EX_MEM_FMult[5].ALUOutput;
		}
		else {
			curr.MEM_WB.IR = 0;
			curr.MEM_WB.add = -1;
		}
	}
	
	private static void runFAdd() {
		for (int i = curr.EX_MEM_FAdd.length-1; i > 0; i--) {
			if (prev.EX_MEM_FAdd[i-1].add != -1) {
				curr.EX_MEM_FAdd[i].IR = prev.EX_MEM_FAdd[i-1].IR;
				curr.EX_MEM_FAdd[i].add = prev.EX_MEM_FAdd[i-1].add;
				curr.EX_MEM_FAdd[i].ALUOutput = prev.EX_MEM_FAdd[i-1].ALUOutput;
				map.signCycle(curr.EX_MEM_FAdd[i].add, curr.cycle, "A" + (i+1));
			}
			else {
				curr.EX_MEM_FAdd[i].IR = 0;
				curr.EX_MEM_FAdd[i].add = -1;
			}
		}
		if (OpcodeDecoder.getType(prev.ID_EX.IR).equals("Extended R-type") && OpcodeDecoder.getFunc(prev.ID_EX.IR) == 0) {
			if (prev.ID_EX.add == -1) {
				curr.EX_MEM_FAdd[0].IR = 0;
				curr.EX_MEM_FAdd[0].add = -1;
				return;
			}
			
			curr.EX_MEM_FAdd[0].IR = prev.ID_EX.IR;
			curr.EX_MEM_FAdd[0].add = prev.ID_EX.add;
			curr.EX_MEM_FAdd[0].ALUOutput = Float.floatToIntBits(Float.intBitsToFloat((int)prev.ID_EX.A) + Float.intBitsToFloat((int)prev.ID_EX.B));
			
			map.signCycle(curr.EX_MEM_FAdd[0].add, curr.cycle, "A1");
		}
		else {
			curr.EX_MEM_FAdd[0].IR = 0;
			curr.EX_MEM_FAdd[0].add = -1;
		}
	}
	
	private static void runFMult() {
		for (int i = curr.EX_MEM_FMult.length-1; i > 0; i--) {
			if (prev.EX_MEM_FMult[i-1].add != -1) {
				curr.EX_MEM_FMult[i].IR = prev.EX_MEM_FMult[i-1].IR;
				curr.EX_MEM_FMult[i].add = prev.EX_MEM_FMult[i-1].add;
				map.signCycle(curr.EX_MEM_FMult[i].add, curr.cycle, "M" + (i+1));
				curr.EX_MEM_FMult[i].ALUOutput = prev.EX_MEM_FMult[i-1].ALUOutput;
			}
			else {
				curr.EX_MEM_FMult[i].IR = 0;
				curr.EX_MEM_FMult[i].add = -1;
			}
		}
		
		if (OpcodeDecoder.getType(prev.ID_EX.IR).equals("Extended R-type") && OpcodeDecoder.getFunc(prev.ID_EX.IR) == 2) {
			if (prev.ID_EX.add == -1) {
				curr.EX_MEM_FMult[0].IR = 0;
				curr.EX_MEM_FMult[0].add = -1;
				return;
			}
				
			curr.EX_MEM_FMult[0].IR = prev.ID_EX.IR;
			curr.EX_MEM_FMult[0].add = prev.ID_EX.add;
			curr.EX_MEM_FMult[0].ALUOutput = Float.floatToIntBits(Float.intBitsToFloat((int)prev.ID_EX.A) * Float.intBitsToFloat((int)prev.ID_EX.B));

			map.signCycle(curr.EX_MEM_FMult[0].add, curr.cycle, "M1");
		}
		else {
			curr.EX_MEM_FMult[0].IR = 0;
			curr.EX_MEM_FMult[0].add = -1;
		}
		
	}
	
	private static void runEX() {
		if (prev.ID_EX.IR == 0) {
			curr.EX_MEM.IR = 0;
			curr.EX_MEM.add = -1;
			curr.EX_MEM.COND = false;
			return;
		}
		
		if (OpcodeDecoder.getType(prev.ID_EX.IR).equals("Extended R-type")) {
			curr.EX_MEM.IR = 0;
			curr.EX_MEM.add = -1;
			curr.EX_MEM.COND = false;
			return;
		}

		curr.EX_MEM.IR = prev.ID_EX.IR;
		curr.EX_MEM.add = prev.ID_EX.add;

		map.signCycle(curr.EX_MEM.add, curr.cycle, "EX");

		switch(OpcodeDecoder.getType(prev.ID_EX.IR)) {
			case "R-type":
				if (OpcodeDecoder.getFunc(prev.ID_EX.IR) == 28) {
					BigInteger op1 = new BigInteger(prev.ID_EX.A + ""); 
					BigInteger op2 = new BigInteger(prev.ID_EX.B + ""); 
					BigInteger out = op1.multiply(op2);
					
					LO = out.longValue();
					HI = out.shiftRight(64).longValue();
				}
			case "Extended R-type":
				curr.EX_MEM.ALUOutput = OpcodeDecoder.executeOp(prev.ID_EX.A, prev.ID_EX.B, prev.ID_EX.IR);
				curr.EX_MEM.COND = false;
				break;
			case "I-type":
				curr.EX_MEM.ALUOutput = OpcodeDecoder.executeOp(prev.ID_EX.A, prev.ID_EX.Imm, prev.ID_EX.IR);
				break;
			case "Shift-type":
				curr.EX_MEM.ALUOutput = OpcodeDecoder.executeOp(prev.ID_EX.A, prev.ID_EX.Imm >>> 6 & 0xFFFFFL, prev.ID_EX.IR);
				break;
			case "Mem-type":
				curr.EX_MEM.ALUOutput = OpcodeDecoder.executeOp(prev.ID_EX.A, prev.ID_EX.Imm, prev.ID_EX.IR);
				curr.EX_MEM.B = prev.ID_EX.B;
				curr.EX_MEM.COND = false;
				break;
			case "J-type": // empty for now
				if (OpcodeDecoder.getOp(prev.ID_EX.IR) == 2) {
					curr.EX_MEM.ALUOutput = prev.ID_EX.Imm << 2;
					curr.EX_MEM.COND = true;
				}
				else { // branch
					curr.EX_MEM.ALUOutput = prev.ID_EX.NPC + (prev.ID_EX.Imm << 2);
					System.out.println(prev.ID_EX.A);
					System.out.println(prev.ID_EX.B);
					curr.EX_MEM.COND = prev.ID_EX.A == prev.ID_EX.B;
				}
				
				break;
		}
	}
	
	private static void runID() {
		if (prev.IF_ID.IR == 0) {
			curr.ID_EX.IR = 0;
			curr.ID_EX.add = -1;
			return;
		}
		
		// check for dependency
		int EX_opcode = prev.EX_MEM.IR;
		int MEM_opcode = prev.MEM_WB.IR;
		int WB_opcode = prev.WB_REG.IR;
		
		boolean ex = dependencyExists(prev.IF_ID.IR, EX_opcode);
		boolean mem = dependencyExists(prev.IF_ID.IR, MEM_opcode);
		boolean wb = dependencyExists(prev.IF_ID.IR, WB_opcode);

		stallFlag = ex || mem || wb;
		
		if (stallFlag) {
			curr.ID_EX.IR = 0;
			curr.ID_EX.add = -1;
			return;
		}

		curr.ID_EX.IR = prev.IF_ID.IR;
		switch(OpcodeDecoder.getType(curr.ID_EX.IR)) {
			case "R-type":
				curr.ID_EX.A = Regs[OpcodeDecoder.getRA(curr.ID_EX.IR)];
				curr.ID_EX.B = Regs[OpcodeDecoder.getRB(curr.ID_EX.IR)];
				curr.ID_EX.Imm = OpcodeDecoder.getImm(curr.ID_EX.IR);
				break;
			case "I-type":
			case "J-type":
				curr.ID_EX.A = Regs[OpcodeDecoder.getRA(curr.ID_EX.IR)];
				curr.ID_EX.B = Regs[OpcodeDecoder.getRB(curr.ID_EX.IR)];
				curr.ID_EX.Imm = OpcodeDecoder.getImm(curr.ID_EX.IR);
				
				if ((curr.ID_EX.Imm & 0x8000L) != 0) {
					curr.ID_EX.Imm |= 0xFFFFFFFFFFFF0000L;
				}
				break;
			case "Mem-type":
				curr.ID_EX.A = Regs[OpcodeDecoder.getRA(curr.ID_EX.IR)];
				if (OpcodeDecoder.getOp(curr.ID_EX.IR) == 49 || OpcodeDecoder.getOp(curr.ID_EX.IR) == 57)
					curr.ID_EX.B = FRegs[OpcodeDecoder.getRB(curr.ID_EX.IR)];
				else
					curr.ID_EX.B = Regs[OpcodeDecoder.getRB(curr.ID_EX.IR)];
				curr.ID_EX.Imm = OpcodeDecoder.getImm(curr.ID_EX.IR);
				break;
			case "Extended R-type":
				curr.ID_EX.A = FRegs[OpcodeDecoder.getRC(curr.ID_EX.IR)];
				curr.ID_EX.B = FRegs[OpcodeDecoder.getRB(curr.ID_EX.IR)];
				curr.ID_EX.Imm = OpcodeDecoder.getImm(curr.ID_EX.IR);
				break;
			case "Shift-type":
				curr.ID_EX.A = Regs[OpcodeDecoder.getRB(curr.ID_EX.IR)];
				curr.ID_EX.B = Regs[OpcodeDecoder.getRC(curr.ID_EX.IR)];
				curr.ID_EX.Imm = OpcodeDecoder.getImm(curr.ID_EX.IR);
				break;
		}
		
		curr.ID_EX.NPC = prev.IF_ID.NPC;
		curr.ID_EX.add = prev.IF_ID.add;
		map.signCycle(curr.ID_EX.add, curr.cycle, "ID");
	}
	
	private static void runIF() {
		
		curr.IF_ID.add = PC;
		curr.IF_ID.IR = opcodeStack.loadInst(PC);

		map.signCycle(curr.IF_ID.add, curr.cycle, "IF");

//		PC = PC + 4;
//		jumpedFlag = jumpFlag;
		jumpFlag = OpcodeDecoder.getType(prev.MEM_WB.IR).equals("J-type") && prev.MEM_WB.COND;
		
		PC = jumpFlag? (int)prev.MEM_WB.ALUOutput : PC + 4;
		curr.IF_ID.NPC = PC;
	}
	
	private static boolean dependencyExists(int toRun, int toCheck) {
		String arg1 = "", arg2 = "";
		String check = "";
		
		switch(OpcodeDecoder.getType(toRun)) {
			case "R-type":
			case "J-type":
				arg1 = "R" + OpcodeDecoder.getRA(toRun);
				arg2 = "R" + OpcodeDecoder.getRB(toRun);
				break;
			case "I-type":
				arg1 = "R" + OpcodeDecoder.getRA(toRun);
				arg2 = "";
				break;
			case "Mem-type":
				arg1 = "R" + OpcodeDecoder.getRA(toRun);
				
				if (OpcodeDecoder.getOp(toRun) == 49 || OpcodeDecoder.getOp(toRun) == 57)
					arg2 = "F" + OpcodeDecoder.getRB(toRun);
				else
					arg2 = "R" + OpcodeDecoder.getRB(toRun);
				break;
			case "Extended R-type":
				arg1 = "F" + OpcodeDecoder.getRC(toRun);
				arg2 = "F" + OpcodeDecoder.getRB(toRun);
				break;
			case "Shift-type":
				arg1 = "R" + OpcodeDecoder.getRB(toRun);
				arg2 = "R" + OpcodeDecoder.getRC(toRun);
				break;
		}
		
		switch(OpcodeDecoder.getType(toCheck)) { 
			case "R-type":
			case "Shift-type":
				check = "R" + OpcodeDecoder.getRC(toCheck);
				break;
			case "Extended R-type":
				check = "F" + OpcodeDecoder.getRD(toCheck);
				break;
			case "I-type":
				check = "R" + OpcodeDecoder.getRB(toCheck);
				break;
			case "Mem-type":
				if (OpcodeDecoder.getOp(toCheck) == 35 || OpcodeDecoder.getOp(toCheck) == 39)
					check = "R" + OpcodeDecoder.getRB(toCheck);
				else if (OpcodeDecoder.getOp(toCheck) == 49)
					check = "F" + OpcodeDecoder.getRB(toCheck);
				break;
			case "J-type":
				if (OpcodeDecoder.getOp(prev.IF_ID.IR) == 4)
					check = "R" + OpcodeDecoder.getRA(toCheck) + "|R" + OpcodeDecoder.getRB(toCheck);
				break;
		}
		
//		System.out.printf("Dependency check - %08x - %08x\n", toRun, toCheck);
//		System.out.println(arg1 + " -> " + check);
//		System.out.println(arg2 + " -> " + check);
		
		if (check.length() == 0)
			return false;
		if (check.charAt(1) == '0')
			return false;
		
		boolean a1 = (arg1.length() != 0) && arg1.matches(check);
		boolean a2 = (arg2.length() != 0) && arg2.matches(check);
		
		return a1 || a2;
	}

	public static void printRegisters() {
		System.out.println("------------");
		System.out.println();
		System.out.printf("IF/ID.IR = %04x %04x\n", curr.IF_ID.IR >>> 16, curr.IF_ID.IR & 0xFFFFL);
		System.out.printf("IF/ID.NPC = %04x %04x\n", curr.IF_ID.NPC >>> 16, curr.IF_ID.NPC & 0xFFFFL);
		System.out.printf("PC = %04x %04x\n", 0, PC);
		System.out.println();
		System.out.printf("ID/EX.IR = %04x %04x\n", curr.ID_EX.IR >>> 16, curr.ID_EX.IR & 0xFFFFL);
		System.out.printf("ID/EX.NPC = %04x %04x\n", curr.ID_EX.NPC >>> 16, curr.ID_EX.NPC & 0xFFFFL);
		System.out.printf("ID/EX.A = %04x %04x\n", curr.ID_EX.A >>> 16, curr.ID_EX.A & 0xFFFFL);
		System.out.printf("ID/EX.B = %04x %04x\n", curr.ID_EX.B >>> 16, curr.ID_EX.B & 0xFFFFL);
		System.out.printf("ID/EX.Imm = %04x %04x\n", 0, curr.ID_EX.Imm);
		System.out.println();
		System.out.printf("EX/MEM.IR = %04x %04x\n", curr.EX_MEM.IR >>> 16, curr.EX_MEM.IR & 0xFFFFL);
		System.out.printf("EX/MEM.ALUOutput = %04x %04x\n", curr.EX_MEM.ALUOutput >>> 16, curr.EX_MEM.ALUOutput & 0xFFFFL);
		System.out.printf("EX/MEM.B = %04x %04x\n", curr.EX_MEM.B >>> 16, curr.EX_MEM.B & 0xFFFFL);
		System.out.println("EX/MEM.COND = " + (curr.EX_MEM.COND? 1 : 0));
		System.out.println();
		System.out.printf("MEM/WB.IR = %04x %04x\n", curr.MEM_WB.IR >>> 16, curr.MEM_WB.IR & 0xFFFFL);
		System.out.printf("MEM/WB.ALUOutput = %04x %04x\n", curr.MEM_WB.ALUOutput >>> 16, curr.MEM_WB.ALUOutput & 0xFFFFL);
		System.out.printf("MEM/WB.LMD = %04x %04x\n", curr.MEM_WB.LMD >>> 16, curr.MEM_WB.LMD & 0xFFFFL);
		System.out.println();
		System.out.println("------------");
	}	
	
	public static void printPhases() {
		System.out.printf("IF.IR = %04x %04x - %04x %04x\n", curr.IF_ID.add >>> 16, curr.IF_ID.add & 0xFFFFL, curr.IF_ID.IR >>> 16, curr.IF_ID.IR & 0xFFFFL);
		System.out.printf("ID.IR = %04x %04x - %04x %04x\n", curr.ID_EX.add >>> 16, curr.ID_EX.add & 0xFFFFL, curr.ID_EX.IR >>> 16, curr.ID_EX.IR & 0xFFFFL);
		System.out.printf("EX.IR = %04x %04x - %04x %04x\n", curr.EX_MEM.add >>> 16, curr.EX_MEM.add & 0xFFFFL, curr.EX_MEM.IR >>> 16, curr.EX_MEM.IR & 0xFFFFL);
		System.out.printf("MEM.IR = %04x %04x - %04x %04x\n", curr.MEM_WB.add >>> 16, curr.MEM_WB.add & 0xFFFFL, curr.MEM_WB.IR >>> 16, curr.MEM_WB.IR & 0xFFFFL);
		System.out.printf("WB.IR = %04x %04x - %04x %04x\n", curr.WB_REG.add >>> 16, curr.WB_REG.add & 0xFFFFL, curr.WB_REG.IR >>> 16, curr.WB_REG.IR & 0xFFFFL);
		System.out.println("------------");
	}
	
	public static Object[][] getInternalRegisterData() {
		Object[][] out = {
				{"IF/ID.IR", formatString(curr.IF_ID.IR)},
				{"IF/ID.NPC", formatString(curr.IF_ID.IR)},
				{"PC", formatString(PC)},
				{"",""},
				{"ID/EX.IR", formatString(curr.ID_EX.IR)},
				{"ID/EX.NPC", formatString(curr.ID_EX.NPC)},
				{"ID/EX.A", formatString(curr.ID_EX.A)},
				{"ID/EX.B", formatString(curr.ID_EX.B)},
				{"ID/EX.Imm", formatString(curr.ID_EX.Imm)},
				{"",""},
				{"EX/MEM.IR", formatString(curr.EX_MEM.IR)},
				{"EX/MEM.ALUOutput", formatString(curr.EX_MEM.ALUOutput)},
				{"EX/MEM.B", formatString(curr.EX_MEM.B)},
				{"EX/MEM.COND", curr.EX_MEM.COND? 1 : 0},
				{"HI", formatString(HI)},
				{"LO", formatString(LO)},
				{"",""},
				{"A1/MEM.IR", formatString(curr.EX_MEM_FAdd[0].IR)},
				{"A1/MEM.ALUOutput", formatString(curr.EX_MEM_FAdd[0].ALUOutput)},
				{"A2/MEM.IR", formatString(curr.EX_MEM_FAdd[1].IR)},
				{"A2/MEM.ALUOutput", formatString(curr.EX_MEM_FAdd[1].ALUOutput)},
				{"A3/MEM.IR", formatString(curr.EX_MEM_FAdd[2].IR)},
				{"A3/MEM.ALUOutput", formatString(curr.EX_MEM_FAdd[2].ALUOutput)},
				{"A4/MEM.IR", formatString(curr.EX_MEM_FAdd[3].IR)},
				{"A4/MEM.ALUOutput", formatString(curr.EX_MEM_FAdd[3].ALUOutput)},
				{"",""},
				{"M1/MEM.IR", formatString(curr.EX_MEM_FMult[0].IR)},
				{"M1/MEM.ALUOutput", formatString(curr.EX_MEM_FMult[0].ALUOutput)},
				{"M2/MEM.IR", formatString(curr.EX_MEM_FMult[1].IR)},
				{"M2/MEM.ALUOutput", formatString(curr.EX_MEM_FMult[1].ALUOutput)},
				{"M3/MEM.IR", formatString(curr.EX_MEM_FMult[2].IR)},
				{"M3/MEM.ALUOutput", formatString(curr.EX_MEM_FMult[2].ALUOutput)},
				{"M4/MEM.IR", formatString(curr.EX_MEM_FMult[3].IR)},
				{"M4/MEM.ALUOutput", formatString(curr.EX_MEM_FMult[3].ALUOutput)},
				{"M5/MEM.IR", formatString(curr.EX_MEM_FMult[4].IR)},
				{"M5/MEM.ALUOutput", formatString(curr.EX_MEM_FMult[4].ALUOutput)},
				{"M6/MEM.IR", formatString(curr.EX_MEM_FMult[5].IR)},
				{"M6/MEM.ALUOutput", formatString(curr.EX_MEM_FMult[5].ALUOutput)},
				{"",""},
				{"MEM/WB.IR", formatString(curr.MEM_WB.IR)},
				{"MEM/WB.ALUOutput", formatString(curr.MEM_WB.ALUOutput)},
				{"MEM/WB.LMD", formatString(curr.MEM_WB.LMD)}
		};
		
		return out;
	}

	
	public static Object[][] getIntRegisterData() {
		Object[][] out = new Object[43][2];
		
		for (int i = 0; i < out.length; i++) {
			out[i][0] = i<31? ("R" + (i+1)) : ("F" + (i-31));
			out[i][1] = i<31? formatString(Regs[i+1]) : formatString(FRegs[i-31]);
		}
		
		return out;
	}

	public static Object[][] getInstructionData() {
		return opcodeStack.getInstructionData();
	}
	
	public static Object[][] getMemoryData() {
		return dataStack.getMemoryData();
	}
	
	public static void setPipelineMap(Instruction[] inst) {
		map.setInstructionSet(inst);
	}
	
	public static void reset() {
		// registers
		Regs = new long[32];
		FRegs = new long[12];
		
//		opcodeStack = new InstructionStack(0,0x4000);
//		dataStack = new DataStack(0x2000,0x4000);
		
		HI = 0;
		LO = 0;

		// board state
		PC = 0;
		prev = new State();
		curr = new State();
		map.clear();
	}
	
	public static void resetAll() {
		// registers
		Regs = new long[32];
		FRegs = new long[12];
		
//		opcodeStack = new InstructionStack(0,0x4000);
		dataStack = new DataStack(0x2000,0x4000);
		
		HI = 0;
		LO = 0;

		// board state
		PC = 0;
		prev = new State();
		curr = new State();
		map.clear();
	}
	
	public static String formatString(int p) {
		return String.format("%04x %04x", p >>> 16, p & 0xFFFFL);
	}
	
	public static String formatString(long p) {
		int hi = (int) (p >>> 32), lo = (int)p;
		return String.format("%04x %04x ", hi >>> 16, hi & 0xFFFFL) + String.format("%04x %04x", lo >>> 16, lo & 0xFFFFL);
	}
}

class State {
	public int cycle = 1;
	public IF_ID IF_ID = new IF_ID();
	public ID_EX ID_EX = new ID_EX();
	public EX_MEM EX_MEM = new EX_MEM();
	
	public EX_MEM_FAdd[] EX_MEM_FAdd = new EX_MEM_FAdd[4];
	public EX_MEM_FMult[] EX_MEM_FMult = new EX_MEM_FMult[6];
	
	public MEM_WB MEM_WB = new MEM_WB();
	public WB_REG WB_REG = new WB_REG();
	
	public State() {
		for (int i = 0; i < 4; i++)
			EX_MEM_FAdd[i] = new EX_MEM_FAdd();
		for (int i = 0; i < 6; i++)
			EX_MEM_FMult[i] = new EX_MEM_FMult();
	}
}

class IF_ID {
	public int add = -1;
	public int NPC = 0, IR = 0;
}

class ID_EX {
	public int add = -1;
	public int NPC = 0, IR = 0;
	public long A = 0, B = 0, Imm = 0;
}

class EX_MEM {
	public int add = -1;
	public int IR = 0;
	public long ALUOutput = 0, B = 0;
	public boolean COND = false;
}

class EX_MEM_FAdd {
	public int add = -1;
	public int IR = 0;
	public long ALUOutput = 0;
}

class EX_MEM_FMult {
	public int add = -1;
	public int IR = 0;
	public long ALUOutput = 0;
}

class MEM_WB {
	public int add = -1;
	public int IR = 0;
	public long ALUOutput = 0, LMD = 0;
	
	// this is cheating
	public boolean COND = false;
}

class WB_REG {
	public int add = -1;
	public int IR = 0;
}