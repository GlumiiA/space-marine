package ru.itmo.is.space_marine_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.is.space_marine_backend.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA автоматически сгенерировал всю работу с таблицей app_user
    Optional<User> findByUsername(String username);
    // вместо select * from app_user u where u.username = 'ivan' limit 1;
}
