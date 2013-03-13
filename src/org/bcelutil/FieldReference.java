package org.bcelutil;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.Utility;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.Type;

import java.util.HashMap;
import java.util.Map;

/**
 * User: whipsch
 * Date: 12/18/12 3:55 PM PST
 */
public final class FieldReference extends MemberReference<Field> {
	public Type declaredType;

	private FieldReference(ClassGen owner, String name, Field field, Type declaredType) {
		super(owner, name, field);

		this.declaredType = declaredType;
	}

	public boolean equals(Object obj) {
		if (obj instanceof FieldReference && super.equals(obj)) {
			FieldReference id = (FieldReference) obj;

			return id.declaredType.equals(declaredType);
		}

		return false;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(Utility.accessToString(member.getAccessFlags())).append(' ');

		sb.append(member.getType());
		if (!member.getType().equals(declaredType))
			sb.append("->").append(declaredType);

		return sb.append(' ')
				.append(owner.getClassName()).append('.').append(member.getName()).append("->")
				.append(name).toString();
	}

	public int hashCode() {
		return makeHashCode(owner, name, member, declaredType);
	}

	private static final Map<Integer, FieldReference> cache = new HashMap<Integer, FieldReference>();

	private static int makeHashCode(ClassGen owner, String name, Field field, Type declaredType) {
		StringBuilder sb = new StringBuilder(owner.getClassName());
		sb.append(name).append(field.getSignature()).append(field.getName()).append(declaredType);

		return sb.toString().hashCode();
	}

	public static FieldReference get(ClassGen owner, String name, Field field) {
		return get(owner, name, field, field.getType());
	}

	public static FieldReference get(ClassGen owner, String name, Field field, Type declaredType) {
		int hash = makeHashCode(owner, name, field, declaredType);
		if (cache.containsKey(hash)) {
			return cache.get(hash);
		}

		FieldReference id = new FieldReference(owner, name, field, declaredType);
		cache.put(hash, id);

		return id;
	}
}
