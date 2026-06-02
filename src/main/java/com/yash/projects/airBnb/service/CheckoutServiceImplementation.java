package com.yash.projects.airBnb.service;

import com.stripe.exception.StripeException;
import com.yash.projects.airBnb.exception.BadRequestException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.yash.projects.airBnb.entity.Booking;
import com.yash.projects.airBnb.entity.User;
import com.yash.projects.airBnb.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutServiceImplementation implements CheckoutService{

    private final BookingRepository bookingRepository;
    private final RequestOptions stripeRequestOptions;

    @Value("${stripe.secret.key:}")
    private String stripeSecretKey;

    @Override
    public String getCheckoutSession(Booking booking, String successUrl, String failureUrl) {
        log.info("Creating session for booking with ID: {}", booking.getId());
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
            throw new IllegalStateException("Stripe secret key is not configured");
        }

        // Pre-check booking amount to avoid Stripe's amount_too_small error.
        // Stripe requires the converted amount to be at least $0.50. Use a sensible
        // business minimum in INR (e.g., INR 50). Adjust as needed for your application.
        final BigDecimal MIN_INR_AMOUNT = BigDecimal.valueOf(50);
        if (booking.getAmount() == null || booking.getAmount().compareTo(MIN_INR_AMOUNT) < 0) {
            throw new BadRequestException("Booking amount is too small for payments. Minimum allowed amount is INR " + MIN_INR_AMOUNT);
        }

        try {
            CustomerCreateParams customerParams = CustomerCreateParams.builder()
                    .setName(user.getName())
                    .setEmail(user.getEmail())
                    .build();
            Customer customer = Customer.create(customerParams, stripeRequestOptions);

            SessionCreateParams sessionParams = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                    .setCustomer(customer.getId())
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(failureUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("inr")
                                                    .setUnitAmount(booking.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(booking.getHotel().getName() +" : "+ booking.getRoom().getType())
                                                                    .setDescription("Booking ID: "+booking.getId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(sessionParams, stripeRequestOptions);

            booking.setPaymentSessionId(session.getId());
            bookingRepository.save(booking);

            log.info("Session created successfully for booking with ID: {}", booking.getId());
            return session.getUrl();

        } catch (StripeException e) {
            // Map Stripe's amount_too_small to a client-friendly 400 Bad Request
            if ("amount_too_small".equals(e.getCode())) {
                throw new BadRequestException("Payment amount too small for Stripe: " + e.getMessage());
            }
            throw new RuntimeException(e);
        }


    }
}
