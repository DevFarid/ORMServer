package hive.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to attach multiple where clauses onto one big where clause.
 * Created by SixEyes on 06/07/2024.
 */
public class WhereAttacher {

    private List<Where> whereClauses = new ArrayList<>();
    private List<ComparisonOp> clauseComparisons = new ArrayList<>();

    public WhereAttacher() {}

    public WhereAttacher add(Where where, ComparisonOp comparison) {
        whereClauses.add(where);
        clauseComparisons.add(comparison);
        return this;
    }

    public static WhereAttacher builder() {
        return new WhereAttacher();
    }

    public boolean hasConditions() {
        return !whereClauses.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder().append("(");
        for (int i = 0; i < whereClauses.size(); i++) {
            stringBuilder.append(whereClauses.get(i));
            if (i < whereClauses.size() - 1) {
                stringBuilder.append(" ").append(clauseComparisons.get(i)).append(" ");
            }
        }
        return stringBuilder.append(")").toString();
    }
}
