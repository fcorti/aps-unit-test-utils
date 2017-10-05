package com.alfresco.aps.testutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MockEmailTransport extends Transport {

	private static final Logger log = LoggerFactory.getLogger(MockEmailTransport.class);

	public MockEmailTransport(Session smtpSession, URLName urlName) {
		super(smtpSession, urlName);
	}

	@Override
	public void sendMessage(Message message, Address[] addresses) throws MessagingException {
		try {

			MimeMultipart multipartMessage = (MimeMultipart) message.getContent();
			EmailType email = new EmailType();

			email.setBody(multipartMessage.getBodyPart(0).getContent().toString());
			
			email.setSubject(message.getSubject());
			
			if(message.getFrom()!=null && message.getFrom().length>0){
				email.setFrom(message.getFrom()[0].toString());
			}
			

			InternetAddress[] toAddressList = (InternetAddress[]) message.getRecipients(Message.RecipientType.TO);
			if (toAddressList.length > 0) {
				List<String> toList = new ArrayList<String>();
				for (InternetAddress toAddress : toAddressList) {
					toList.add(toAddress.toString());
				}
				email.setTo(toList);
			}

			InternetAddress[] ccAddressList = (InternetAddress[]) message.getRecipients(Message.RecipientType.CC);
			if (ccAddressList.length > 0) {
				List<String> ccList = new ArrayList<String>();
				for (InternetAddress ccAddress : ccAddressList) {
					ccList.add(ccAddress.toString());
				}
				email.setCc(ccList);
			}

			InternetAddress[] bccAddressList = (InternetAddress[]) message.getRecipients(Message.RecipientType.BCC);
			if (bccAddressList.length > 0) {
				List<String> bccList = new ArrayList<String>();
				for (InternetAddress bccAddress : bccAddressList) {
					bccList.add(bccAddress.toString());
				}
				email.setBcc(bccList);
			}
			AbstractBpmnTest.actualEmails.add(email);

		} catch (IOException ex) {
			log.error("Email assertion failed" + ex);
		}
	}

	@Override
	public void connect() throws MessagingException {
	}

	@Override
	public void connect(String host, int port, String username, String password) throws MessagingException {
	}

	@Override
	public void connect(String host, String username, String password) throws MessagingException {
	}

	@Override
	public void close() {
	}
}
