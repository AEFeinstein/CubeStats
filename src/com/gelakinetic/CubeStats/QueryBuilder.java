package com.gelakinetic.CubeStats;

public class QueryBuilder {
	
	StringBuilder queryBuilder = new StringBuilder();

	public QueryBuilder selectAll() {
		queryBuilder.append("SELECT * ");
		return this;
	}

	public QueryBuilder fromAll() {
		queryBuilder.append("FROM cards JOIN sets ON cards.expansion = sets.code ");
		return this;
	}

	public QueryBuilder where(WhereBuilder statement) {
		queryBuilder.append("WHERE (").append(statement.toString()).append(") ");
		return this;
	}

	public String build() {
		return queryBuilder.toString();
	}

}
