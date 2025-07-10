package com.ddoongs.auth.storage.db.core;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseEntity extends MetaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  protected BaseEntity() {}

  protected BaseEntity(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }
}
