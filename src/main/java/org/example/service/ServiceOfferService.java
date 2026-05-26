package org.example.service;

import jakarta.persistence.criteria.Predicate;
import org.example.entity.Category;
import org.example.entity.OfferStatus;
import org.example.entity.ServiceOffer;
import org.example.entity.User;
import org.example.repository.ServiceOfferRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ServiceOfferService {

    @Autowired
    private ServiceOfferRepository offerRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ServiceOffer createOffer(Long userId, String title, String description,
                                    Category category, Integer priceInSkillCoins) {
        if (title == null || title.isBlank()) {
            throw new RuntimeException("Укажите название предложения");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        ServiceOffer offer = ServiceOffer.builder()
                .title(title.trim())
                .description(description != null ? description.trim() : "")
                .category(category != null ? category : Category.OTHER)
                .priceInSkillCoins(priceInSkillCoins != null ? priceInSkillCoins : 0)
                .status(OfferStatus.ACTIVE)
                .createdBy(user)
                .build();

        return offerRepository.save(offer);
    }

    @Transactional(readOnly = true)
    public List<ServiceOffer> searchOffers(String query, Category category,
                                           Integer minPrice, Integer maxPrice,
                                           OfferStatus status) {
        return offerRepository.findAll(buildSpecification(query, category, minPrice, maxPrice, status));
    }

    @Transactional(readOnly = true)
    public Optional<ServiceOffer> getOfferById(Long id) {
        return offerRepository.findById(id);
    }

    private Specification<ServiceOffer> buildSpecification(String query, Category category,
                                                           Integer minPrice, Integer maxPrice,
                                                           OfferStatus status) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            OfferStatus effectiveStatus = status != null ? status : OfferStatus.ACTIVE;
            predicates.add(cb.equal(root.get("status"), effectiveStatus));

            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("priceInSkillCoins"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("priceInSkillCoins"), maxPrice));
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
