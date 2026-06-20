package com.movieticket.service.catalog;

import com.movieticket.domain.entity.DiscountCode;
import com.movieticket.exception.ApiException;
import com.movieticket.repository.DiscountCodeRepository;
import com.movieticket.web.dto.catalog.CreateDiscountCodeRequest;
import com.movieticket.web.dto.catalog.DiscountCodeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountCodeService {

    private final DiscountCodeRepository discountCodeRepository;

    @Transactional
    public DiscountCodeResponse create(CreateDiscountCodeRequest request) {
        String code = request.code().trim().toUpperCase();

        if (discountCodeRepository.findByCodeIgnoreCase(code).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Discount code already exists: " + code);
        }

        if (!request.validUntil().isAfter(request.validFrom())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "validUntil must be after validFrom");
        }

        DiscountCode discountCode = DiscountCode.builder()
                .code(code)
                .type(request.type())
                .value(request.value())
                .maxDiscountAmount(request.maxDiscountAmount())
                .minOrderAmount(request.minOrderAmount())
                .validFrom(request.validFrom())
                .validUntil(request.validUntil())
                .maxUsageCount(request.maxUsageCount())
                .active(request.active())
                .build();

        return CatalogMapper.toDiscountCodeResponse(discountCodeRepository.save(discountCode));
    }

    @Transactional(readOnly = true)
    public List<DiscountCodeResponse> listAll() {
        return discountCodeRepository.findAll().stream()
                .map(CatalogMapper::toDiscountCodeResponse)
                .toList();
    }
}
