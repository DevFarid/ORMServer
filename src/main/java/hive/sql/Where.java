package hive.sql;

public class Where {

    private String column;
    private ComparisonOp op;
    private String value;

    public Where() {}

    public Where column(String column) {
        this.column = column;
        return this;
    }

    public Where op(ComparisonOp op) {
        this.op = op;
        return this;
    }

    public Where value(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", column, op.getSymbol(), value);
    }

}
