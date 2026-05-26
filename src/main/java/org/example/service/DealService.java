package org.example.service;

import org.example.entity.*;
import org.example.repository.DealRepository;
import org.example.repository.ServiceOfferRepository;
import org.example.repository.ServiceRequestRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DealService {

    private static final List<DealStatus> ACTIVE_DEAL_STATUSES = List.of(
            DealStatus.PROPOSED, DealStatus.ACCEPTED, DealStatus.IN_PROGRESS
    );

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private ServiceOfferRepository offerRepository;

    @Autowired
    private ServiceRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Deal createFromOffer(Long offerId, Long customerId, Integer amount) {
        ServiceOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Предложение не найдено"));

        if (!offer.getStatus().equals(OfferStatus.ACTIVE)) {
            throw new RuntimeException("Предложение недоступно для сделки");
        }
        if (offer.getCreatedBy().getId().equals(customerId)) {
            throw new RuntimeException("Нельзя заключить сделку со своим предложением");
        }
        if (dealRepository.existsByOfferIdAndCustomerIdAndStatusIn(offerId, customerId, ACTIVE_DEAL_STATUSES)) {
            throw new RuntimeException("У вас уже есть активная сделка по этому предложению");
        }

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        int dealAmount = resolveAmount(amount, offer.getPriceInSkillCoins());

        Deal deal = Deal.builder()
                .offer(offer)
                .executor(offer.getCreatedBy())
                .customer(customer)
                .amount(dealAmount)
                .status(DealStatus.PROPOSED)
                .build();

        return dealRepository.save(deal);
    }

    @Transactional
    public Deal createFromRequest(Long requestId, Long executorId, Integer amount) {
        ServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Запрос не найден"));

        if (!request.getStatus().equals(RequestStatus.OPEN)) {
            throw new RuntimeException("Запрос недоступен для сделки");
        }
        if (request.getRequestedBy().getId().equals(executorId)) {
            throw new RuntimeException("Нельзя откликнуться на свой запрос");
        }
        if (dealRepository.existsByRequestIdAndExecutorIdAndStatusIn(requestId, executorId, ACTIVE_DEAL_STATUSES)) {
            throw new RuntimeException("У вас уже есть активная сделка по этому запросу");
        }

        User executor = userRepository.findById(executorId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        int dealAmount = resolveAmount(amount, request.getMaxPrice());

        Deal deal = Deal.builder()
                .request(request)
                .executor(executor)
                .customer(request.getRequestedBy())
                .amount(dealAmount)
                .status(DealStatus.PROPOSED)
                .build();

        return dealRepository.save(deal);
    }

    @Transactional(readOnly = true)
    public List<Deal> getDealsForUser(Long userId) {
        return dealRepository.findAllForUser(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Deal> getDealById(Long dealId) {
        return dealRepository.findById(dealId);
    }

    @Transactional
    public Deal acceptDeal(Long dealId, Long userId) {
        Deal deal = loadDealForParticipant(dealId, userId);

        if (deal.getStatus() != DealStatus.PROPOSED) {
            throw new RuntimeException("Сделку можно принять только в статусе PROPOSED");
        }
        if (!canAccept(deal, userId)) {
            throw new RuntimeException("Вы не можете принять эту сделку");
        }

        deal.accept();

        if (deal.getRequest() != null) {
            ServiceRequest request = deal.getRequest();
            request.setStatus(RequestStatus.IN_PROGRESS);
            requestRepository.save(request);
        }

        return dealRepository.save(deal);
    }

    @Transactional
    public Deal cancelDeal(Long dealId, Long userId) {
        Deal deal = loadDealForParticipant(dealId, userId);

        if (deal.getStatus() == DealStatus.COMPLETED || deal.getStatus() == DealStatus.CANCELLED) {
            throw new RuntimeException("Сделку нельзя отменить");
        }

        deal.cancel();
        reopenRequestIfNeeded(deal);

        return dealRepository.save(deal);
    }

    @Transactional
    public Deal completeDeal(Long dealId, Long userId) {
        Deal deal = loadDealForParticipant(dealId, userId);

        if (deal.getStatus() != DealStatus.ACCEPTED && deal.getStatus() != DealStatus.IN_PROGRESS) {
            throw new RuntimeException("Завершить можно только принятую сделку");
        }

        deal.complete();

        if (deal.getOffer() != null) {
            ServiceOffer offer = deal.getOffer();
            offer.setStatus(OfferStatus.COMPLETED);
            offerRepository.save(offer);
        }
        if (deal.getRequest() != null) {
            ServiceRequest request = deal.getRequest();
            request.setStatus(RequestStatus.DONE);
            requestRepository.save(request);
        }

        return dealRepository.save(deal);
    }

    public boolean canAccept(Deal deal, Long userId) {
        if (deal.getStatus() != DealStatus.PROPOSED) {
            return false;
        }
        if (deal.getOffer() != null) {
            return deal.getExecutor().getId().equals(userId);
        }
        if (deal.getRequest() != null) {
            return deal.getCustomer().getId().equals(userId);
        }
        return false;
    }

    public boolean canCancel(Deal deal, Long userId) {
        if (!isParticipant(deal, userId)) {
            return false;
        }
        return deal.getStatus() != DealStatus.COMPLETED
                && deal.getStatus() != DealStatus.CANCELLED;
    }

    public boolean canComplete(Deal deal, Long userId) {
        if (!isParticipant(deal, userId)) {
            return false;
        }
        return deal.getStatus() == DealStatus.ACCEPTED
                || deal.getStatus() == DealStatus.IN_PROGRESS;
    }

    public boolean isParticipant(Deal deal, Long userId) {
        return deal.getCustomer().getId().equals(userId)
                || deal.getExecutor().getId().equals(userId);
    }

    private Deal loadDealForParticipant(Long dealId, Long userId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Сделка не найдена"));
        if (!isParticipant(deal, userId)) {
            throw new RuntimeException("Нет доступа к этой сделке");
        }
        return deal;
    }

    private int resolveAmount(Integer amount, Integer defaultAmount) {
        int value = amount != null ? amount : (defaultAmount != null ? defaultAmount : 0);
        if (value < 0) {
            throw new RuntimeException("Сумма не может быть отрицательной");
        }
        return value;
    }

    private void reopenRequestIfNeeded(Deal deal) {
        if (deal.getRequest() != null && deal.getRequest().getStatus() == RequestStatus.IN_PROGRESS) {
            ServiceRequest request = deal.getRequest();
            request.setStatus(RequestStatus.OPEN);
            requestRepository.save(request);
        }
    }
}
