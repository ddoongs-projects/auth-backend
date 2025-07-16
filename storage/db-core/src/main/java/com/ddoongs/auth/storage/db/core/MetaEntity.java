package com.ddoongs.auth.storage.db.core;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class MetaEntity {

  @Enumerated(EnumType.STRING)
  @Column(name = "entityStatus")
  private EntityStatus entityStatus = EntityStatus.ACTIVE;

  @Getter
  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @Getter
  @LastModifiedDate
  private LocalDateTime updatedAt;

  public void active() {
    this.entityStatus = EntityStatus.ACTIVE;
  }

  public void delete() {
    this.entityStatus = EntityStatus.DELETED;
  }

  public boolean isActive() {
    return this.entityStatus == EntityStatus.ACTIVE;
  }

  public boolean isDeleted() {
    return this.entityStatus == EntityStatus.DELETED;
  }
}
