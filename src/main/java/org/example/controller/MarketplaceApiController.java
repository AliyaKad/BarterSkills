package org.example.controller;

import jakarta.servlet.http.HttpSession;
import org.example.entity.*;
import org.example.service.DealService;
import org.example.service.ServiceOfferService;
import org.example.service.ServiceRequestService;
import org.example.service.SkillCoinService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class MarketplaceApiController {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final ServiceOfferService offerService;
    private final ServiceRequestService requestService;
    private final DealService dealService;
    private final SkillCoinService skillCoinService;

    public MarketplaceApiController(ServiceOfferService offerService,
                                    ServiceRequestService requestService,
                                    DealService dealService,
                                    SkillCoinService skillCoinService) {
        this.offerService = offerService;
        this.requestService = requestService;
        this.dealService = dealService;
        this.skillCoinService = skillCoinService;
    }

    @GetMapping("/offers")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> listOffers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice) {
        Category cat = parseCategory(category);
        List<ServiceOffer> offers = offerService.searchOffers(q, cat, minPrice, maxPrice, OfferStatus.ACTIVE);
        return ResponseEntity.ok(offers.stream().map(this::mapOffer).collect(Collectors.toList()));
    }

    @GetMapping("/offers/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getOffer(@PathVariable Long id) {
        return offerService.getOfferById(id)
                .map(o -> ResponseEntity.ok(mapOfferDetail(o)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/offers")
    public ResponseEntity<?> createOffer(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer priceInSkillCoins,
            HttpSession session) {
        Long userId = requireUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        try {
            ServiceOffer offer = offerService.createOffer(userId, title, description,
                    parseCategory(category), priceInSkillCoins);
            return ResponseEntity.ok(mapOffer(offer));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/requests")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> listRequests(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer maxBudget) {
        Category cat = parseCategory(category);
        List<ServiceRequest> requests = requestService.searchRequests(q, cat, maxBudget, RequestStatus.OPEN);
        return ResponseEntity.ok(requests.stream().map(this::mapRequest).collect(Collectors.toList()));
    }

    @GetMapping("/requests/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getRequest(@PathVariable Long id) {
        return requestService.getRequestById(id)
                .map(r -> ResponseEntity.ok(mapRequestDetail(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/requests")
    public ResponseEntity<?> createRequest(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer maxPrice,
            HttpSession session) {
        Long userId = requireUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        try {
            ServiceRequest request = requestService.createRequest(userId, title, description,
                    parseCategory(category), maxPrice);
            return ResponseEntity.ok(mapRequest(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/deals")
    @Transactional(readOnly = true)
    public ResponseEntity<?> listDeals(HttpSession session) {
        Long userId = requireUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        List<Deal> deals = dealService.getDealsForUser(userId);
        return ResponseEntity.ok(deals.stream().map(d -> mapDeal(d, userId)).collect(Collectors.toList()));
    }

    @GetMapping("/deals/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getDeal(@PathVariable Long id, HttpSession session) {
        Long userId = requireUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        return dealService.getDealById(id)
                .filter(d -> dealService.isParticipant(d, userId))
                .map(d -> ResponseEntity.ok(mapDealDetail(d, userId)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/deals/from-offer/{offerId}")
    @Transactional
    public ResponseEntity<?> createDealFromOffer(
            @PathVariable Long offerId,
            @RequestParam(required = false) Integer amount,
            HttpSession session) {
        return runDealAction(session, () -> dealService.createFromOffer(offerId, requireUserId(session), amount));
    }

    @PostMapping("/deals/from-request/{requestId}")
    @Transactional
    public ResponseEntity<?> createDealFromRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) Integer amount,
            HttpSession session) {
        return runDealAction(session, () -> dealService.createFromRequest(requestId, requireUserId(session), amount));
    }

    @PostMapping("/deals/{id}/accept")
    @Transactional
    public ResponseEntity<?> acceptDeal(@PathVariable Long id, HttpSession session) {
        return runDealAction(session, () -> dealService.acceptDeal(id, requireUserId(session)));
    }

    @PostMapping("/deals/{id}/cancel")
    @Transactional
    public ResponseEntity<?> cancelDeal(@PathVariable Long id, HttpSession session) {
        return runDealAction(session, () -> dealService.cancelDeal(id, requireUserId(session)));
    }

    @PostMapping("/deals/{id}/complete")
    @Transactional
    public ResponseEntity<?> confirmDeal(@PathVariable Long id, HttpSession session) {
        return runDealAction(session, () -> dealService.confirmCompletion(id, requireUserId(session)));
    }

    @PostMapping("/deals/{id}/dispute")
    @Transactional
    public ResponseEntity<?> disputeDeal(@PathVariable Long id, HttpSession session) {
        return runDealAction(session, () -> dealService.disputeDeal(id, requireUserId(session)));
    }

    @GetMapping("/transactions")
    @Transactional(readOnly = true)
    public ResponseEntity<?> listTransactions(HttpSession session) {
        Long userId = requireUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        List<Transaction> txs = skillCoinService.getTransactionsForUser(userId);
        return ResponseEntity.ok(txs.stream().map(t -> mapTransaction(t, userId)).collect(Collectors.toList()));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> categories() {
        return ResponseEntity.ok(
                Stream.of(Category.values()).map(Enum::name).collect(Collectors.toList()));
    }

    private ResponseEntity<?> runDealAction(HttpSession session, DealSupplier action) {
        Long userId = requireUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        try {
            Deal deal = action.get();
            deal = dealService.getDealById(deal.getId()).orElse(deal);
            return ResponseEntity.ok(mapDealDetail(deal, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @FunctionalInterface
    private interface DealSupplier {
        Deal get();
    }

    private Long requireUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }

    private Category parseCategory(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Category.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Category.OTHER;
        }
    }

    private Map<String, Object> mapOffer(ServiceOffer o) {
        User author = o.getCreatedBy();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("kind", "offer");
        m.put("title", o.getTitle());
        m.put("description", o.getDescription());
        m.put("category", o.getCategory().name());
        m.put("price", o.getPriceInSkillCoins());
        m.put("status", o.getStatus().name());
        m.put("authorId", author.getId());
        m.put("authorName", formatName(author));
        m.put("authorRating", author.getRating());
        m.put("createdAt", o.getCreatedAt() != null ? o.getCreatedAt().format(DT) : null);
        return m;
    }

    private Map<String, Object> mapOfferDetail(ServiceOffer o) {
        Map<String, Object> m = mapOffer(o);
        m.put("authorEmail", o.getCreatedBy().getEmail());
        return m;
    }

    private Map<String, Object> mapRequest(ServiceRequest r) {
        User author = r.getRequestedBy();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("kind", "request");
        m.put("title", r.getTitle());
        m.put("description", r.getDescription());
        m.put("category", r.getCategory().name());
        m.put("price", r.getMaxPrice());
        m.put("status", r.getStatus().name());
        m.put("authorId", author.getId());
        m.put("authorName", formatName(author));
        m.put("authorRating", author.getRating());
        m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().format(DT) : null);
        return m;
    }

    private Map<String, Object> mapRequestDetail(ServiceRequest r) {
        Map<String, Object> m = mapRequest(r);
        m.put("authorEmail", r.getRequestedBy().getEmail());
        return m;
    }

    private Map<String, Object> mapDeal(Deal d, Long currentUserId) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("status", d.getStatus().name());
        m.put("amount", d.getAmount());
        m.put("coinsHeld", Boolean.TRUE.equals(d.getCoinsHeld()));
        m.put("customerConfirmed", Boolean.TRUE.equals(d.getCustomerConfirmed()));
        m.put("executorConfirmed", Boolean.TRUE.equals(d.getExecutorConfirmed()));
        m.put("createdAt", d.getCreatedAt() != null ? d.getCreatedAt().format(DT) : null);
        m.put("completionDate", d.getCompletionDate() != null ? d.getCompletionDate().format(DT) : null);

        if (d.getOffer() != null) {
            m.put("offerId", d.getOffer().getId());
            m.put("offerTitle", d.getOffer().getTitle());
        }
        if (d.getRequest() != null) {
            m.put("requestId", d.getRequest().getId());
            m.put("requestTitle", d.getRequest().getTitle());
        }

        m.put("customerId", d.getCustomer().getId());
        m.put("customerName", formatName(d.getCustomer()));
        m.put("executorId", d.getExecutor().getId());
        m.put("executorName", formatName(d.getExecutor()));
        m.put("role", d.getCustomer().getId().equals(currentUserId) ? "customer" : "executor");
        return m;
    }

    private Map<String, Object> mapDealDetail(Deal d, Long currentUserId) {
        Map<String, Object> m = mapDeal(d, currentUserId);
        m.put("canAccept", dealService.canAccept(d, currentUserId));
        m.put("canCancel", dealService.canCancel(d, currentUserId));
        m.put("canConfirmCompletion", dealService.canConfirmCompletion(d, currentUserId));
        m.put("canDispute", dealService.canDispute(d, currentUserId));
        return m;
    }

    private Map<String, Object> mapTransaction(Transaction t, Long currentUserId) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("type", t.getType().name());
        m.put("amount", t.getAmount());
        m.put("description", t.getDescription());
        m.put("timestamp", t.getTimestamp() != null ? t.getTimestamp().format(DT) : null);
        m.put("dealId", t.getDeal() != null ? t.getDeal().getId() : null);

        Long fromId = t.getFromUser().getId();
        Long toId = t.getToUser().getId();
        m.put("fromUserId", fromId);
        m.put("toUserId", toId);
        m.put("counterpartyName", fromId.equals(currentUserId)
                ? formatName(t.getToUser())
                : formatName(t.getFromUser()));

        String txType = t.getType().name();
        boolean income = switch (txType) {
            case "BONUS", "TRANSFER" -> toId.equals(currentUserId);
            case "REFUND" -> fromId.equals(currentUserId) && toId.equals(currentUserId);
            case "HOLD" -> false;
            default -> toId.equals(currentUserId);
        };
        m.put("direction", income ? "income" : "expense");
        return m;
    }

    private String formatName(User u) {
        String first = u.getFirstName() != null ? u.getFirstName() : "";
        String last = u.getLastName() != null ? u.getLastName() : "";
        return (first + " " + last).trim();
    }
}
