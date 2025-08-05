package com.mentoring.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mentoring.entity.FaultEventEntity;


@Repository
public interface FaultEventRepository extends JpaRepository<FaultEventEntity, Long> {
	Optional<FaultEventEntity> findByCctvIdAndFaultType(Long cctvId, String faultType);
	void deleteByCctvIdAndFaultType(Long cctvId, String faultType);

}
