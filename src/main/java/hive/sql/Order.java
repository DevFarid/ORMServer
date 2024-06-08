package hive.sql;

public class Order {
    private String column;
    private boolean ascending;

    public Order column(String column) {
        this.column = column;
        return this;
    }

    public Order ascending() {
        this.ascending = true;
        return this;
    }

    public Order descending() {
        this.ascending = false;
        return this;
    }

    @Override
    public String toString() {
        return String.format("%s %s", column, ascending ? "ASC" : "DESC");
    }

    public static Order builder(String column, boolean ascending) {
        return (ascending) ? new Order().column(column).ascending() : new Order().column(column).descending();
    }
}
