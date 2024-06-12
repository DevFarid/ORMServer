package hive.sql.elements;

import java.util.ArrayList;
import java.util.List;

public class OrderBuilder {

    private final List<Order> orderList = new ArrayList<>();

    public static OrderBuilder builder() {
        return new OrderBuilder();
    }

    public void add(Order order) {
        this.orderList.add(order);
    }

    public boolean hasOrders() {
        return !this.orderList.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ORDER BY ");
        for (int i = 0; i < this.orderList.size(); i++) {
            stringBuilder.append(this.orderList.get(i));
            if (i < this.orderList.size() - 1) {
                stringBuilder.append(", ");
            }
        }
        return stringBuilder.toString();
    }
}
