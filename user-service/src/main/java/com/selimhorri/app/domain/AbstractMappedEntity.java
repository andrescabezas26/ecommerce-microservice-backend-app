package com.selimhorri.app.domain;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public abstract class AbstractMappedEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;
	
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	public void prePersist() {
		this.createdAt = Instant.now();
		this.updatedAt = Instant.now();
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = Instant.now();
	}
}










