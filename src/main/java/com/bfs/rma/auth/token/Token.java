package com.bfs.rma.auth.token;

import com.bfs.rma.auth.model.AppUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer token_id;
    private String token;
    private LocalDateTime createAt;
    private LocalDateTime expiresAt;
    private LocalDateTime validatedAt;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;
}
