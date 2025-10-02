package com.sanisidro.restaurante.features.notifications.templates;

public class ContactEmailTemplateBuilder {

    public static String buildContactEmail(String senderName, String senderEmail, String senderPhone, String subject, String message, String actionUrl) {
        String actionButton = "";
        if (actionUrl != null && !actionUrl.isBlank()) {
            actionButton = """
                <div style="text-align:center; margin-top:25px;">
                    <a href="%s" style="background-color:#3b82f6; color:white; padding:12px 25px; border-radius:5px; text-decoration:none; font-weight:bold; font-size:16px;">Ver detalles</a>
                </div>
            """.formatted(escapeHtml(actionUrl));
        }

        return """
            <html>
              <body style="font-family:'Segoe UI', sans-serif; background-color:#f4f4f6; padding:20px;">
                <div style="max-width:650px; margin:auto; background-color:#ffffff; border-radius:10px; overflow:hidden; box-shadow:0 4px 15px rgba(0,0,0,0.1);">
                  
                  <!-- Header -->
                  <div style="background-color:#3b82f6; padding:30px; text-align:center; color:white;">
                    <h1 style="margin:0; font-size:28px;">Nuevo mensaje de contacto</h1>
                  </div>

                  <!-- Sender Info -->
                  <div style="padding:20px; background-color:#f3f4f6; border-bottom:1px solid #e5e7eb;">
                    <p><strong>Nombre:</strong> %s</p>
                    <p><strong>Email:</strong> %s</p>
                    <p><strong>Teléfono:</strong> %s</p>
                    <p><strong>Asunto:</strong> %s</p>
                  </div>

                  <!-- Message Body -->
                  <div style="padding:25px; font-size:14px; color:#374151;">
                    <p style="margin-bottom:0;"><strong>Mensaje:</strong></p>
                    <p style="padding:15px; background-color:#f9fafb; border-left:4px solid #3b82f6; border-radius:5px;">%s</p>
                    %s
                  </div>

                  <!-- Footer -->
                  <div style="padding:15px; font-size:12px; color:#9ca3af; text-align:center;">
                    Este mensaje fue enviado desde el formulario de contacto del sitio web.
                  </div>
                </div>
              </body>
            </html>
        """.formatted(
                escapeHtml(senderName),
                escapeHtml(senderEmail),
                escapeHtml(senderPhone != null && !senderPhone.isBlank() ? senderPhone : "N/A"),
                escapeHtml(subject),
                escapeHtml(message),
                actionButton
        );
    }

    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}