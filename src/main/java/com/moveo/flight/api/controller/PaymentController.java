package com.moveo.flight.api.controller;

import com.moveo.flight.api.dto.PaymentDTO;
import com.moveo.flight.api.model.*;
import com.moveo.flight.api.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.json.JSONObject;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret; // Replace with your actual Stripe webhook secret

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Pay for a reservation
    @PostMapping("/{reservationId}")
    public ResponseEntity<PaymentDTO> payForReservation(@PathVariable Long reservationId, @RequestParam(defaultValue = "EMAIL") NotificationType notificationType) throws StripeException {
        PaymentDTO payment = paymentService.processPayment(reservationId, notificationType);
        return ResponseEntity.ok(payment);
    }

    // Get payment details for a reservation
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<Payment> getPayment(@PathVariable Long reservationId) {
        Payment payment = paymentService.getPaymentByReservation(reservationId);
        if (payment == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(payment);
    }

    // Confirm payment after Stripe client-side confirmation
    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                  @RequestHeader("Stripe-Signature") String sigHeader) {
    try {
        // Verify webhook signature
        Event event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);

        if ("payment_intent.succeeded".equals(event.getType())) {
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            PaymentIntent intent = null;
            if (deserializer.getObject().isPresent()) {
                // Full object payment intent is present
                intent = (PaymentIntent) deserializer.getObject().get();
            } else {
                // Use getRawJson() and org.json to extract the PaymentIntent ID
                String rawJson = deserializer.getRawJson();
                if (rawJson != null) {
                    JSONObject json = new JSONObject(rawJson);
                    if (json.has("id")) {
                        String paymentIntentId = json.getString("id");
                        
                        try {
                            intent = PaymentIntent.retrieve(paymentIntentId);
                        } catch (StripeException e) {
                            return ResponseEntity.badRequest().body("Could not retrieve PaymentIntent: " + e.getMessage());
                        }
                    } else {
                        return ResponseEntity.badRequest().body("Missing PaymentIntent ID");
                    }
                } else {
                    return ResponseEntity.badRequest().body("Missing PaymentIntent data");
                }
            }

            if (intent != null){
                // Update payment, reservation and notification status in the database
                String paymentIntentId = intent.getId();
                paymentService.handleSuccessfulPayment(paymentIntentId, intent.getMetadata().get("notificationType"));
            }
        } else if ("payment_intent.payment_failed".equals(event.getType())) {
            PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new RuntimeException("Missing intent"));

            String paymentIntentId = intent.getId();
            paymentService.handleFailedPayment(paymentIntentId);
        } else {
            return ResponseEntity.ok("Ignored");
        }
        return ResponseEntity.ok("Webhook handled");
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Webhook error: " + e.getMessage());
    }
}
}
