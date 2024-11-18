package org.cresplanex.api.state.organizationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.common.saga.local.organization.InvalidOrganizationPlanException;
import org.cresplanex.api.state.common.saga.local.organization.NotFoundOrganizationException;
import org.cresplanex.api.state.common.service.BaseService;
import org.cresplanex.api.state.organizationservice.constants.PlanType;
import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.entity.OrganizationUserEntity;
import org.cresplanex.api.state.organizationservice.exception.OrganizationNotFoundException;
import org.cresplanex.api.state.organizationservice.repository.OrganizationRepository;
import org.cresplanex.api.state.organizationservice.repository.OrganizationUserRepository;
import org.cresplanex.api.state.organizationservice.saga.model.organization.AddUsersOrganizationSaga;
import org.cresplanex.api.state.organizationservice.saga.model.organization.CreateOrganizationSaga;
import org.cresplanex.api.state.organizationservice.saga.state.organization.AddUsersOrganizationSagaState;
import org.cresplanex.api.state.organizationservice.saga.state.organization.CreateOrganizationSagaState;
import org.cresplanex.core.saga.orchestration.SagaInstanceFactory;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrganizationService extends BaseService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationUserRepository organizationUserRepository;
    private final SagaInstanceFactory sagaInstanceFactory;

    private final CreateOrganizationSaga createOrganizationSaga;
    private final AddUsersOrganizationSaga addUsersOrganizationSaga;

    @Transactional(readOnly = true)
    public OrganizationEntity findById(String organizationId) {
        return internalFindById(organizationId);
    }

    private OrganizationEntity internalFindById(String organizationId) {
        return organizationRepository.findById(organizationId).orElseThrow(() -> new OrganizationNotFoundException(
                OrganizationNotFoundException.FindType.BY_ID,
                organizationId
        ));
    }

    @Transactional(readOnly = true)
    public List<OrganizationEntity> get() {
        return organizationRepository.findAll();
    }

    @Transactional
    public String beginCreate(String operatorId, OrganizationEntity organization, List<OrganizationUserEntity> users) {
        CreateOrganizationSagaState.InitialData initialData = CreateOrganizationSagaState.InitialData.builder()
                .name(organization.getName())
                .plan(organization.getPlan())
                .users(users.stream().map(user -> CreateOrganizationSagaState.InitialData.User.builder()
                        .userId(user.getUserId())
                        .build())
                        .toList())
                .build();
        CreateOrganizationSagaState state = new CreateOrganizationSagaState();
        state.setInitialData(initialData);
        state.setOperatorId(operatorId);

        String jobId = getJobId();
        state.setJobId(jobId);

        // 一つ目がlocalStepであるため, 一つ目のLocal Exceptionが発生する場合,
        // ここで処理する必要がある.
        // ただし, rollbackExceptionに登録する必要がある.
        try {
            sagaInstanceFactory.create(createOrganizationSaga, state);
        } catch (InvalidOrganizationPlanException e) {
            // Jobで失敗イベント送信済みのため, ここでは何もしない
            log.debug("InvalidOrganizationPlanException: {}", e.getMessage());
            return jobId;
        }

        return jobId;
    }

    @Transactional
    public String beginAddUsers(String operatorId, String organizationId, List<OrganizationUserEntity> users) {
        AddUsersOrganizationSagaState.InitialData initialData = AddUsersOrganizationSagaState.InitialData.builder()
                .organizationId(organizationId)
                .users(users.stream().map(user -> AddUsersOrganizationSagaState.InitialData.User.builder()
                        .userId(user.getUserId())
                        .build())
                        .toList())
                .build();
        AddUsersOrganizationSagaState state = new AddUsersOrganizationSagaState();
        state.setInitialData(initialData);
        state.setOperatorId(operatorId);

        String jobId = getJobId();
        state.setJobId(jobId);

        try {
            sagaInstanceFactory.create(addUsersOrganizationSaga, state);
        } catch (NotFoundOrganizationException e) {
            // Jobで失敗イベント送信済みのため, ここでは何もしない
            log.debug("NotFoundOrganizationException: {}", e.getMessage());
            return jobId;
        }

        return jobId;
    }

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

    public OrganizationEntity
    createAndAddUsers(String operatorId, OrganizationEntity organization) {
        return organizationRepository.save(organization);
    }

    public List<OrganizationUserEntity> addUsers(String operatorId, String organizationId, List<String> userIds) {
        List<OrganizationUserEntity> users = userIds.stream()
                .map(userId -> {
                    OrganizationUserEntity user = new OrganizationUserEntity();
                    user.setOrganizationId(organizationId);
                    user.setUserId(userId);
                    return user;
                })
                .toList();
        return organizationUserRepository.saveAll(users);
    }

    public void undoCreate(String organizationId) {
        OrganizationEntity organization = organizationRepository.findByIdWithUsers(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(
                        OrganizationNotFoundException.FindType.BY_ID,
                        organizationId
                ));
        organizationRepository.delete(organization);
    }

    public void undoAddUsers(List<String> organizationUserIds) {
        organizationUserRepository.deleteAllById(organizationUserIds);
    }
}
