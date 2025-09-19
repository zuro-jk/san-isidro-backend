package com.sanisidro.restaurante.features.notifications.templates;

import com.sanisidro.restaurante.features.notifications.dto.EmailVerificationEvent;

public class EmailVerificationTemplateBuilder {

    public static String buildVerificationEmail(EmailVerificationEvent event, String recipientName) {
        String name = recipientName != null ? escapeHtml(recipientName) : "Cliente";
        String actionUrl = event.getActionUrl() != null ? event.getActionUrl() : "#";

        return """
                <html>
                  <body style="font-family:'Segoe UI', sans-serif; background-color:#f4f4f6; padding:30px; margin:0;">
                    <div style="max-width:700px; margin:auto; background-color:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 6px 20px rgba(0,0,0,0.1);">

                      <!-- Header -->
                      <div style="background-color:#10b981; padding:30px; text-align:center; color:white;">
                        <h1 style="margin:0; font-size:30px;">¡Hola %s!</h1>
                        <p style="margin:5px 0 0; font-size:18px;">Verifica tu correo electrónico ✅</p>
                      </div>

                      <!-- Body -->
                      <div style="padding:30px; color:#374151; font-size:15px; line-height:1.6;">
                        <p>Gracias por registrarte en <strong>San Isidro Restaurante</strong>! Para activar tu cuenta, por favor haz clic en el botón de abajo:</p>

                        <div style="text-align:center; margin-top:30px; margin-bottom:30px;">
                          <a href="%s" style="background-color:#10b981; color:white; padding:14px 28px; border-radius:8px; text-decoration:none; font-weight:bold; font-size:16px;">Verificar correo</a>
                        </div>

                        <p style="margin-top:25px; font-size:13px; color:#6b7280;">Si no creaste esta cuenta, puedes ignorar este correo.</p>
                      </div>

                      <!-- Footer -->
                      <div style="background-color:#f9fafb; padding:15px; text-align:center; font-size:12px; color:#9ca3af;">
                        © 2025 San Isidro Restaurante. Todos los derechos reservados.
                      </div>

                    </div>
                  </body>
                </html>
                """.formatted(name, actionUrl);
    }

    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
