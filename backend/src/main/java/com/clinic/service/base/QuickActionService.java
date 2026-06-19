package com.clinic.service.base;

import com.clinic.dto.base.QuickActionResponse;
import com.clinic.entity.base.QuickAction;
import com.clinic.repository.base.QuickActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuickActionService {

    private final QuickActionRepository quickActionRepository;

    @Transactional(readOnly = true)
    public List<QuickActionResponse> getAllActiveQuickActions() {
        return quickActionRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuickActionResponse getQuickActionById(Integer id) {
        return quickActionRepository.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public QuickActionResponse getQuickActionBySlug(String slug) {
        // Tìm tất cả rồi lọc (có thể tối ưu bằng repository)
        return quickActionRepository.findAll().stream()
                .filter(q -> q.getSlug().equals(slug))
                .map(this::toResponse)
                .findFirst()
                .orElse(null);
    }

    private QuickActionResponse toResponse(QuickAction entity) {
        return QuickActionResponse.builder()
                .actionId(entity.getActionId())
                .title(entity.getTitle())
                .slug(entity.getSlug())
                .iconUrl(entity.getIconUrl())
                .displayOrder(entity.getDisplayOrder())
                .isActive(entity.getIsActive())
                .build();
    }
}