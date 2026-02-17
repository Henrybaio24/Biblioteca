package biblioteca.util;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public final class EnviadorCorreo {
    
    private static final Logger LOG = Logger.getLogger(EnviadorCorreo.class.getName());
    
    private EnviadorCorreo() {}
    
    public static boolean enviarCorreoConPDF(String destinatario, String nombreUsuario,
                                             byte[] pdfBytes, String nombreArchivo) {
        try {
            // Configurar propiedades SMTP (587 + STARTTLS)
            Properties props = new Properties();
            props.put("mail.smtp.host", ConfiguracionCorreo.HOST_SMTP);      // smtp.gmail.com
            props.put("mail.smtp.port", String.valueOf(ConfiguracionCorreo.PUERTO)); // 465
            props.put("mail.smtp.auth", "true");

            // SSL implícito en 465
            props.put("mail.smtp.socketFactory.port", String.valueOf(ConfiguracionCorreo.PUERTO));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.starttls.enable", "false");
            props.put("mail.smtp.starttls.required", "false");

            // Crear sesión con autenticación
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            ConfiguracionCorreo.EMAIL_REMITENTE,
                            ConfiguracionCorreo.PASSWORD_APP
                    );
                }
            });
            session.setDebug(true); // ver el log SMTP en consola

            // Crear mensaje
            Message mensaje = new MimeMessage(session);
            mensaje.setFrom(new InternetAddress(ConfiguracionCorreo.EMAIL_REMITENTE));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensaje.setSubject("Comprobante de Préstamo Bibliotecario");
            
            // Cuerpo del correo
            MimeMultipart multipart = new MimeMultipart();
            
            // Texto
            MimeBodyPart textoBodyPart = new MimeBodyPart();
            textoBodyPart.setText(
                "Hola " + nombreUsuario + ",\n\n" +
                "Te enviamos el comprobante en PDF del préstamo que acabas de realizar en la biblioteca.\n" +
                "En el archivo encontrarás los datos del material y las fechas de préstamo y devolución.\n\n" +
                "Por favor, respeta la fecha de devolución para evitar bloqueos o sanciones.\n\n" +
                "Saludos cordiales,\nSistema de Gestión Bibliotecaria",
                "utf-8"
            );

            multipart.addBodyPart(textoBodyPart);
            
            // Adjuntar PDF
            MimeBodyPart adjuntoBodyPart = new MimeBodyPart();
            adjuntoBodyPart.setFileName(nombreArchivo);
            adjuntoBodyPart.setContent(pdfBytes, "application/pdf");
            multipart.addBodyPart(adjuntoBodyPart);
            
            mensaje.setContent(multipart);
            
            // Enviar
            Transport.send(mensaje);
            
            LOG.info("Correo enviado exitosamente a: " + destinatario);
            return true;
            
        } catch (MessagingException e) {
            LOG.log(Level.SEVERE, "Error al enviar correo", e);
            return false;
        }
    }
}


