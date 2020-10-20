/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.reconcile;

/**
 * An implementation of {@link ReconcileProblem} that is just a simple data object.
 *
 * @author Kris De Volder
 */
public class ReconcileProblemImpl implements ReconcileProblem {

	final private ProblemType type;
	final private String msg;
	final private int offset;
	final private int len;

	public ReconcileProblemImpl(ProblemType type, String msg, int offset, int len) {
		super();
		this.type = type;
		this.msg = msg;
		this.offset = offset;
		this.len = len;
	}

	@Override
	public ProblemType getType() {
		return type;
	}

	@Override
	public String getMessage() {
		return msg;
	}

	@Override
	public int getOffset() {
		return offset;
	}

	@Override
	public int getLength() {
		return len;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + len;
		result = prime * result + ((msg == null) ? 0 : msg.hashCode());
		result = prime * result + offset;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReconcileProblemImpl other = (ReconcileProblemImpl) obj;
		if (len != other.len)
			return false;
		if (msg == null) {
			if (other.msg != null)
				return false;
		} else if (!msg.equals(other.msg))
			return false;
		if (offset != other.offset)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
