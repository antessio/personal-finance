package com.personalfinance.transaction.service.impl;

import com.personalfinance.category.model.Category;
import com.personalfinance.category.persistence.CategoryRepository;
import com.personalfinance.transaction.dto.CreateTransactionDTO;
import com.personalfinance.transaction.dto.TransactionDTO;
import com.personalfinance.transaction.model.Transaction;
import com.personalfinance.transaction.persistence.TransactionRepository;
import com.personalfinance.transaction.service.TransactionService;
import com.personalfinance.user.model.User;
import com.personalfinance.user.persistence.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public TransactionDTO createTransaction(CreateTransactionDTO createTransactionDTO, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setDescription(createTransactionDTO.getDescription());
        transaction.setAmount(createTransactionDTO.getAmount());
        transaction.setDate(createTransactionDTO.getDate() != null 
                ? createTransactionDTO.getDate().toLocalDate() 
                : LocalDate.now());
        transaction.setSource(createTransactionDTO.getSource());
        transaction.setSkip(false);
        transaction.setUser(user);
        
        // Handle categories
        if (createTransactionDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(createTransactionDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
            transaction.getCategories().add(category);
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        return convertToDTO(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionDTO updateTransaction(TransactionDTO transactionDTO, Long userId) {
        Transaction transaction = transactionRepository.findById(transactionDTO.getId())
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        transaction.setDescription(transactionDTO.getDescription());
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setDate(transactionDTO.getDate());
        transaction.setUniqueId(transactionDTO.getUniqueId());
        transaction.setSource(transactionDTO.getSource());
        transaction.setSkip(transactionDTO.getSkip());
        
        // Update categories if provided
        if (transactionDTO.getCategoryIds() != null) {
            Set<Category> categories = categoryRepository.findAllById(transactionDTO.getCategoryIds())
                .stream()
                .collect(Collectors.toSet());
                
            if (categories.size() != transactionDTO.getCategoryIds().size()) {
                throw new RuntimeException("One or more categories not found");
            }
            
            transaction.getCategories().clear();
            transaction.getCategories().addAll(categories);
        }

        Transaction updatedTransaction = transactionRepository.save(transaction);
        return convertToDTO(updatedTransaction);
    }

    @Override
    @Transactional
    public void deleteTransaction(UUID id, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
                
        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        transactionRepository.delete(transaction);
    }

    @Override
    public TransactionDTO getTransaction(UUID id, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
                
        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        return convertToDTO(transaction);
    }

    @Override
    public List<TransactionDTO> getTransactionsByUser(Long userId, LocalDate startDate, LocalDate endDate, Boolean skip,
                Long categoryId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return transactionRepository.findByUser(user, skip, startDate, endDate, categoryId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    private TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setDescription(transaction.getDescription());
        dto.setAmount(transaction.getAmount());
        dto.setDate(transaction.getDate());
        dto.setUniqueId(transaction.getUniqueId());
        dto.setSource(transaction.getSource());
        dto.setSkip(transaction.getSkip());
        dto.setInsertedAt(transaction.getInsertedAt());
        dto.setUpdatedAt(transaction.getUpdatedAt());
        
        // Map categories to their IDs
        if (transaction.getCategories() != null) {
            dto.setCategoryIds(transaction.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toSet()));
        }
        
        return dto;
    }

} 