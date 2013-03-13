package org.bcelutil;

import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.bcel.generic.ClassGen;

/**
 * User: whipsch
 * Date: 12/18/12 3:59 PM PST
 */
public abstract class MemberReference<T extends FieldOrMethod> {
	public ClassGen owner;
	public String name;
	public T member;

	protected MemberReference(ClassGen owner, String name, T member) {
		this.owner = owner;
		this.name = name;
		this.member = member;
	}

	public boolean equals(Object obj) {
		if (obj instanceof MemberReference) {
			MemberReference o = (MemberReference) obj;

			return o.name.equals(name) &&
					o.owner.getClassName().equals(owner.getClassName()) &&
					o.member.getName().equals(member.getName()) &&
					o.member.getSignature().equals(member.getSignature());
		}

		return false;
	}
}

