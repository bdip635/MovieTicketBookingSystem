package com.movieticket.service.pricing;

import com.movieticket.domain.entity.DiscountCode;
import com.movieticket.domain.enums.DiscountType;

public interface DiscountStrategy {

    boolean supports(DiscountType type);

    void validate(DiscountCode code, DiscountContext context);

    DiscountResult apply(DiscountCode code, DiscountContext context);
}
