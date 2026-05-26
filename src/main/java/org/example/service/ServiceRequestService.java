package org.example.service;

import jakarta.persistence.criteria.Predicate;
import org.example.entity.Category;
import org.example.entity.RequestStatus;
import org.example.entity.ServiceRequest;
import org.example.entity.User;
import org.example.repository.ServiceRequestRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ServiceRequestService {

    @Autowired
    private ServiceRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ServiceRequest createRequest(Long userId, String title, String description,
                                        Category category, Integer maxPrice) {
        if (title == null || title.isBlank()) {
            throw new RuntimeException("Укажите название запроса");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        ServiceRequest request = ServiceRequest.builder()
                .title(title.trim())
                .description(description != null ? description.trim() : "")
                .category(category != null ? category : Category.OTHER)
                .maxPrice(maxPrice)
                .status(RequestStatus.OPEN)
                .requestedBy(user)
                .build();

        return requestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<ServiceRequest> searchRequests(String query, Category category,
                                               Integer maxBudget, RequestStatus status) {
        return requestRepository.findAll(buildSpecification(query, category, maxBudget, status));
    }

    @Transactional(readOnly = true)
    public Optional<ServiceRequest> getRequestById(Long id) {
        return requestRepository.findById(id);
    }

    private Specification<ServiceRequest> buildSpecification(String query, Category category,
                                                             Integer maxBudget, RequestStatus status) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            RequestStatus effectiveStatus = status != null ? status : RequestStatus.OPEN;
            predicates.add(cb.equal(root.get("status"), effectiveStatus));

            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (maxBudget != null) {
                predicates.add(cb.or(
                        cb.isNull(root.get("maxPrice")),
                        cb.lessThanOrEqualTo(root.get("maxPrice"), maxBudget)
                ));
            }
            if (query != null && !query.isBlank()) {
                String pattern = "%" + query.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            cq.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
