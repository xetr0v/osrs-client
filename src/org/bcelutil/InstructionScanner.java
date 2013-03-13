package org.bcelutil;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.*;
import org.apache.bcel.util.InstructionFinder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Johan
 */
public class InstructionScanner {
	private int index;

	private InstructionList instrList;
	private ConstantPoolGen cpg;
	private InstructionFinder finder;

	public InstructionScanner(ConstantPoolGen cpg, InstructionList instrList) {
		this.instrList = instrList;
		this.cpg = cpg;
		
		finder = new InstructionFinder(instrList);
		index = 0;
	}

	public void first() {
		index = 0;
	}

	public void last() {
		Instruction[] inst = instrList.getInstructions();

		if (inst.length > 0) {
			this.index = instrList.getInstructions().length - 1;
		}
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		Instruction[] instructions = instrList.getInstructions();

		if (index < 0) {
			index = 0;
		} else if (index >= instructions.length) {
			index = instructions.length - 1;
		}

		this.index = index;
	}

	public Instruction instruction() {
		return instrList.getInstructions()[index];
	}

	public InstructionHandle handle() {
		return instrList.getInstructionHandles()[index];
	}
	
	public Instruction getNext() {
		Instruction[] inst = instrList.getInstructions();		
		return index < inst.length ? inst[index++] : null;
	}

	public Instruction getNext(short... opcodes) {
		Instruction[] inst = instrList.getInstructions();

		if (index >= inst.length)
			return null;

		for (int i = index; i < inst.length; i++) {
			for (short opcode : opcodes) {
				if (inst[i].getOpcode() == opcode) {
					index = i + 1;
					return inst[i];
				}
			}
		}

		return null;
	}

	public <T> T getNext(Class<T> type) {
		Instruction[] inst = instrList.getInstructions();

		if (index >= inst.length)
			return null;

		for (int i = index; i < inst.length; i++) {
			if (type.isAssignableFrom(inst[i].getClass())) {
				index = i + 1;
				return (T) inst[i];
			}
		}

		return null;
	}
	
	public boolean seekNext() {
		return getNext() != null;
	}

	public boolean seekNext(short... opcodes) {
		return getNext(opcodes) != null;
	}

	public boolean seekNext(Class<?>... types) {
		Instruction[] inst = instrList.getInstructions();

		if (index >= inst.length)
			return false;

		for (int i = index; i < inst.length; i++) {
			for (Class<?> type : types) {
				if (type.isAssignableFrom(inst[i].getClass())) {
					index = i + 1;
					return true;
				}
			}
		}

		return false;
	}
	
	public Instruction getPrevious() {
		Instruction[] inst = instrList.getInstructions();		
		return index >= 0 ? inst[index--] : null;
	}

	public Instruction getPrevious(short... opcodes) {
		Instruction[] inst = instrList.getInstructions();

		for (int i = index; i >= 0; i--) {
			if (i >= inst.length)
				i = inst.length - 1;

			for (short opcode : opcodes) {
				if (inst[i].getOpcode() == opcode) {
					index = i - 1;
					return inst[i];
				}
			}
		}

		return null;
	}

	public <T> T getPrevious(Class<T> type) {
		Instruction[] inst = instrList.getInstructions();

		for (int i = index; i >= 0; i--) {
			if (i >= inst.length)
				i = inst.length - 1;

			if (type.isAssignableFrom(inst[i].getClass())) {
				index = i - 1;
				return (T) inst[i];
			}
		}

		return null;
	}
	
	public boolean seekPrevious() {
		return getPrevious() != null;
	}

	public boolean seekPrevious(short... opcodes) {
		return getPrevious(opcodes) != null;
	}

	public boolean seekPrevious(Class<?>... types) {
		Instruction[] inst = instrList.getInstructions();

		for (int i = index; i >= 0; i--) {
			if (i >= inst.length)
				i = inst.length - 1;

			for (Class<?> type : types) {
				if (type.isAssignableFrom(inst[i].getClass())) {
					index = i - 1;
					return true;
				}
			}
		}

		return false;
	}

	// TODO: proper pattern finding
	public ArrayList<InstructionHandle[]> find(String pattern) {
		ArrayList<InstructionHandle[]> handles = new ArrayList<InstructionHandle[]>();
		for (Iterator e = finder.search(pattern); e.hasNext(); ) {
			handles.add((InstructionHandle[]) e.next());
		}
		return handles;
	}

	public InstructionHandle[][] find(String... regexPatt) {
		Pattern[] patterns = new Pattern[regexPatt.length];
		for (int i = 0; i < patterns.length; i++) {
			if (regexPatt[i] == null) {
				continue;
			}
			patterns[i] = Pattern.compile(regexPatt[i]);
		}
		return find(patterns);
	}

	public InstructionHandle[][] find(Pattern... patterns) {

		List<InstructionHandle[]> matches = new LinkedList<InstructionHandle[]>();
		InstructionHandle[] instructionHandles = instrList.getInstructionHandles();
		int offset = 0;
		for (int i = 0; i < instructionHandles.length; i++) {
			Matcher matcher = patterns[offset].matcher(instructionHandles[i].getInstruction().getName());
			if (!matcher.find()) {
				offset = 0;
				continue;
			}
			if (++offset == patterns.length) {
				InstructionHandle[] match = new InstructionHandle[offset];
				System.arraycopy(instructionHandles, i - offset + 1, match, 0, match.length);
				matches.add(match);
				offset = 0;
			}
		}
		return matches.toArray(new InstructionHandle[matches.size()][]);
	}

	public LocalVariableInstruction getNextVar(int index) {
		int i = this.index;

		LocalVariableInstruction var;
		while ((var = getNext(LocalVariableInstruction.class)) != null) {
			if (var.getIndex() == index)
				return var;
		}

		this.index = i;
		return null;
	}

	public LocalVariableInstruction getNextVar(Type type) {
		int i = this.index;

		if (type instanceof ObjectType)
			type = Type.OBJECT;

		LocalVariableInstruction var;
		while ((var = getNext(LocalVariableInstruction.class)) != null) {
			if (var.getType(cpg).equals(type))
				return var;
		}

		this.index = i;
		return null;
	}

	public boolean seekNextVar(int index) {
		return getNextVar(index) != null;
	}

	public boolean seekNextVar(Type type) {
		return getNextVar(type) != null;
	}

	public LocalVariableInstruction getPreviousVar(int index) {
		int i = this.index;

		LocalVariableInstruction var;
		while ((var = getPrevious(LocalVariableInstruction.class)) != null) {
			if (var.getIndex() == index)
				return var;
		}

		this.index = i;
		return null;
	}

	public LocalVariableInstruction getPreviousVar(Type type) {
		int i = this.index;

		if (type instanceof ObjectType)
			type = Type.OBJECT;

		LocalVariableInstruction var;
		while ((var = getPrevious(LocalVariableInstruction.class)) != null) {
			if (var.getType(cpg).equals(type))
				return var;
		}

		this.index = i;
		return null;
	}

	public boolean seekPreviousVar(int index) {
		return getPreviousVar(index) != null;
	}

	public boolean seekPreviousVar(Type type) {
		return getPreviousVar(type) != null;
	}

	public LoadInstruction getNextLoad(int index) {
		int i = this.index;

		LocalVariableInstruction var;
		while ((var = getNextVar(index)) != null) {
			if (LoadInstruction.class.isAssignableFrom(var.getClass()))
				return (LoadInstruction) var;
		}

		this.index = i;
		return null;
	}

	public LoadInstruction getNextLoad(Type type) {
		int i = this.index;

		LocalVariableInstruction var;
		while ((var = getNextVar(type)) != null) {
			if (LoadInstruction.class.isAssignableFrom(var.getClass()))
				return (LoadInstruction) var;
		}

		this.index = i;
		return null;
	}

	public boolean seekNextLoad(int index) {
		return getNextLoad(index) != null;
	}

	public boolean seekNextLoad(Type type) {
		return getNextLoad(type) != null;
	}

	public LoadInstruction getPreviousLoad(int index) {
		int i = this.index;

		LocalVariableInstruction var;
		while ((var = getPreviousVar(index)) != null) {
			if (LoadInstruction.class.isAssignableFrom(var.getClass()))
				return (LoadInstruction) var;
		}

		this.index = i;
		return null;
	}

	public LoadInstruction getPreviousLoad(Type type) {
		int i = this.index;

		LocalVariableInstruction var;
		while ((var = getPreviousVar(type)) != null) {
			if (LoadInstruction.class.isAssignableFrom(var.getClass()))
				return (LoadInstruction) var;
		}

		this.index = i;
		return null;
	}

	public boolean seekPreviousLoad(int index) {
		return getPreviousLoad(index) != null;
	}

	public boolean seekPreviousLoad(Type type) {
		return getPreviousLoad(type) != null;
	}

	public StoreInstruction getNextStore(int index) {
		int i = this.index;

		LocalVariableInstruction var;
		while ((var = getNextVar(index)) != null) {
			if (StoreInstruction.class.isAssignableFrom(var.getClass()))
				return (StoreInstruction) var;
		}

		this.index = i;
		return null;
	}

	public StoreInstruction getNextStore(Type type) {
		int i = this.index;

		LocalVariableInstruction var;
		while ((var = getNextVar(type)) != null) {
			if (StoreInstruction.class.isAssignableFrom(var.getClass()))
				return (StoreInstruction) var;
		}

		this.index = i;
		return null;
	}

	public boolean seekNextStore(int index) {
		return getNextStore(index) != null;
	}

	public boolean seekNextStore(Type type) {
		return getNextStore(type) != null;
	}

	public StoreInstruction getPreviousStore(int index) {
		int i = this.index;

		LocalVariableInstruction var;
		while ((var = getPreviousVar(index)) != null) {
			if (StoreInstruction.class.isAssignableFrom(var.getClass()))
				return (StoreInstruction) var;
		}

		this.index = i;
		return null;
	}

	public StoreInstruction getPreviousStore(Type type) {
		int i = this.index;

		LocalVariableInstruction var;
		while ((var = getPreviousVar(type)) != null) {
			if (StoreInstruction.class.isAssignableFrom(var.getClass()))
				return (StoreInstruction) var;
		}

		this.index = i;
		return null;
	}

	public boolean seekPreviousStore(int index) {
		return getPreviousStore(index) != null;
	}

	public boolean seekPreviousStore(Type type) {
		return getPreviousStore(type) != null;
	}
	
	public FieldOrMethod getNextMember(String signature) {
		return getNextMember(FieldOrMethod.class, signature);
	}
	
	public <T extends FieldOrMethod> T getNextMember(Class<T> type, String signature) {
		int i = this.index;

		T insn;
		while ((insn = getNext(type)) != null) {
			if (insn.getSignature(cpg).equals(signature))
				return insn;
		}

		this.index = i;
		return null;		
	}
	
	public FieldOrMethod getNextMember(MemberReference<?> ref) {
		return getNextMember(FieldOrMethod.class, ref);
	}
	
	public <T extends FieldOrMethod> T getNextMember(Class<T> type, MemberReference<?> ref) {
		int i = this.index;

		Type owner = getType(ref.owner);
		
		T insn;
		while ((insn = getNext(type)) != null) {
			if (insn.getReferenceType(cpg).equals(owner) &&
					insn.getName(cpg).equals(ref.member.getName()) &&
					insn.getSignature(cpg).equals(ref.member.getSignature()))
				return insn;
		}
		
		this.index = i;
		return null;
	}
	
	public boolean seekNextMember(String signature) {
		return getNextMember(signature) != null;
	}
	
	public <T extends FieldOrMethod> boolean seekNextMember(Class<T> type, String signature) {
		return getNextMember(type, signature) != null;
	}
	
	public boolean seekNextMember(MemberReference<?> ref) {
		return getNextMember(ref) != null;
	}
	
	public <T extends FieldOrMethod> boolean seekNextMember(Class<T> type, MemberReference<?> ref) {
		return getNextMember(type, ref) != null;
	}
	
	public FieldOrMethod getPreviousMember(String signature) {
		return getPreviousMember(FieldOrMethod.class, signature);
	}

	public <T extends FieldOrMethod> T getPreviousMember(Class<T> type, String signature) {
		int i = this.index;

		T insn;
		while ((insn = getPrevious(type)) != null) {
			if (insn.getSignature(cpg).equals(signature))
				return insn;
		}

		this.index = i;
		return null;
	}

	public FieldOrMethod getPreviousMember(MemberReference<?> ref) {
		return getPreviousMember(FieldOrMethod.class, ref);
	}

	public <T extends FieldOrMethod> T getPreviousMember(Class<T> type, MemberReference<?> ref) {
		int i = this.index;

		Type owner = getType(ref.owner);

		T insn;
		while ((insn = getPrevious(type)) != null) {
			if (insn.getReferenceType(cpg).equals(owner) &&
					insn.getName(cpg).equals(ref.member.getName()) &&
					insn.getSignature(cpg).equals(ref.member.getSignature()))
				return insn;
		}

		this.index = i;
		return null;
	}

	public boolean seekPreviousMember(String signature) {
		return getPreviousMember(signature) != null;
	}

	public <T extends FieldOrMethod> boolean seekPreviousMember(Class<T> type, String signature) {
		return getPreviousMember(type, signature) != null;
	}

	public boolean seekPreviousMember(MemberReference<?> ref) {
		return getPreviousMember(ref) != null;
	}

	public <T extends FieldOrMethod> boolean seekPreviousMember(Class<T> type, MemberReference<?> ref) {
		return getPreviousMember(type, ref) != null;
	}	

	public FieldInstruction getNextField(Type type) {
		return getNextMember(FieldInstruction.class, type.getSignature());
	}

	public FieldInstruction getNextField(FieldReference ref) {
		return getNextMember(FieldInstruction.class, ref);
	}

	public boolean seekNextField(Type type) {
		return getNextField(type) != null;
	}

	public boolean seekNextField(FieldReference ref) {
		return getNextField(ref) != null;
	}

	public FieldInstruction getPreviousField(Type type) {
		return getPreviousMember(FieldInstruction.class, type.getSignature());
	}

	public FieldInstruction getPreviousField(FieldReference ref) {
		return getPreviousMember(FieldInstruction.class, ref);
	}

	public boolean seekPreviousField(Type type) {
		return getPreviousField(type) != null;
	}

	public boolean seekPreviousField(FieldReference ref) {
		return getPreviousField(ref) != null;
	}
	
	public InvokeInstruction getNextMethod(String signature) {
		return getNextMember(InvokeInstruction.class, signature);
	}
	
	public InvokeInstruction getNextMethod(Type ret, Type... args) {
		return getNextMethod(Type.getMethodSignature(ret, args));
	}
	
	public InvokeInstruction getNextMethod(MethodReference ref) {
		return getNextMember(InvokeInstruction.class, ref);
	}
	
	public boolean seekNextMethod(String signature) {
		return getNextMethod(signature) != null;
	}
	
	public boolean seekNextMethod(Type ret, Type... args) {
		return getNextMethod(ret, args) != null;
	}
	
	public boolean seekNextMethod(MethodReference ref) {
		return getNextMethod(ref) != null;
	}

	public InvokeInstruction getPreviousMethod(String signature) {
		return getPreviousMember(InvokeInstruction.class, signature);
	}

	public InvokeInstruction getPreviousMethod(Type ret, Type... args) {
		return getPreviousMethod(Type.getMethodSignature(ret, args));
	}

	public InvokeInstruction getPreviousMethod(MethodReference ref) {
		return getPreviousMember(InvokeInstruction.class, ref);
	}

	public boolean seekPreviousMethod(String signature) {
		return getPreviousMethod(signature) != null;
	}

	public boolean seekPreviousMethod(Type ret, Type... args) {
		return getPreviousMethod(ret, args) != null;
	}

	public boolean seekPreviousMethod(MethodReference ref) {
		return getPreviousMethod(ref) != null;
	}

	public ConstantPushInstruction getNextConstantPush(Number val) {
		int i = this.index;

		ConstantPushInstruction insn;
		while ((insn = getNext(ConstantPushInstruction.class)) != null) {
			if (insn.getValue().equals(val))
				return insn;
		}

		this.index = i;
		return null;
	}

	public <T extends ConstantPushInstruction> T getNextConstantPush(Class<T> type, Number val) {
		int i = this.index;

		T insn;
		while ((insn = getNext(type)) != null) {
			if (insn.getValue().equals(val))
				return insn;
		}

		this.index = i;
		return null;
	}

	public boolean seekNextConstantPush(Number val) {
		return getNextConstantPush(val) != null;
	}

	public <T extends ConstantPushInstruction> boolean seekNextConstantPush(Class<T> type, Number val) {
		return getNextConstantPush(type, val) != null;
	}

	public ConstantPushInstruction getPreviousConstantPush(Number val) {
		int i = this.index;

		ConstantPushInstruction insn;
		while ((insn = getPrevious(ConstantPushInstruction.class)) != null) {
			if (insn.getValue().equals(val))
				return insn;
		}

		this.index = i;
		return null;
	}

	public <T extends ConstantPushInstruction> T getPreviousConstantPush(Class<T> type, Number val) {
		int i = this.index;

		T insn;
		while ((insn = getPrevious(type)) != null) {
			if (insn.getValue().equals(val))
				return insn;
		}

		this.index = i;
		return null;
	}

	public boolean seekPreviousConstantPush(Number val) {
		return getPreviousConstantPush(val) != null;
	}

	public <T extends ConstantPushInstruction> boolean seekPreviousConstantPush(Class<T> type, Number val) {
		return getPreviousConstantPush(type, val) != null;
	}

	public LDC getNextLdc(String val) {
		int i = this.index;

		LDC ldc;
		while ((ldc = getNext(LDC.class)) != null) {
			if (ldc.getValue(cpg).toString().equals(val))
				return ldc;
		}

		this.index = i;
		return null;
	}

	public LDC2_W getNextLdc2w(String val) {
		int i = this.index;

		LDC2_W ldc;
		while ((ldc = getNext(LDC2_W.class)) != null) {
			if (ldc.getValue(cpg).toString().equals(val))
				return ldc;
		}

		this.index = i;
		return null;
	}

	public LDC2_W getNextLdc2w(Number val) {
		return getNextLdc2w(val.toString());
	}

	public boolean seekNextLdc(String val) {
		int i = this.index;

		Instruction ldc;
		while ((ldc = getNext(Constants.LDC, Constants.LDC_W, Constants.LDC2_W)) != null) {
			Object value = ldc.getOpcode() == Constants.LDC2_W ? ((LDC2_W) ldc).getValue(cpg) : ((LDC) ldc).getValue(cpg);

			if (value.toString().equals(val))
				return true;
		}

		this.index = i;
		return false;
	}

	public boolean seekNextLdc2w(String val) {
		return getNextLdc2w(val) != null;
	}

	public boolean seekNextLdc2w(Number val) {
		return getNextLdc2w(val) != null;
	}

	public LDC getPreviousLdc(String val) {
		int i = this.index;

		LDC ldc;
		while ((ldc = getPrevious(LDC.class)) != null) {
			if (ldc.getValue(cpg).toString().equals(val))
				return ldc;
		}

		this.index = i;
		return null;
	}

	public LDC2_W getPreviousLdc2w(String val) {
		int i = this.index;

		LDC2_W ldc;
		while ((ldc = getPrevious(LDC2_W.class)) != null) {
			if (ldc.getValue(cpg).toString().equals(val))
				return ldc;
		}

		this.index = i;
		return null;
	}

	public LDC2_W getPreviousLdc2w(Number val) {
		return getPreviousLdc2w(val.toString());
	}

	public boolean seekPreviousLdc(String val) {
		int i = this.index;

		Instruction ldc;
		while ((ldc = getPrevious(Constants.LDC, Constants.LDC_W, Constants.LDC2_W)) != null) {
			Object value = ldc.getOpcode() == Constants.LDC2_W ? ((LDC2_W) ldc).getValue(cpg) : ((LDC) ldc).getValue(cpg);

			if (value.toString().equals(val))
				return true;
		}

		this.index = i;
		return false;
	}

	public boolean seekPreviousLdc2w(String val) {
		return getPreviousLdc2w(val) != null;
	}

	public boolean seekPreviousLdc2w(Number val) {
		return getPreviousLdc2w(val) != null;
	}

	public ReturnInstruction getNextReturn(Type... types) {
		int i = this.index;

		ReturnInstruction insn;
		while ((insn = getNext(ReturnInstruction.class)) != null) {
			for (Type type : types) {
				if (insn.getType().equals(type))
					return insn;
			}
		}

		this.index = i;
		return null;
	}

	public boolean seekNextReturn(Type... types) {
		return getNextReturn(types) != null;
	}

	public ReturnInstruction getPreviousReturn(Type... types) {
		int i = this.index;

		ReturnInstruction insn;
		while ((insn = getPrevious(ReturnInstruction.class)) != null) {
			for (Type type : types) {
				if (insn.getType().equals(type))
					return insn;
			}
		}

		this.index = i;
		return null;
	}

	public boolean seekPreviousReturn(Type... types) {
		return getPreviousReturn(types) != null;
	}

    public int count(short... opcodes) {
        Instruction[] inst = instrList.getInstructions();
        int count = 0;

        for (int i = 0; i < inst.length; i++) {
            for (short opcode : opcodes) {
                if (inst[i].getOpcode() == opcode) {
                    count++;
                }
            }
        }
        return count;
    }

    public static Type getType(String name) {
        StringBuilder sig = new StringBuilder("L");

        for (char c : name.toCharArray()) {
            switch (c) {
                case '[':
                    sig.insert(0, c);
                case ']':
                    break;

                case '.':
                    sig.append('/');
                    break;

                default:
                    sig.append(c);
            }
        }

        return Type.getType(sig.append(';').toString());
    }

    public static Type getType(ClassGen cg) {
        return getType(cg.getClassName());
    }

}