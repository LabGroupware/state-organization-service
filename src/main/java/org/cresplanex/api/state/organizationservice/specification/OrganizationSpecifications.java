package org.cresplanex.api.state.organizationservice.specification;

import jakarta.persistence.criteria.Predicate;
import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.filter.organization.OwnerFilter;
import org.cresplanex.api.state.organizationservice.filter.organization.PlanFilter;
import org.cresplanex.api.state.organizationservice.filter.organization.UsersFilter;
import org.springframework.data.jpa.domain.Specification;

public class OrganizationSpecifications {

    public static Specification<OrganizationEntity> withPlanFilter(PlanFilter planFilter) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (planFilter != null && planFilter.isValid()) {
                if (planFilter.getPlans() != null && !planFilter.getPlans().isEmpty()) {
                    predicate = criteriaBuilder.and(predicate, root.get("plan").in(planFilter.getPlans()));
                }
            }
            return predicate;
        };
    }

    public static Specification<OrganizationEntity> withOwnerFilter(OwnerFilter ownerFilter) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (ownerFilter != null && ownerFilter.isValid()) {
                if (ownerFilter.getOwnerIds() != null && !ownerFilter.getOwnerIds().isEmpty()) {
                    predicate = criteriaBuilder.and(predicate, root.get("ownerId").in(ownerFilter.getOwnerIds()));
                }
            }
            return predicate;
        };
    }

    public static Specification<OrganizationEntity> withBelongUsersFilter(UsersFilter usersFilter) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (usersFilter != null && usersFilter.isValid()) {
                if (usersFilter.getUserIds() != null && !usersFilter.getUserIds().isEmpty()) {
                    if (!usersFilter.isAny()) {
                        // all
                        for (String userId : usersFilter.getUserIds()) {
                            predicate = criteriaBuilder.and(predicate, criteriaBuilder.isMember(userId, root.get("organizationUsers")));
                        }
                    } else {
                        // any
                        predicate = criteriaBuilder.and(predicate, root.get("organizationUsers").get("userId").in(usersFilter.getUserIds()));
                    }
                }
            }
            return predicate;
        };
    }
}
