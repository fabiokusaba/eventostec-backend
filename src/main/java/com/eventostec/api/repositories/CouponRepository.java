package com.eventostec.api.repositories;

import com.eventostec.api.domain.coupon.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    // Declarando um novo metodo no nosso repository para fazer a busca por eventId e Valid, ou seja, todos os cupons
    // que ainda estão válidos.
    List<Coupon> findByEventIdAndValidAfter(UUID eventId, Date validAfter);
}
