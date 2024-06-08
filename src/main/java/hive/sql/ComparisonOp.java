package hive.sql;

public enum ComparisonOp {

    EQUALS("="),
    NOT_EQUALS("!="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUALS(">="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUALS("<="),
    LIKE("LIKE"),
    NOT_LIKE("NOT LIKE"),
    IN("IN"),
    NOT_IN("NOT IN"),
    IS_NULL("IS NULL"),
    IS_NOT_NULL("IS NOT NULL"),
    BETWEEN("BETWEEN"),
    NOT_BETWEEN("NOT BETWEEN"),
    AND("AND"),
    OR("OR"),
    NOT("NOT"),
    EXISTS("EXISTS"),
    NOT_EXISTS("NOT EXISTS"),
    ALL("ALL"),
    ANY("ANY"),
    SOME("SOME"),
    UNKNOWN("UNKNOWN");

    private final String symbol;

    ComparisonOp(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public static ComparisonOp getFromSymbol(String symbol) {
        for (ComparisonOp op : ComparisonOp.values()) {
            if (op.getSymbol().equals(symbol)) {
                return op;
            }
        }
        return null;
    }
}
