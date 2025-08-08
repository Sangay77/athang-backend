package com.bfs.rma.fee;

import com.bfs.rma.auth.model.AppUser;
import com.bfs.rma.auth.repository.UserRepository;
import com.bfs.rma.auth.util.AuthUtils;
import com.bfs.rma.payment.dto.AuthoriseRequestDTO;
import com.bfs.rma.payment.service.AuthoriseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FeeGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(FeeGenerationService.class);

    @Autowired
    private FeeGenerationRepository feeGenerationRepository;

    @Autowired
    private UserRepository appUserRepository;

    @Autowired
    private AuthoriseService authoriseService;

    public Map<String, String> createFee(FeeRequest request) throws Exception {
        logger.info("Initiating fee generation process...");

        String username = AuthUtils.getCurrentUsername();
        logger.info("Extracted current username from SecurityContext: {}", username);

        AppUser user = appUserRepository.getUserByEmail(username)
                .orElseThrow(() -> {
                    logger.error("User not found for username: {}", username);
                    return new RuntimeException("User not found: " + username);
                });

        logger.info("Fetched user: {} (ID: {})", user.getEmail(), user.getId());

        FeeGeneration fee = FeeGeneration.builder()
                .feeType(request.getFeeType())
                .traineeType(request.getTraineeType())
                .amount(request.getAmount())
                .feeStatus(FeeStatus.PENDING)
                .user(user)
                .build();

        feeGenerationRepository.save(fee);
        logger.info("Saved fee record for user {} with amount {} and feeType {}", username, request.getAmount(), request.getFeeType());

        AuthoriseRequestDTO authoriseRequest = new AuthoriseRequestDTO();
        authoriseRequest.setTxnAmount(String.valueOf(request.getAmount()));
        authoriseRequest.setBfs_paymentDesc(request.getFeeType());

        try {
            authoriseRequest.setFormId(fee.getId());
        } catch (NumberFormatException e) {
            logger.error("Invalid traineeType value: {}", request.getTraineeType(), e);
            throw new IllegalArgumentException("Invalid traineeType: must be an integer", e);
        }

        logger.info("Sending AuthoriseRequestDTO to AuthoriseService: {}", authoriseRequest);
        Map<String, String> response = authoriseService.authService(authoriseRequest);
        logger.info("Received response from AuthoriseService: {}", response);

        return response;
    }

    public List<FeeGeneration> getMyFees(Authentication authentication) {
        String username = authentication.getName();
        logger.info("Fetching fee records for user: {}", username);

        AppUser user = appUserRepository.getUserByEmail(username)
                .orElseThrow(() -> {
                    logger.error("User not found while fetching fees: {}", username);
                    return new RuntimeException("User not found");
                });

        List<FeeGeneration> fees = feeGenerationRepository.findByUser(user);
        logger.info("Fetched {} fee(s) for user: {}", fees.size(), username);

        return fees;
    }
}
