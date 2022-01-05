package edu.uclm.esi.tys2122.model;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Email {
	private final Properties properties = new Properties();

	public void send(String destinatario, String subject, String body) {
		String smtpHost= "smtp.gmail.com";
		String startTTLS="true";
		String port="587";
		String sender="tysweb2122@gmail.com";		// REMITENTE
		String serverUser="tysweb2122@gmail.com";	// USUARIO
		String userAutentication= "true";
		String pwd="EscuelaSITysweb";				// PONER LA CONTRASEÑA
		String fallback="true";	
		
		properties.put("mail.smtp.host", smtpHost);  
        properties.put("mail.smtp.starttls.enable", startTTLS);  
        properties.put("mail.smtp.port", port);  
        properties.put("mail.smtp.mail.sender", sender);  
        properties.put("mail.smtp.user", serverUser);  
        properties.put("mail.smtp.auth", userAutentication);
        properties.put("mail.smtp.socketFactory.port", port);
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", fallback);
        
        Runnable r = new Runnable() {
			@Override
			public void run() {
		        Authenticator auth = new AutentificadorSMTP(sender, pwd);
		        Session session = Session.getInstance(properties, auth);

		        MimeMessage msg = new MimeMessage(session);
		        try {
		        	
			        msg.setSubject(subject);
			        msg.setText(body); 
			        msg.setFrom(new InternetAddress(sender));
			        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(destinatario));
			        Transport.send(msg);
		        } catch (Exception e) {
					System.err.println(e);
				}
			}
		};
		new Thread(r).start();
	}
	public void sendRestore(String destinatario,String token) {
		String smtpHost= "smtp.gmail.com";
		String startTTLS="true";
		String port="587";
		String sender="pepitopruebasadc@gmail.com";		// REMITENTE
		String serverUser="pepitopruebasadc@gmail.com";	// USUARIO
		String userAutentication= "true";
		String pwd="1234abcd_A";				// PONER LA CONTRASEÑA
		String fallback="true";	
		
		properties.put("mail.smtp.host", smtpHost);  
        properties.put("mail.smtp.starttls.enable", startTTLS);  
        properties.put("mail.smtp.port", port);  
        properties.put("mail.smtp.mail.sender", sender);  
        properties.put("mail.smtp.user", serverUser);  
        properties.put("mail.smtp.auth", userAutentication);
        properties.put("mail.smtp.socketFactory.port", port);
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", fallback);
        
        Runnable r = new Runnable() {
			@Override
			public void run() {
		        Authenticator auth = new AutentificadorSMTP(sender, pwd);
		        Session session = Session.getInstance(properties, auth);

		        MimeMessage msg = new MimeMessage(session);
		        try {
		        	
		        	String html = "Test\n" + "\n Cuando haga click en el siguiente enlace, necesitará "
		        			+ "el siguiente codigo:\n"+token+"\nHaga click en el enlace para recuperar la contraseña"
		        			+ "\n<a href='http://localhost:8080/?ojr=restorePassword'>Recuperar contraseña</a>";
			        msg.setSubject("Recuperar contraseña Tysweb");
			        msg.setText(html, "UTF-8", "html"); 
			        msg.setFrom(new InternetAddress(sender));
			        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(destinatario));
			        Transport.send(msg);
		        } catch (Exception e) {
					System.err.println(e);
				}
			}
		};
		new Thread(r).start();
	}
	
	private class AutentificadorSMTP extends javax.mail.Authenticator {
        private String sender;
		private String pwd;

		public AutentificadorSMTP(String sender, String pwd) {
			this.sender = sender;
			this.pwd = pwd;
		}

		public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(sender, pwd);
        }
    }
	
	public static void main(String[] args) throws Exception {
		Email sender=new Email();
		sender.send("macario.polo@uclm.es", "Hola", "Caracola");
		System.out.println("Enviado");
	}
}
