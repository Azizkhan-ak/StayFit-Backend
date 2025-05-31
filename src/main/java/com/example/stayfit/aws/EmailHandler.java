package com.example.stayfit.aws;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
public class EmailHandler {

    private final SesClient sesClient;

    public EmailHandler() {
        this.sesClient = SesClient.builder()
                .region(Region.US_EAST_1) // replace with your SES region
                .build();
    }

    public void sendErrorEmail(String subject,String body) {
        try {
                SendEmailRequest emailRequest = SendEmailRequest.builder()
                        .destination(Destination.builder()
                                .toAddresses("azizullahkhanauk1@gmail.com")
                                .build())
                        .message(Message.builder()
                                .subject(Content.builder()
                                        .data(subject)
                                        .charset("UTF-8")
                                        .build())
                                .body(Body.builder()
                                        .text(Content.builder()
                                                .data(body)
                                                .charset("UTF-8")
                                                .build())
                                        .build())
                                .build())
                        .source("azizullahkhanauk0@gmail.com")
                        .build();

                sesClient.sendEmail(emailRequest);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .destination(Destination.builder()
                            .toAddresses(to)
                            .build())
                    .message(Message.builder()
                            .subject(Content.builder()
                                    .data(subject)
                                    .charset("UTF-8")
                                    .build())
                            .body(Body.builder()
                                    .html(Content.builder()
                                            .data(body)
                                            .charset("UTF-8")
                                            .build())
                                    .build())
                            .build())
                    .source("azizullahkhanauk0@gmail.com")
                    .build();

            sesClient.sendEmail(emailRequest);

        } catch (SesException e) {
            System.err.println("❌ Failed to send email: " + e.awsErrorDetails().errorMessage());
        }
    }

}
