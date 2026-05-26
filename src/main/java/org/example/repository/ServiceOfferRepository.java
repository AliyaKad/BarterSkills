package org.example.repository;

import org.example.entity.ServiceOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ServiceOfferRepository extends JpaRepository<ServiceOffer, Long>, JpaSpecificationExecutor<ServiceOffer> {
}
