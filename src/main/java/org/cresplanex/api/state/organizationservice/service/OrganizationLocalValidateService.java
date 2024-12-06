package org.cresplanex.api.state.organizationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.common.saga.local.organization.InvalidOrganizationPlanException;
import org.cresplanex.api.state.common.saga.local.organization.NotAllowedOrganizationUsersContainOwnerException;
import org.cresplanex.api.state.common.saga.local.organization.NotFoundOrganizationException;
import org.cresplanex.api.state.common.saga.local.organization.WillAddedOrganizationUserDuplicatedException;
import org.cresplanex.api.state.common.service.BaseService;
import org.cresplanex.api.state.organizationservice.constants.PlanType;
import org.cresplanex.api.state.organizationservice.repository.OrganizationRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrganizationLocalValidateService extends BaseService {

    private final OrganizationRepository organizationRepository;

    public void validateCreatedOrganization(String name, String plan, String ownerId, List<String> userIds)
            throws InvalidOrganizationPlanException {
        if (!Arrays.asList(PlanType.ALL).contains(plan)) {
            throw new InvalidOrganizationPlanException(List.of(plan));
        }
        
        if (userIds.size() != userIds.stream().distinct().count()) {
            throw new WillAddedOrganizationUserDuplicatedException(userIds);
        }

        if (userIds.contains(ownerId)) {
            throw new NotAllowedOrganizationUsersContainOwnerException(userIds, ownerId);
        }
    }

    public void validateOrganizations(List<String> organizationIds, List<String> userIds)
            throws NotFoundOrganizationException {
        organizationRepository.countByOrganizationIdIn(organizationIds)
                .ifPresent(count -> {
                    if (count != organizationIds.size()) {
                        throw new NotFoundOrganizationException(organizationIds);
                    }
                });

        if (userIds.size() != userIds.stream().distinct().count()) {
            throw new WillAddedOrganizationUserDuplicatedException(userIds);
        }
    }
}
