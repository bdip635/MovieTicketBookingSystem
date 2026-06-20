package com.movieticket.service.catalog;

import com.movieticket.domain.entity.RefundPolicy;
import com.movieticket.domain.entity.RefundPolicyTier;
import com.movieticket.domain.entity.Theater;
import com.movieticket.exception.ApiException;
import com.movieticket.repository.RefundPolicyRepository;
import com.movieticket.web.dto.catalog.CreateRefundPolicyRequest;
import com.movieticket.web.dto.catalog.RefundPolicyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefundPolicyService {

    private final RefundPolicyRepository refundPolicyRepository;
    private final TheaterService theaterService;

    @Transactional
    public RefundPolicyResponse create(CreateRefundPolicyRequest request) {
        Theater theater = theaterService.getTheaterOrThrow(request.theaterId());

        RefundPolicy policy = RefundPolicy.builder()
                .theater(theater)
                .name(request.name().trim())
                .active(request.active())
                .build();

        for (CreateRefundPolicyRequest.RefundPolicyTierRequest tierRequest : request.tiers()) {
            RefundPolicyTier tier = RefundPolicyTier.builder()
                    .minHoursBefore(tierRequest.minHoursBefore())
                    .refundPercentage(tierRequest.refundPercentage())
                    .sortOrder(tierRequest.sortOrder())
                    .build();
            policy.addTier(tier);
        }

        return CatalogMapper.toRefundPolicyResponse(refundPolicyRepository.save(policy));
    }

    @Transactional(readOnly = true)
    public List<RefundPolicyResponse> listAll(UUID theaterId) {
        List<RefundPolicy> policies = theaterId == null
                ? refundPolicyRepository.findAll()
                : refundPolicyRepository.findByTheaterId(theaterId);

        return policies.stream()
                .map(CatalogMapper::toRefundPolicyResponse)
                .toList();
    }
}
