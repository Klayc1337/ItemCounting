package org.example.itemcounting.repository;

import org.example.itemcounting.entity.UserCoeff;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCoeffRepository extends JpaRepository<UserCoeff, Long> {
    Optional<UserCoeff> findByGroupName(String groupName);
}