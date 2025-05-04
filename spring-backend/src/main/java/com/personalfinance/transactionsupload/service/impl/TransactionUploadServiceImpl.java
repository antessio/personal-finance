package com.personalfinance.transactionsupload.service.impl;

import com.personalfinance.transactionsupload.dto.TransactionUploadDTO;
import com.personalfinance.transactionsupload.dto.TransactionUploadImport;
import com.personalfinance.transactionsupload.model.TransactionUpload;
import com.personalfinance.transactionsupload.model.TransactionUploadStatus;
import com.personalfinance.transactionsupload.persistence.TransactionUploadRepository;
import com.personalfinance.transactionsupload.service.TransactionUploadService;
import com.personalfinance.user.model.User;
import com.personalfinance.user.persistence.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransactionUploadServiceImpl implements TransactionUploadService {

    private final TransactionUploadRepository uploadRepository;
    private final UserRepository userRepository;

    public TransactionUploadServiceImpl(TransactionUploadRepository uploadRepository, UserRepository userRepository) {
        this.uploadRepository = uploadRepository;
        this.userRepository = userRepository;
    }

    @Override
    public TransactionUploadDTO importTransactions(TransactionUploadImport transactionUploadImport, Long userId) {
        TransactionUpload upload = new TransactionUpload();
        upload.setSourceType(transactionUploadImport.getSourceType());
        upload.setFilePath(transactionUploadImport.getFilePath());
        upload.setStatus(TransactionUploadStatus.PENDING);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        upload.setUser(user);
        TransactionUpload savedUpload = uploadRepository.save(upload);
        return convertToDTO(savedUpload);
    }

    @Override
    public void deleteUpload(Long id, Long userId) {
        TransactionUpload upload = uploadRepository.findById(id)
                .filter(u -> u.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Upload not found"));
        uploadRepository.delete(upload);
    }

    @Override
    public Optional<TransactionUploadDTO> getUpload(Long id, Long userId) {
        return uploadRepository.findById(id)
                .filter(upload -> upload.getUser().getId().equals(userId))
               .map(this::convertToDTO);
    }

    @Override
    public List<TransactionUploadDTO> getUploadsByUser(Long userId) {
        return uploadRepository.findByUserId(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private TransactionUploadDTO convertToDTO(TransactionUpload upload) {
        TransactionUploadDTO dto = new TransactionUploadDTO();
        dto.setId(upload.getId());
        dto.setSourceType(upload.getSourceType());
        dto.setFilePath(upload.getFilePath());
        dto.setStatus(upload.getStatus().name());
        dto.setInsertedAt(upload.getInsertedAt());
        dto.setUpdatedAt(upload.getUpdatedAt());
        return dto;
    }
}