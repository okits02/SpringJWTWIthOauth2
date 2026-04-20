package com.okits02.SpringJWTWithOauth2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_user_name", columnList = "user_name"),
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_first_name", columnList = "first_name"),
                @Index(name = "idx_last_name", columnList = "last_name"),
                @Index(name = "idx_created_at", columnList = "created_at"),
                @Index(name = "idx_updated_at", columnList = "updated_at")
        }
)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    String id;

    @Column(name = "user_name")
    String userName;

    @Column(name = "password")
    String password;

    @Column(name = "email")
    String email;

    @Column(name = "first_name")
    String firstName;

    @Column(name = "last_name")
    String lastName;

    @Column(name = "dob")
    LocalDate dob;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_name"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role_name"})
    )
    @Builder.Default
    Set<Role> roles = new HashSet<>();
}
