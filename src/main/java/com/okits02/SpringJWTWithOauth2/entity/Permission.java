package com.okits02.SpringJWTWithOauth2.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Permission {
    @Id
    String name;

    String description;
}
