package com.mentoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mentoring.entity.FaultEventEntity;

public interface FaultEventRepository extends JpaRepository<FaultEventEntity, Long> {}
