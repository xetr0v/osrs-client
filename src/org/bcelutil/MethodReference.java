package org.bcelutil;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Utility;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.Type;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * User: whipsch
 * Date: 12/18/12 3:57 PM PST
 */
public class MethodReference extends MemberReference<Method> {
	public Type declaredReturnType;
	public Type[] declaredArgumentTypes;

	private MethodReference(ClassGen owner, String name, Method method, Type declaredReturnType, Type[] declaredArgumentTypes) {
		super(owner, name, method);
		this.declaredReturnType = declaredReturnType;
		this.declaredArgumentTypes = declaredArgumentTypes;
	}

	public boolean equals(Object obj) {
		if (obj instanceof MethodReference && super.equals(obj)) {
			MethodReference id = (MethodReference) obj;

			return id.declaredReturnType.equals(declaredReturnType) &&
					Arrays.equals(id.declaredArgumentTypes, declaredArgumentTypes);
		}

		return false;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(Utility.accessToString(member.getAccessFlags()));

		sb.append(' ').append(member.getReturnType()).append("->").append(declaredReturnType)
				.append(' ').append(owner.getClassName()).append('.').append(member.getName())
				.append('(');

		Type[] argumentTypes = member.getArgumentTypes();
		for (int i = 0; i < argumentTypes.length; i++) {
			sb.append(argumentTypes[i]);
			if (!argumentTypes[i].equals(declaredArgumentTypes[i])) {
				sb.append("->").append(declaredArgumentTypes[i]);
			}

			if (i != argumentTypes.length - 1)
				sb.append(", ");
		}

		return sb.append(')').toString();
	}


	public int hashCode() {
		return makeHashCode(owner, name, member, declaredReturnType, declaredArgumentTypes);
	}

	private static final Map<Integer, MethodReference> cache = new HashMap<Integer, MethodReference>();

	private static int makeHashCode(ClassGen owner, String name, Method method, Type declaredReturnType, Type[] declaredArgumentTypes) {
		StringBuilder sb = new StringBuilder(owner.getClassName());
		sb.append(name).append(method.getSignature()).append(method.getName()).append(declaredReturnType);
		sb.append(Arrays.toString(declaredArgumentTypes));

		return sb.toString().hashCode();
	}

	public static MethodReference get(ClassGen owner, String name, Method method) {
		return get(owner, name, method, method.getReturnType());
	}

	public static MethodReference get(ClassGen owner, String name, Method method, Type declaredReturnType) {
		return get(owner, name, method, declaredReturnType, method.getArgumentTypes());
	}

	public static MethodReference get(ClassGen owner, String name, Method method, Type declaredReturnType, Type[] declaredArgumentTypes) {
		int hash = makeHashCode(owner, name, method, declaredReturnType, declaredArgumentTypes);
		if (cache.containsKey(hash)) {
			return cache.get(hash);
		}

		MethodReference id = new MethodReference(owner, name, method, declaredReturnType, declaredArgumentTypes);
		cache.put(hash, id);

		return id;
	}
}
