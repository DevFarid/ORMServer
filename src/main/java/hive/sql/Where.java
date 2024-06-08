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

    public Where value(String... values) {
        if(values.length > 0) {
            StringBuilder sb = new StringBuilder("(");
            for (int i = 0; i < values.length; i++) {
                sb.append("'").append(values[i]).append("'");
                if (i < values.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append(")");
            this.value = sb.toString();
        }
        return this;
    }

    @Override
    public String toString() {
        if(value != null)
            return String.format("%s %s %s", column, op.getSymbol(), value);
        else return String.format("%s %s", column, op.getSymbol());
    }

}
