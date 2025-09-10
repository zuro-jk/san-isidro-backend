package com.sanisidro.restaurante.features.orders.service;

import com.sanisidro.restaurante.features.orders.model.Order;
import com.sanisidro.restaurante.features.orders.model.OrderDetail;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class OrderEmailBuilder {

    public static String buildOrderCreatedEmail(Order order) {
        // Escapar posibles %
        String customerName = escapePercent(order.getCustomer().getUser().getFullName());
        BigDecimal total = order.getTotal();
        String formattedTotal = escapePercent(NumberFormat.getCurrencyInstance(new Locale("es", "PE")).format(total));

        // Construir filas de la tabla de productos
        StringBuilder productsRows = new StringBuilder();
        for (OrderDetail d : order.getDetails()) {
            BigDecimal subtotal = d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity()));
            String formattedSubtotal = escapePercent(NumberFormat.getCurrencyInstance(new Locale("es", "PE")).format(subtotal));
            String formattedUnitPrice = escapePercent(NumberFormat.getCurrencyInstance(new Locale("es", "PE")).format(d.getUnitPrice()));

            productsRows.append("<tr style=\"border-bottom: 1px solid #e5e7eb;\">")
                    .append("<td style=\"padding: 8px 4px;\">").append(escapePercent(d.getProduct().getName())).append("</td>")
                    .append("<td style=\"padding: 8px 4px; text-align:center;\">").append(d.getQuantity()).append("</td>")
                    .append("<td style=\"padding: 8px 4px; text-align:right;\">").append(formattedUnitPrice).append("</td>")
                    .append("<td style=\"padding: 8px 4px; text-align:right;\">").append(formattedSubtotal).append("</td>")
                    .append("</tr>");
        }

        // Construir HTML completo
        StringBuilder html = new StringBuilder();
        html.append("<html>")
                .append("<body style=\"font-family: sans-serif; background-color: #f9fafb; padding: 20px;\">")
                .append("<div style=\"max-width: 600px; margin:auto; background-color: #ffffff; border-radius: 8px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);\">")
                .append("<h2 style=\"color: #111827; font-size: 24px; font-weight: bold; margin-bottom: 10px;\">")
                .append("Hola ").append(customerName).append(", tu orden #").append(order.getId()).append(" fue registrada ✅")
                .append("</h2>")
                .append("<p style=\"color: #374151; font-size: 16px; margin-bottom: 20px;\">")
                .append("Total: <strong>").append(formattedTotal).append("</strong>")
                .append("</p>")
                .append("<h3 style=\"color: #111827; font-size: 18px; font-weight: bold; margin-bottom: 10px;\">Productos:</h3>")
                .append("<table style=\"width:100%; border-collapse: collapse; margin-bottom: 20px;\">")
                .append("<thead>")
                .append("<tr style=\"background-color:#f3f4f6;\">")
                .append("<th style=\"padding:8px 4px; text-align:left;\">Producto</th>")
                .append("<th style=\"padding:8px 4px; text-align:center;\">Cantidad</th>")
                .append("<th style=\"padding:8px 4px; text-align:right;\">Precio Unitario</th>")
                .append("<th style=\"padding:8px 4px; text-align:right;\">Subtotal</th>")
                .append("</tr>")
                .append("</thead>")
                .append("<tbody>")
                .append(productsRows)
                .append("</tbody>")
                .append("</table>")
                .append("<p style=\"color: #6b7280; font-size: 14px;\">")
                .append("Gracias por comprar con nosotros. Esperamos verte pronto de nuevo!")
                .append("</p>")
                .append("</div>")
                .append("</body>")
                .append("</html>");

        return html.toString();
    }

    private static String escapePercent(String input) {
        return input == null ? "" : input.replace("%", "%%");
    }
}
