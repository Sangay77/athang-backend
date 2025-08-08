package com.bfs.rma.auth.service;

import com.bfs.rma.auth.dto.RegisterRequest;
import com.bfs.rma.auth.dto.AuthenticationRequest;
import com.bfs.rma.auth.dto.AuthenticationResponse;
import com.bfs.rma.auth.email.EmailService;
import com.bfs.rma.auth.email.EmailTemplateName;
import com.bfs.rma.auth.jwt.JwtService;
import com.bfs.rma.auth.model.AppUser;
import com.bfs.rma.auth.model.Role;
import com.bfs.rma.auth.token.Token;
import com.bfs.rma.auth.model.UserPrincipal;
import com.bfs.rma.auth.repository.RoleRepository;
import com.bfs.rma.auth.token.TokenRepository;
import com.bfs.rma.auth.repository.UserRepository;
import com.bfs.rma.auth.util.FileUploadUtil;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Set;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private EmailService emailService;

    @Value("${spring.security.mailing.frontend.activation-url}")
    private String activationUrl;

    @Value("${jwt.expiration}")
    private long JWT_EXPIRATION_TIME;

    public void register(RegisterRequest request) throws MessagingException, IOException {
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalArgumentException("Role not initialized"));

        AppUser user = AppUser.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(false)
                .roles(Set.of(userRole))
                .build();

        MultipartFile photo = request.getPhoto();
        if (photo != null && !photo.isEmpty()) {
            String fileName = StringUtils.cleanPath(photo.getOriginalFilename());
            user.setPhotos(fileName);
        }

        AppUser savedUser = userRepository.save(user);
//        var jwtToken=jwtService.generateToken(user);

        if (photo != null && !photo.isEmpty()) {
            String uploadDir = "user-photos/" + savedUser.getId();
            FileUploadUtil.cleanDir(uploadDir);
            FileUploadUtil.saveFile(uploadDir, savedUser.getPhotos(), photo);
        }



        sendValidationEmail(savedUser);
    }

    private void sendValidationEmail(AppUser user) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user);
        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newToken,
                "Account activation"
        );
    }

    private String generateAndSaveActivationToken(AppUser user) {
        String generatedToken = generateActivationCode(6);
        var token = Token.builder()
                .token(generatedToken)
                .createAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        tokenRepository.save(token);
        return generatedToken;
    }

    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        var claims = new HashMap<String, Object>();
        claims.put("fullName", userPrincipal.getFullname());
        var jwtToken = jwtService.generateToken(claims, userPrincipal, JWT_EXPIRATION_TIME);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }


    @Transactional
    public void activateAccount(String token) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("INVALID TOKEN"));

        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation token has expired. A new token has been sent to the same email address");
        }

        var user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setEnabled(true);
        userRepository.save(user);

        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
    }
}
