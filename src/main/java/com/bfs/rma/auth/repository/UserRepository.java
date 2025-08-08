package com.bfs.rma.auth.repository;

import com.bfs.rma.auth.model.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Integer> {

    @Query("SELECT u FROM AppUser u WHERE u.email=:email")
    Optional<AppUser> getUserByEmail(@Param("email") String email);

    Long countById(Integer id);

//    @Query("SELECT u FROM User u WHERE u.firstName LIKE %?1% OR u.lastName LIKE %?1% OR u.email LIKE %?1%")
    @Query("SELECT u FROM AppUser u WHERE CONCAT(u.id,' ',u.email,' ',u.firstName,' ',u.lastName, ' ') LIKE %?1%")
    Page<AppUser> search(String keyword, Pageable pageable);


    @Query("UPDATE AppUser u SET u.enabled=?2 WHERE u.id=?1")
    @Modifying
    void updateEnabledStatus(Integer id, boolean enabled);

}
