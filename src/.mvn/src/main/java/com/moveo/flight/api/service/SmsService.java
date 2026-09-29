package com.moveo.flight.api.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    public void sendSms(String toPhoneNumber, String messageBody) {
        Message message = Message.creator(
                new PhoneNumber(toPhoneNumber),     // recipient
                new PhoneNumber(twilioPhoneNumber), // Twilio sender
                messageBody
        ).create();

        System.out.println("SMS sent. SID: " + message.getSid());
    }
}
