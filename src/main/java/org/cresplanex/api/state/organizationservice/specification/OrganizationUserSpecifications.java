package org.cresplanex.api.state.organizationservice.specification;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.organizationservice.entity.OrganizationUserEntity;
import org.hibernate.type.descriptor.java.StringJavaType;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OrganizationUserSpecifications {

    public static Specification<OrganizationUserEntity> fetchOrganization() {
        return (root, query, criteriaBuilder) -> {
            if (query == null) {
                return null;
            }
            if (Long.class != query.getResultType()) {
                root.fetch("organization", JoinType.LEFT);
                query.distinct(true);
                return null;
            }

            return null;
        };
    }

    public static Specification<OrganizationUserEntity> whereOrganizationId(String organizationId) {
        String newOrganizationId = new StringJavaType().wrap(organizationId, null);
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("organizationId"), newOrganizationId));
            return predicate;
        };
    }

    public static Specification<OrganizationUserEntity> whereOrganizationIds(Iterable<String> organizationIds) {
        List<String> organizationIdList = new ArrayList<>();
        organizationIds.forEach(organizationId -> {
            organizationIdList.add(new StringJavaType().wrap(organizationId, null));
        });

        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            predicate = criteriaBuilder.and(predicate, root.get("organizationId").in(organizationIdList));
            return predicate;
        };
    }

    public static Specification<OrganizationUserEntity> whereUserId(String userId) {
        String newUserId = new StringJavaType().wrap(userId, null);
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("userId"), newUserId));
            return predicate;
        };
    }

    public static Specification<OrganizationUserEntity> whereUserIds(Iterable<String> userIds) {
        List<String> userIdList = new ArrayList<>();
        userIds.forEach(userId -> {
            userIdList.add(new StringJavaType().wrap(userId, null));
        });

        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            predicate = criteriaBuilder.and(predicate, root.get("userId").in(userIds));
            return predicate;
        };
    }
}
