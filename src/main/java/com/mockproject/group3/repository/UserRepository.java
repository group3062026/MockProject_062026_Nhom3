package com.mockproject.group3.repository;

import com.mockproject.group3.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByIdAndIsDeletedFalse(Long id);

    boolean existsByEmail(String email);

    boolean existsByEmployeeCode(String employeeCode);

    Page<User> findByIsDeletedFalse(Pageable pageable);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN u.facilities uf " +
           "WHERE u.isDeleted = false " +
           "AND (:roleId IS NULL OR u.role.id = :roleId) " +
           "AND (:status IS NULL OR u.status = :status) " +
           "AND (:facilityId IS NULL OR uf.facility.id = :facilityId) " +
           "AND (:search IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.employeeCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsers(@Param("roleId") Long roleId, 
                           @Param("status") String status, 
                           @Param("facilityId") Long facilityId, 
                           @Param("search") String search, 
                           Pageable pageable);
}


