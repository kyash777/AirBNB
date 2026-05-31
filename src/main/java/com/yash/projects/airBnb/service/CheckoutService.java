package com.yash.projects.airBnb.service;

import com.yash.projects.airBnb.entity.Booking;

public interface CheckoutService {

    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);

}
