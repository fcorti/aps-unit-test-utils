package com.alfresco.aps.testutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import static org.junit.Assert.*;

import java.io.IOException;

public class MockEmailTransport extends Transport {

	private static final Logger log = LoggerFactory.getLogger(MockEmailTransport.class);

	public MockEmailTransport(Session smtpSession, URLName urlName) {
		super(smtpSession, urlName);
	}

	@Override
	public void sendMessage(Message message, Address[] addresses) throws MessagingException {
		try {

			MimeMultipart multipartMessage = (MimeMultipart) message.getContent();
			// Assert Email props, this will ensure that developers will always
			// put email assertions in the testcases
			assertEquals(1, multipartMessage.getCount());

			log.info(multipartMessage.getBodyPart(0).getContent().toString());

			assertEquals(AbstractTest.expectedEmailMessage.replaceAll("\\s+", ""),
					multipartMessage.getBodyPart(0).getContent().toString().replaceAll("\\s+", ""));

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
