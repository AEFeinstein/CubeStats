package com.gelakinetic.CubeStats;

public class WhereBuilder {

	StringBuilder builder = new StringBuilder();

	public WhereBuilder cmcEquals(int i) {
		builder.append("(cmc = " + i + ") ");
		return this;
	}

	public WhereBuilder and() {
		builder.append("AND ");
		return this;
	}

	public WhereBuilder typeLike(String string) {
		builder.append("(supertype LIKE '%" + string + "%') ");
		return this;
	}

	public WhereBuilder colorEquals(String string) {
		builder.append("(color = '" + string + "') ");
		return this;
	}

	@Override
	public String toString() {
		return builder.toString();
	}

}
