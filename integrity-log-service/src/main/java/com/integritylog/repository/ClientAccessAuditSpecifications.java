package com.integritylog.repository;

import com.integritylog.domain.ClientAccessAudit;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class ClientAccessAuditSpecifications {

    private ClientAccessAuditSpecifications() {
    }

    public static Specification<ClientAccessAudit> matches(String actorId,
                                                          String resourceType,
                                                          String resourceId,
                                                          String action,
                                                          Instant from,
                                                          Instant to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (actorId != null && !actorId.isBlank()) {
                predicates.add(cb.equal(root.get("actorId"), actorId));
            }
            if (resourceType != null && !resourceType.isBlank()) {
                predicates.add(cb.equal(root.get("resourceType"), resourceType));
            }
            if (resourceId != null && !resourceId.isBlank()) {
                predicates.add(cb.equal(root.get("resourceId"), resourceId));
            }
            if (action != null && !action.isBlank()) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("accessedAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("accessedAt"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
