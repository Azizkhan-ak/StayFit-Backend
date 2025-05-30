package com.example.stayfit.stripe;

import com.example.stayfit.dtos.Order;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin("http://localhost:5173")
public class StripeController {

    @Value("${stripe.secret.key}")
    private String secretKey;

    @PostMapping("/create-payment-intent")
    public Map<String, String> createPaymentIntent(@RequestBody Order order) throws StripeException {

        Stripe.apiKey= secretKey;
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(order.getProducts().stream().mapToLong(p->(p.getPrice().longValue()*p.getQuantity())).sum())
                .setCurrency("pkr")
                .addPaymentMethodType("card")
                .setReceiptEmail(order.getEmail())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", intent.getClientSecret());
        return response;
    }
}
