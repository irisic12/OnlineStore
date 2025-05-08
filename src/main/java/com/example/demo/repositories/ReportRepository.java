package com.example.demo.repositories;

import com.example.demo.entities.Order;

import java.util.Date;
import java.util.List;

public interface ReportRepository {

    // Отчет 1: Продажи по товарам за период
    List<Object[]> findSalesByProductForPeriod(Date startDate, Date endDate);

    // Отчет 2: Продажи по категориям товаров за период
    List<Object[]> findSalesByCategoryForPeriod(Date startDate, Date endDate);

    // Отчет 3: Продажи по клиентам за период
    List<Object[]> findSalesByCustomerForPeriod(Date startDate, Date endDate);

    // Отчет 4: Список самых продаваемых товаров (Топ-N) за период
    List<Object[]> findTopNBestSellingProductsForPeriod(Date startDate, Date endDate, int topN);

    // Отчет 5: Динамика продаж по месяцам
    List<Object[]> getSalesDynamicsByMonth(Date startDate, Date endDate);

    // Отчет 6: Динамика продаж по годам
    List<Object[]> getSalesDynamicsByYear(Date startDate, Date endDate);

    // Отчет 7: Заказы в статусе "Ожидание оплаты" более X дней
    List<Order> findPendingPaymentOrdersOlderThan(Date dateThreshold);
}
