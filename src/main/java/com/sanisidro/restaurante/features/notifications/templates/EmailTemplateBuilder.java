package com.sanisidro.restaurante.features.notifications.templates;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class EmailTemplateBuilder {

    public static String buildOrderConfirmationEmail(
            String customerName,
            Long orderId,
            List<OrderProduct> products,
            BigDecimal total,
            LocalDateTime orderDate,
            String orderUrl,
            String logoUrl
    ) {
        String formattedTotal = NumberFormat.getCurrencyInstance(new Locale("es", "PE")).format(total);
        String formattedDate = orderDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        StringBuilder productsRows = new StringBuilder();
        boolean alternate = false;
        for (OrderProduct p : products) {
            BigDecimal subtotal = p.getUnitPrice().multiply(BigDecimal.valueOf(p.getQuantity()));
            String formattedUnitPrice = NumberFormat.getCurrencyInstance(new Locale("es", "PE")).format(p.getUnitPrice());
            String formattedSubtotal = NumberFormat.getCurrencyInstance(new Locale("es", "PE")).format(subtotal);

            String bgColor = alternate ? "#f9fafb" : "#ffffff";
            alternate = !alternate;

            productsRows.append("<tr style=\"background-color:").append(bgColor).append("; border-bottom:1px solid #e5e7eb;\">")
                    .append("<td style=\"padding:8px 4px;\">").append(escapeHtml(p.getName())).append("</td>")
                    .append("<td style=\"padding:8px 4px; text-align:center;\">").append(p.getQuantity()).append("</td>")
                    .append("<td style=\"padding:8px 4px; text-align:right;\">").append(formattedUnitPrice).append("</td>")
                    .append("<td style=\"padding:8px 4px; text-align:right; font-weight:bold;\">").append(formattedSubtotal).append("</td>")
                    .append("</tr>");
        }

        String logoHtml = (logoUrl != null && !logoUrl.isBlank())
                ? "<div style=\"text-align:center; margin-bottom:15px;\"><img src=\"" + logoUrl + "\" alt=\"Logo\" style=\"max-height:60px;\"></div>"
                : "";

        return """
                <html>
                  <body style="font-family:'Segoe UI', sans-serif; background-color:#f4f4f6; padding:20px;">
                    <div style="max-width:650px; margin:auto; background-color:#ffffff; border-radius:10px; overflow:hidden; box-shadow:0 4px 15px rgba(0,0,0,0.1);">
                      
                      %s

                      <div style="background-color:#16a34a; padding:30px; text-align:center; color:white;">
                        <h1 style="margin:0; font-size:28px;">¡Wow, %s! 🎉</h1>
                        <p style="margin:5px 0 0; font-size:16px;">Tu orden #%d ha sido registrada ✅</p>
                      </div>

                      <div style="padding:25px;">
                        <p style="font-size:14px; color:#374151;">Fecha de la orden: <strong>%s</strong></p>
                        <h2 style="font-size:18px; color:#111827; margin-bottom:10px;">Productos en tu orden:</h2>

                        <table style="width:100%%; border-collapse: collapse; margin-bottom:20px;">
                          <thead>
                            <tr style="background-color:#f3f4f6;">
                              <th style="padding:8px 4px; text-align:left;">Producto</th>
                              <th style="padding:8px 4px; text-align:center;">Cantidad</th>
                              <th style="padding:8px 4px; text-align:right;">Precio Unitario</th>
                              <th style="padding:8px 4px; text-align:right;">Subtotal</th>
                            </tr>
                          </thead>
                          <tbody>
                            %s
                          </tbody>
                        </table>

                        <p style="font-size:20px; font-weight:bold; text-align:right; color:#16a34a; margin-top:10px; margin-bottom:30px;">
                          Total a pagar: %s
                        </p>

                        <div style="text-align:center; margin-bottom:30px;">
                          <a href="%s" style="background-color:#16a34a; color:white; padding:12px 25px; border-radius:5px; text-decoration:none; font-weight:bold; font-size:16px;">Ver mi orden</a>
                        </div>

                        <p style="font-size:14px; color:#6b7280; margin-bottom:5px;">¡Gracias por confiar en nosotros! Esperamos que disfrutes tu pedido y verte pronto de nuevo.</p>
                        <p style="font-size:12px; color:#9ca3af; margin-top:0;">Si tienes alguna duda o consulta, contáctanos respondiendo este correo.</p>
                      </div>
                    </div>
                  </body>
                </html>
                """.formatted(logoHtml, escapeHtml(customerName), orderId, formattedDate, productsRows, formattedTotal, orderUrl);
    }

    public static String buildPromotionEmail(String title, String message, String buttonUrl) {
        return """
            <html>
              <body style="font-family:'Segoe UI', sans-serif; background-color:#f4f4f6; padding:20px;">
                <div style="max-width:650px; margin:auto; background-color:#ffffff; border-radius:10px; overflow:hidden; box-shadow:0 4px 15px rgba(0,0,0,0.1);">
                  <div style="background-color:#3b82f6; padding:30px; text-align:center; color:white;">
                    <h1 style="margin:0; font-size:28px;">%s</h1>
                  </div>
                  <div style="padding:25px; font-size:14px; color:#374151;">
                    <p>%s</p>
                    <div style="text-align:center; margin-top:20px;">
                      <a href="%s" style="background-color:#3b82f6; color:white; padding:12px 25px; border-radius:5px; text-decoration:none; font-weight:bold; font-size:16px;">Ver promoción</a>
                    </div>
                  </div>
                </div>
              </body>
            </html>
            """.formatted(escapeHtml(title), escapeHtml(message), buttonUrl);
    }

    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static class OrderProduct {
        private final String name;
        private final BigDecimal unitPrice;
        private final int quantity;

        public OrderProduct(String name, BigDecimal unitPrice, int quantity) {
            this.name = name;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
        }

        public String getName() { return name; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public int getQuantity() { return quantity; }
    }
}
