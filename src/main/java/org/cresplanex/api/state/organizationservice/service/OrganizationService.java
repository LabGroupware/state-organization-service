package org.cresplanex.api.state.organizationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.common.entity.ListEntityWithCount;
import org.cresplanex.api.state.common.enums.PaginationType;
import org.cresplanex.api.state.common.saga.local.LocalException;
import org.cresplanex.api.state.common.service.BaseService;
import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.entity.OrganizationUserEntity;
import org.cresplanex.api.state.organizationservice.enums.OrganizationSortType;
import org.cresplanex.api.state.organizationservice.exception.AlreadyExistOrganizationUserException;
import org.cresplanex.api.state.organizationservice.exception.NotFoundOrganizationUserException;
import org.cresplanex.api.state.organizationservice.exception.OrganizationNotFoundException;
import org.cresplanex.api.state.organizationservice.filter.organization.OwnerFilter;
import org.cresplanex.api.state.organizationservice.filter.organization.PlanFilter;
import org.cresplanex.api.state.organizationservice.filter.organization.UsersFilter;
import org.cresplanex.api.state.organizationservice.repository.OrganizationRepository;
import org.cresplanex.api.state.organizationservice.repository.OrganizationUserRepository;
import org.cresplanex.api.state.organizationservice.saga.model.organization.AddUsersOrganizationSaga;
import org.cresplanex.api.state.organizationservice.saga.model.organization.CreateOrganizationSaga;
import org.cresplanex.api.state.organizationservice.saga.state.organization.AddUsersOrganizationSagaState;
import org.cresplanex.api.state.organizationservice.saga.state.organization.CreateOrganizationSagaState;
import org.cresplanex.api.state.organizationservice.specification.OrganizationSpecifications;
import org.cresplanex.core.saga.orchestration.SagaInstanceFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
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
    public ListEntityWithCount<OrganizationEntity> get(
            PaginationType paginationType,
            int limit,
            int offset,
            String cursor,
            OrganizationSortType sortType,
            boolean withCount,
            PlanFilter planFilter,
            OwnerFilter ownerFilter,
            UsersFilter usersFilter
    ) {
        Specification<OrganizationEntity> spec = Specification.where(
                OrganizationSpecifications.withPlanFilter(planFilter)
                        .and(OrganizationSpecifications.withOwnerFilter(ownerFilter))
                        .and(OrganizationSpecifications.withBelongUsersFilter(usersFilter)));

        List<OrganizationEntity> data = switch (paginationType) {
            case OFFSET ->
                    organizationRepository.findListWithOffsetPagination(spec, sortType, PageRequest.of(offset / limit, limit));
            case CURSOR -> organizationRepository.findList(spec, sortType); // TODO: Implement cursor pagination
            default -> organizationRepository.findList(spec, sortType);
        };

        int count = 0;
        if (withCount){
            count = organizationRepository.countList(spec);
        }
        return new ListEntityWithCount<>(
                data,
                count
        );
    }

    @Transactional(readOnly = true)
    public List<OrganizationEntity> getByOrganizationIds(
            List<String> organizationIds,
            OrganizationSortType sortType
    ) {
        return organizationRepository.findListByOrganizationIds(organizationIds, sortType);
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
        } catch (LocalException e) {
            // Jobで失敗イベント送信済みのため, ここでは何もしない
            log.debug("LocalException: {}", e.getMessage());
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
        } catch (LocalException e) {
            // Jobで失敗イベント送信済みのため, ここでは何もしない
            log.debug("LocalException: {}", e.getMessage());
            return jobId;
        }

        return jobId;
    }

    public OrganizationEntity createAndAddUsers(String operatorId, OrganizationEntity organization, List<OrganizationUserEntity> users) {
        organization = organizationRepository.save(organization);
        OrganizationEntity finalOrganization = organization;
        users = users.stream()
                .peek(user -> user.setOrganization(finalOrganization))
                .toList();
        organizationUserRepository.saveAll(users);
        organization.setOrganizationUsers(users);
        return organization;
    }

    public List<OrganizationUserEntity> addUsers(String operatorId, String organizationId, List<OrganizationUserEntity> users) {
        List<OrganizationUserEntity> existUsers = organizationUserRepository.
                findAllByOrganizationIdAndUserIds(organizationId, users.stream()
                        .map(OrganizationUserEntity::getUserId)
                        .toList());
        if (!existUsers.isEmpty()) {
            List<String> existUserIds = existUsers.stream()
                    .map(OrganizationUserEntity::getUserId)
                    .toList();
            throw new AlreadyExistOrganizationUserException(existUserIds);
        }
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

    public void validateOrganizationsAndOrganizationUsers(String organizationId, List<String> userIds) {
        organizationRepository.countByOrganizationIdIn(List.of(organizationId))
                .ifPresent(count -> {
                    if (count != 1) {
                        throw new org.cresplanex.api.state.organizationservice.exception.NotFoundOrganizationException(
                                List.of(organizationId)
                        );
                    }
                });
        List<String> existUserIds = organizationUserRepository.
                findAllByOrganizationIdAndUserIds(organizationId, userIds)
                .stream()
                .map(OrganizationUserEntity::getUserId)
                .toList();
        if (existUserIds.size() != userIds.size()) {
            List<String> notExistUserIds = userIds.stream()
                    .filter(userId -> !existUserIds.contains(userId))
                    .toList();
            throw new NotFoundOrganizationUserException(organizationId, notExistUserIds);
        }
    }
}
