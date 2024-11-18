package org.cresplanex.api.state.organizationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.common.saga.local.LocalException;
import org.cresplanex.api.state.common.saga.local.organization.InvalidOrganizationPlanException;
import org.cresplanex.api.state.common.saga.local.organization.NotFoundOrganizationException;
import org.cresplanex.api.state.common.service.BaseService;
import org.cresplanex.api.state.organizationservice.constants.PlanType;
import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.entity.OrganizationUserEntity;
import org.cresplanex.api.state.organizationservice.exception.AlreadyExistOrganizationUserException;
import org.cresplanex.api.state.organizationservice.exception.NotFoundOrganizationUserException;
import org.cresplanex.api.state.organizationservice.exception.OrganizationNotFoundException;
import org.cresplanex.api.state.organizationservice.repository.OrganizationRepository;
import org.cresplanex.api.state.organizationservice.repository.OrganizationUserRepository;
import org.cresplanex.api.state.organizationservice.saga.model.organization.AddUsersOrganizationSaga;
import org.cresplanex.api.state.organizationservice.saga.model.organization.CreateOrganizationSaga;
import org.cresplanex.api.state.organizationservice.saga.state.organization.AddUsersOrganizationSagaState;
import org.cresplanex.api.state.organizationservice.saga.state.organization.CreateOrganizationSagaState;
import org.cresplanex.core.saga.orchestration.SagaInstanceFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrganizationLocalValidateService extends BaseService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationUserRepository organizationUserRepository;

    public void validateCreatedOrganization(String name, String plan)
            throws InvalidOrganizationPlanException {
        if (!Arrays.asList(PlanType.ALL).contains(plan)) {
            throw new InvalidOrganizationPlanException(List.of(plan));
        }
    }

    public void validateOrganizations(List<String> organizationIds)
            throws NotFoundOrganizationException {
        organizationRepository.countByOrganizationIdIn(organizationIds)
                .ifPresent(count -> {
                    if (count != organizationIds.size()) {
                        throw new NotFoundOrganizationException(organizationIds);
                    }
                });
    }
}
