package com.mockproject.group3.repository;

import com.mockproject.group3.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    Optional<Contact> findByIdAndIsDeletedFalse(Long id);

    Page<Contact> findByIsDeletedFalse(Pageable pageable);

    @Query("SELECT c FROM Contact c WHERE c.isDeleted = false " +
           "AND (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR c.phonePrimary LIKE CONCAT('%', :search, '%') " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Contact> searchContacts(@Param("search") String search, Pageable pageable);

    @Query("SELECT c FROM Contact c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR c.phonePrimary LIKE CONCAT('%', :search, '%') " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Contact> searchContactsIncludeDeleted(@Param("search") String search, Pageable pageable);
}

