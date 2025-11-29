package com.krd.starter.payment.models;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interface defining the contract for order information needed for payment processing.
 * <p>
 * Applications should implement this interface on their domain Order entity
 * to provide the payment gateway with necessary checkout information.
 * <p>
 * Example:
 * <pre>
 * {@code
 * @Entity
 * public class Order implements OrderInfo {
 *     // ... entity fields ...
 *
 *     @Override
 *     public Long getOrderId() { return this.id; }
 *
 *     @Override
 *     public List<LineItem> getLineItems() {
 *         return orderItems.stream()
 *             .map(item -> new LineItem(
 *                 item.getProduct().getName(),
 *                 item.getUnitPrice(),
 *                 item.getQuantity()
 *             ))
 *             .collect(Collectors.toList());
 *     }
 * }
 * }
 * </pre>
 */
public interface OrderInfo {

    /**
     * Gets the unique identifier for this order.
     *
     * @return the order ID
     */
    Long getOrderId();

    /**
     * Gets the list of line items in this order.
     * Each line item represents a product/service with its price and quantity.
     *
     * @return list of line items
     */
    List<LineItem> getLineItems();

    /**
     * Represents a single line item in an order.
     */
    class LineItem {
        private final String name;
        private final BigDecimal unitPrice;
        private final int quantity;

        public LineItem(String name, BigDecimal unitPrice, int quantity) {
            this.name = name;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
        }

        public String getName() {
            return name;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}
