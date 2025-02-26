package com.example.blps.repository.auth;

import com.example.blps.model.authEntity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Найти пользователя по имени пользователя
    Optional<User> findByUsername(String username);

    // Найти всех администраторов
    List<User> findByRole(String role);
    // Найти всех пользователей с ролью ADMIN
    @Query("FROM User WHERE role = 'ADMIN'")
    List<User> findAllAdmins();

    // Удалить пользователя по ID (этот метод уже предоставляется JpaRepository)
    void deleteById(Long id);

}
