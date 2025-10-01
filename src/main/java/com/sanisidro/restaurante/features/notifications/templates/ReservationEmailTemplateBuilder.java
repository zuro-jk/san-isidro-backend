package com.sanisidro.restaurante.features.notifications.templates;

import com.sanisidro.restaurante.features.notifications.dto.ReservationNotificationEvent;

import java.time.format.DateTimeFormatter;

public class ReservationEmailTemplateBuilder {

    public static String buildReservationConfirmationEmail(ReservationNotificationEvent event, String frontendUrl) {
        String date = event.getReservationDate() != null
                ? event.getReservationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "Fecha no disponible";

        String time = event.getReservationTime() != null ? event.getReservationTime() : "Hora no disponible";

        // 🔗 Construcción del link hacia tu frontend
        String actionUrl = frontendUrl + "/reservations/" + event.getReservationId();

        return """
                <html>
                  <body style="font-family:'Segoe UI', sans-serif; background-color:#f4f4f6; padding:30px; margin:0;">
                    <div style="max-width:700px; margin:auto; background-color:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 6px 20px rgba(0,0,0,0.1);">
                
                      <!-- Header -->
                      <div style="background-color:#10b981; padding:30px; text-align:center; color:white;">
                        <h1 style="margin:0; font-size:30px;">¡Hola %s!</h1>
                        <p style="margin:5px 0 0; font-size:18px;">Tu reserva #%d ha sido confirmada ✅</p>
                      </div>
                
                      <!-- Body -->
                      <div style="padding:30px; color:#374151; font-size:15px; line-height:1.6;">
                        <p>Gracias por elegir <strong>San Isidro Restaurante</strong>! Aquí tienes los detalles de tu reserva:</p>
                
                        <table style="width:100%%; border-collapse:collapse; margin-top:15px; margin-bottom:25px;">
                          <tr style="background-color:#f3f4f6;">
                            <td style="padding:12px; font-weight:bold; width:120px;">Fecha</td>
                            <td style="padding:12px;">%s</td>
                          </tr>
                          <tr>
                            <td style="padding:12px; font-weight:bold;">Hora</td>
                            <td style="padding:12px;">%s</td>
                          </tr>
                          <tr style="background-color:#f3f4f6;">
                            <td style="padding:12px; font-weight:bold;">Personas</td>
                            <td style="padding:12px;">%d</td>
                          </tr>
                          <tr>
                            <td style="padding:12px; font-weight:bold;">Mesa</td>
                            <td style="padding:12px;">%s</td>
                          </tr>
                        </table>
                
                        <div style="text-align:center; margin-top:20px;">
                          <a href="%s" style="background-color:#10b981; color:white; padding:14px 28px; border-radius:8px; text-decoration:none; font-weight:bold; font-size:16px;">Ver mi reserva</a>
                        </div>
                
                        <p style="margin-top:25px; font-size:13px; color:#6b7280;">Si tienes dudas, contáctanos respondiendo este correo o llamando al restaurante.</p>
                      </div>
                
                      <!-- Footer -->
                      <div style="background-color:#f9fafb; padding:15px; text-align:center; font-size:12px; color:#9ca3af;">
                        © 2025 San Isidro Restaurante. Todos los derechos reservados.
                      </div>
                
                    </div>
                  </body>
                </html>
                """.formatted(
                escapeHtml(event.getCustomerName()),
                event.getReservationId(),
                date,
                time,
                event.getNumberOfPeople(),
                escapeHtml(event.getTableName()),
                actionUrl
        );
    }

    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}