package com.integritylog.repository;

import com.integritylog.domain.AuditEvent;
import com.integritylog.service.AuditEventQuery;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
public final class AuditEventSpecifications {
    private AuditEventSpecifications() {
    }
    public static Specification<AuditEvent> matches(AuditEventQuery query) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query.actorId() != null && !query.actorId().isBlank()) {
                predicates.add(cb.equal(root.get("actorId"), query.actorId()));
            }
            if (query.resourceType() != null && !query.resourceType().isBlank()) {
                predicates.add(cb.equal(root.get("resourceType"), query.resourceType()));
            }
            if (query.resourceId() != null && !query.resourceId().isBlank()) {
                predicates.add(cb.equal(root.get("resourceId"), query.resourceId()));
            }
            if (query.eventType() != null && !query.eventType().isBlank()) {
                predicates.add(cb.equal(root.get("eventType"), query.eventType()));
            }
            if (query.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), query.from()));
            }
            if (query.to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), query.to()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

