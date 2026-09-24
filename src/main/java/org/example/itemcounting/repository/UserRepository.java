package org.example.itemcounting.repository;

import org.example.itemcounting.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
