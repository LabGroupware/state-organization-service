package org.cresplanex.api.state.organizationservice.specification;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.filter.organization.OwnerFilter;
import org.cresplanex.api.state.organizationservice.filter.organization.PlanFilter;
import org.cresplanex.api.state.organizationservice.filter.organization.UsersFilter;
import org.hibernate.type.descriptor.java.StringJavaType;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class OrganizationSpecifications {

    public static Specification<OrganizationEntity> whereOrganizationIds(Iterable<String> organizationIds) {
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

    public static Specification<OrganizationEntity> fetchOrganizationUsers() {
        return (root, query, criteriaBuilder) -> {
            if (query == null) {
                return null;
            }
            if (Long.class != query.getResultType()) {
                root.join("organizationUsers", JoinType.LEFT);
                query.distinct(true);
                return null;
            }

            return null;
        };
    }

    public static Specification<OrganizationEntity> withPlanFilter(PlanFilter planFilter) {
        List<String> planList = new ArrayList<>();
        if (planFilter != null && planFilter.isValid()) {
            planFilter.getPlans().forEach(plan -> {
                planList.add(new StringJavaType().wrap(plan, null));
            });
        }

        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (planFilter != null && planFilter.isValid()) {
                return criteriaBuilder.and(predicate, root.get("plan").in(planList));
            }
            return predicate;
        };
    }

    public static Specification<OrganizationEntity> withOwnerFilter(OwnerFilter ownerFilter) {
        List<String> ownerList = new ArrayList<>();
        if (ownerFilter != null && ownerFilter.isValid()) {
            ownerFilter.getOwnerIds().forEach(owner -> {
                ownerList.add(new StringJavaType().wrap(owner, null));
            });
        }

        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (ownerFilter != null && ownerFilter.isValid()) {
                predicate = criteriaBuilder.and(predicate, root.get("ownerId").in(ownerList));
            }
            return predicate;
        };
    }

    public static Specification<OrganizationEntity> withBelongUsersFilter(UsersFilter usersFilter) {
        List<String> userIdList = new ArrayList<>();
        if (usersFilter != null && usersFilter.isValid()) {
            usersFilter.getUserIds().forEach(userId -> {
                userIdList.add(new StringJavaType().wrap(userId, null));
            });
        }

        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (usersFilter != null && usersFilter.isValid()) {
                if (usersFilter.getUserIds() != null && !usersFilter.getUserIds().isEmpty()) {
                    if (!usersFilter.isAny()) {
                        // all
                        for (String userId : userIdList) {
                            predicate = criteriaBuilder.and(predicate, criteriaBuilder.isMember(userId, root.get("organizationUsers")));
                        }
                    } else {
                        // any
                        predicate = criteriaBuilder.and(predicate, root.get("organizationUsers").get("userId").in(userIdList));
                    }
                }
            }
            return predicate;
        };
    }
}
