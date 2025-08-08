package com.bfs.rma.fee;

import com.bfs.rma.auth.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeeGenerationRepository extends JpaRepository<FeeGeneration, Long> {
    List<FeeGeneration> findByUser(AppUser user);
}
