package com.eventostec.api.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eventostec.api.domain.coupom.Coupom;

public interface CoupomRepository extends JpaRepository<Coupom, UUID> {

}
