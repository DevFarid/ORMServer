package hive.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to attach multiple where clauses onto one big where clause.
 * Created by SixEyes on 06/07/2024.
 */
public class WhereAttacher {
    private final List<Where> whereClauses = new ArrayList<>();
    private final List<ComparisonOp> clauseComparisons = new ArrayList<>();

    /**
     * Adds a where clause to the list of where clauses.
     * @param where The clause to add to the compound.
     * @param trailingComparison The comparison operator to append to the clause.
     * @return The new modified WhereAttacher object.
     */
    public WhereAttacher add(Where where, ComparisonOp trailingComparison) {
        this.whereClauses.add(where);
        this.clauseComparisons.add(trailingComparison);
        return this;
    }

    /**
     * Checks if there are any conditions in the where clause.
     * @return True if there are conditions, false otherwise.
     */
    public boolean hasConditions() {
        return !this.whereClauses.isEmpty();
    }

    /**
     * Generates the where clause(s) string.
     * @return The where clause(s) string.
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < this.whereClauses.size(); i++) {
            stringBuilder.append(this.whereClauses.get(i));
            if (i < this.whereClauses.size() - 1) {
                stringBuilder.append(" ").append(this.clauseComparisons.get(i)).append(" ");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Creates a new WhereAttacher object.
     * @return The new WhereAttacher object.
     */
    public static WhereAttacher builder() {
        return new WhereAttacher();
    }
}
