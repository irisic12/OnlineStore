package com.example.demo.helpClass;

import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentMethod;

import java.util.Map;

public class DisplayNames {
    private static final Map<OrderStatus, String> orderStatusNames = Map.of(
            OrderStatus.CREATED, "Создан",
            OrderStatus.PAID, "Оплачен",
            OrderStatus.IN_PROCESSING, "В обработке",
            OrderStatus.SENT, "Отправлен",
            OrderStatus.DELIVERED, "Доставлен",
            OrderStatus.CANCELLED, "Отменен"
    );

    private static final Map<PaymentMethod, String> paymentMethodNames = Map.of(
            PaymentMethod.CREDIT_CARD, "Банковская карта",
            PaymentMethod.CASH, "Наличные"
    );

    public static String getDisplayName(OrderStatus status) {
        return orderStatusNames.get(status);
    }

    public static String getDisplayName(PaymentMethod method) {
        return paymentMethodNames.get(method);
    }
}
