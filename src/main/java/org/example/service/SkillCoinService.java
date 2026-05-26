package org.example.service;

import org.example.entity.*;
import org.example.repository.TransactionRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SkillCoinService {

    public static final int REGISTRATION_BONUS = 50;
    public static final int SKILLS_SECTION_BONUS = 20;
    public static final int SKILLS_SECTION_THRESHOLD = 3;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public boolean canAfford(User customer, int amount) {
        return amount <= 0 || customer.hasAvailableSkillCoins(amount);
    }

    @Transactional
    public void holdForDeal(Deal deal) {
        if (Boolean.TRUE.equals(deal.getCoinsHeld())) {
            return;
        }

        int amount = deal.getAmount();
        if (amount <= 0) {
            deal.setCoinsHeld(true);
            return;
        }

        User customer = reloadUser(deal.getCustomer().getId());
        if (!canAfford(customer, amount)) {
            throw new RuntimeException(
                    "Недостаточно SkillCoin для сделки. Нужно: " + amount
                            + ", доступно: " + customer.getSkillCoinBalance());
        }

        customer.holdSkillCoins(amount);
        userRepository.save(customer);

        saveTransaction(deal, customer, customer, amount, TransactionType.HOLD,
                "Заморозка " + amount + " SC по сделке #" + deal.getId());

        deal.setCoinsHeld(true);
    }

    @Transactional
    public void finalizeDealPayment(Deal deal) {
        int amount = deal.getAmount();
        if (amount <= 0) {
            deal.setCoinsHeld(false);
            return;
        }

        if (!Boolean.TRUE.equals(deal.getCoinsHeld())) {
            holdForDeal(deal);
        }

        User customer = reloadUser(deal.getCustomer().getId());
        User executor = reloadUser(deal.getExecutor().getId());

        customer.releaseHeldSkillCoins(amount);
        executor.addSkillCoins(amount);
        userRepository.save(customer);
        userRepository.save(executor);

        saveTransaction(deal, customer, executor, amount, TransactionType.TRANSFER,
                "Оплата " + amount + " SC по сделке #" + deal.getId());

        deal.setCoinsHeld(false);
    }

    @Transactional
    public void refundHold(Deal deal) {
        if (!Boolean.TRUE.equals(deal.getCoinsHeld())) {
            return;
        }

        int amount = deal.getAmount();
        if (amount <= 0) {
            deal.setCoinsHeld(false);
            return;
        }

        User customer = reloadUser(deal.getCustomer().getId());
        customer.refundHeldSkillCoins(amount);
        userRepository.save(customer);

        saveTransaction(deal, customer, customer, amount, TransactionType.REFUND,
                "Возврат " + amount + " SC по сделке #" + deal.getId());

        deal.setCoinsHeld(false);
    }

    @Transactional
    public void grantRegistrationBonus(User user) {
        grantBonus(user, REGISTRATION_BONUS, "Бонус за регистрацию");
    }

    @Transactional
    public int tryGrantSkillsCanOfferBonus(User user) {
        if (Boolean.TRUE.equals(user.getBonusSkillsCanOfferGranted())) {
            return 0;
        }
        if (user.getSkillsCanOffer().size() < SKILLS_SECTION_THRESHOLD) {
            return 0;
        }
        user.setBonusSkillsCanOfferGranted(true);
        grantBonus(user, SKILLS_SECTION_BONUS,
                "Бонус за добавление первых " + SKILLS_SECTION_THRESHOLD + " навыков «Могу предложить»");
        return SKILLS_SECTION_BONUS;
    }

    @Transactional
    public int tryGrantSkillsNeededBonus(User user) {
        if (Boolean.TRUE.equals(user.getBonusSkillsNeededGranted())) {
            return 0;
        }
        if (user.getSkillsNeeded().size() < SKILLS_SECTION_THRESHOLD) {
            return 0;
        }
        user.setBonusSkillsNeededGranted(true);
        grantBonus(user, SKILLS_SECTION_BONUS,
                "Бонус за добавление первых " + SKILLS_SECTION_THRESHOLD + " потребностей «Мне требуется»");
        return SKILLS_SECTION_BONUS;
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsForUser(Long userId) {
        return transactionRepository.findAllForUser(userId);
    }

    private void grantBonus(User user, int amount, String description) {
        user.addSkillCoins(amount);
        userRepository.save(user);
        saveBonusTransaction(user, amount, description);
    }

    private void saveBonusTransaction(User user, int amount, String description) {
        Transaction transaction = Transaction.builder()
                .fromUser(user)
                .toUser(user)
                .amount(amount)
                .type(TransactionType.BONUS)
                .description(description)
                .build();
        transactionRepository.save(transaction);
    }

    private User reloadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    private void saveTransaction(Deal deal, User fromUser, User toUser, int amount,
                               TransactionType type, String description) {
        Transaction transaction = Transaction.builder()
                .deal(deal)
                .fromUser(fromUser)
                .toUser(toUser)
                .amount(amount)
                .type(type)
                .description(description)
                .build();
        transactionRepository.save(transaction);
    }
}
