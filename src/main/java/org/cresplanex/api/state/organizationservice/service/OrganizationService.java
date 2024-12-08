package org.cresplanex.api.state.organizationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.common.entity.ListEntityWithCount;
import org.cresplanex.api.state.common.enums.PaginationType;
import org.cresplanex.api.state.common.saga.local.LocalException;
import org.cresplanex.api.state.common.service.BaseService;
import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.entity.OrganizationUserEntity;
import org.cresplanex.api.state.organizationservice.enums.OrganizationOnUserSortType;
import org.cresplanex.api.state.organizationservice.enums.OrganizationSortType;
import org.cresplanex.api.state.organizationservice.enums.OrganizationWithUsersSortType;
import org.cresplanex.api.state.organizationservice.enums.UserOnOrganizationSortType;
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
import org.cresplanex.api.state.organizationservice.specification.OrganizationUserSpecifications;
import org.cresplanex.core.saga.orchestration.SagaInstanceFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    @Transactional(readOnly = true)
    public OrganizationEntity findByIdWithUsers(String organizationId) {
        return organizationRepository.findByIdWithUsers(organizationId).orElseThrow(() -> new OrganizationNotFoundException(
                OrganizationNotFoundException.FindType.BY_ID,
                organizationId
        ));
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

        Sort sort = createSort(sortType);

        Pageable pageable = switch (paginationType) {
            case OFFSET -> PageRequest.of(offset / limit, limit, sort);
            case CURSOR -> PageRequest.of(0, limit, sort); // TODO: Implement cursor pagination
            default -> Pageable.unpaged(sort);
        };

        Page<OrganizationEntity> data = organizationRepository.findAll(spec, pageable);

        int count = 0;
        if (withCount){
            count = (int)data.getTotalElements();
        }
        return new ListEntityWithCount<>(
                data.getContent(),
                count
        );
    }

    @Transactional(readOnly = true)
    public ListEntityWithCount<OrganizationEntity> getWithUsers(
            PaginationType paginationType,
            int limit,
            int offset,
            String cursor,
            OrganizationWithUsersSortType sortType,
            boolean withCount,
            PlanFilter planFilter,
            OwnerFilter ownerFilter,
            UsersFilter usersFilter
    ) {
        Specification<OrganizationEntity> spec = Specification.where(
                OrganizationSpecifications.withPlanFilter(planFilter)
                        .and(OrganizationSpecifications.withOwnerFilter(ownerFilter))
                        .and(OrganizationSpecifications.withBelongUsersFilter(usersFilter))
                        .and(OrganizationSpecifications.fetchOrganizationUsers()));

        Sort sort = createSort(sortType);

        Pageable pageable = switch (paginationType) {
            case OFFSET -> PageRequest.of(offset / limit, limit, sort);
            case CURSOR -> PageRequest.of(0, limit, sort); // TODO: Implement cursor pagination
            default -> Pageable.unpaged(sort);
        };

        Page<OrganizationEntity> data = organizationRepository.findAll(spec, pageable);

        int count = 0;
        if (withCount){
            count = (int)data.getTotalElements();
        }
        return new ListEntityWithCount<>(
                data.getContent(),
                count
        );
    }

    @Transactional(readOnly = true)
    public ListEntityWithCount<OrganizationUserEntity> getUsersOnOrganization(
            String organizationId,
            PaginationType paginationType,
            int limit,
            int offset,
            String cursor,
            UserOnOrganizationSortType sortType,
            boolean withCount
    ) {
        Specification<OrganizationUserEntity> spec = Specification.where(
                OrganizationUserSpecifications.whereOrganizationId(organizationId));

        Sort sort = createSort(sortType);

        Pageable pageable = switch (paginationType) {
            case OFFSET -> PageRequest.of(offset / limit, limit, sort);
            case CURSOR -> PageRequest.of(0, limit, sort); // TODO: Implement cursor pagination
            default -> Pageable.unpaged(sort);
        };

        Page<OrganizationUserEntity> data = organizationUserRepository.findAll(spec, pageable);

        int count = 0;
        if (withCount){
            count = (int)data.getTotalElements();
        }
        return new ListEntityWithCount<>(
                data.getContent(),
                count
        );
    }

    @Transactional(readOnly = true)
    public ListEntityWithCount<OrganizationUserEntity> getOrganizationsOnUser(
            String userId,
            PaginationType paginationType,
            int limit,
            int offset,
            String cursor,
            OrganizationOnUserSortType sortType,
            boolean withCount
    ) {
        Specification<OrganizationUserEntity> spec = Specification.where(
                OrganizationUserSpecifications.whereUserId(userId)
                        .and(OrganizationUserSpecifications.fetchOrganization()));

        Sort sort = createSort(sortType);

        Pageable pageable = switch (paginationType) {
            case OFFSET -> PageRequest.of(offset / limit, limit, sort);
            case CURSOR -> PageRequest.of(0, limit, sort); // TODO: Implement cursor pagination
            default -> Pageable.unpaged(sort);
        };

        Page<OrganizationUserEntity> data = organizationUserRepository.findAll(spec, pageable);

        int count = 0;
        if (withCount){
            count = (int)data.getTotalElements();
        }
        return new ListEntityWithCount<>(
                data.getContent(),
                count
        );
    }

    @Transactional(readOnly = true)
    public List<OrganizationEntity> getByOrganizationIds(
            List<String> organizationIds,
            OrganizationSortType sortType
    ) {
        Specification<OrganizationEntity> spec = Specification.where(
                OrganizationSpecifications.whereOrganizationIds(organizationIds));

        return organizationRepository.findAll(spec, createSort(sortType));
    }

    @Transactional(readOnly = true)
    public List<OrganizationEntity> getByOrganizationIdsWithUsers(
            List<String> organizationIds,
            OrganizationWithUsersSortType sortType
    ) {
        Specification<OrganizationEntity> spec = Specification.where(
                OrganizationSpecifications.whereOrganizationIds(organizationIds)
                        .and(OrganizationSpecifications.fetchOrganizationUsers()));

        return organizationRepository.findAll(spec, createSort(sortType));
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
        OrganizationEntity organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(
                        OrganizationNotFoundException.FindType.BY_ID,
                        organizationId
                ));
        users = users.stream()
                .peek(user -> user.setOrganization(organization))
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

    private Sort createSort(OrganizationSortType sortType) {
        return switch (sortType) {
            case CREATED_AT_ASC -> Sort.by(Sort.Order.asc("createdAt"));
            case CREATED_AT_DESC -> Sort.by(Sort.Order.desc("createdAt"));
            case NAME_ASC -> Sort.by(Sort.Order.asc("name"), Sort.Order.desc("createdAt"));
            case NAME_DESC -> Sort.by(Sort.Order.desc("name"), Sort.Order.desc("createdAt"));
        };
    }

    private Sort createSort(OrganizationWithUsersSortType sortType) {
        return switch (sortType) {
            case CREATED_AT_ASC -> Sort.by(Sort.Order.asc("createdAt"));
            case CREATED_AT_DESC -> Sort.by(Sort.Order.desc("createdAt"));
            case NAME_ASC -> Sort.by(Sort.Order.asc("name"), Sort.Order.desc("createdAt"));
            case NAME_DESC -> Sort.by(Sort.Order.desc("name"), Sort.Order.desc("createdAt"));
        };
    }

    private Sort createSort(UserOnOrganizationSortType sortType) {
        return switch (sortType) {
            case ADD_AT_ASC -> Sort.by(Sort.Order.asc("createdAt"));
            case ADD_AT_DESC -> Sort.by(Sort.Order.desc("createdAt"));
        };
    }

    private Sort createSort(OrganizationOnUserSortType sortType) {
        return switch (sortType) {
            case ADD_AT_ASC -> Sort.by(Sort.Order.asc("createdAt"));
            case ADD_AT_DESC -> Sort.by(Sort.Order.desc("createdAt"));
            case NAME_ASC -> Sort.by(Sort.Order.asc("organization.name"), Sort.Order.desc("createdAt"));
            case NAME_DESC -> Sort.by(Sort.Order.desc("organization.name"), Sort.Order.desc("createdAt"));
            case CREATED_AT_ASC -> Sort.by(Sort.Order.asc("organization.createdAt"), Sort.Order.desc("createdAt"));
            case CREATED_AT_DESC -> Sort.by(Sort.Order.desc("organization.createdAt"), Sort.Order.desc("createdAt"));
        };
    }
}
