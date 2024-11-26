package org.cresplanex.api.state.organizationservice.handler;

import build.buf.gen.cresplanex.nova.v1.Count;
import build.buf.gen.cresplanex.nova.v1.SortOrder;
import build.buf.gen.organization.v1.*;
import org.cresplanex.api.state.common.entity.ListEntityWithCount;
import org.cresplanex.api.state.common.enums.PaginationType;
import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.entity.OrganizationUserEntity;
import org.cresplanex.api.state.organizationservice.enums.OrganizationOnUserSortType;
import org.cresplanex.api.state.organizationservice.enums.OrganizationSortType;
import org.cresplanex.api.state.organizationservice.enums.OrganizationWithUsersSortType;
import org.cresplanex.api.state.organizationservice.enums.UserOnOrganizationSortType;
import org.cresplanex.api.state.organizationservice.filter.organization.OwnerFilter;
import org.cresplanex.api.state.organizationservice.filter.organization.PlanFilter;
import org.cresplanex.api.state.organizationservice.filter.organization.UsersFilter;
import org.cresplanex.api.state.organizationservice.mapper.proto.ProtoMapper;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.cresplanex.api.state.organizationservice.service.OrganizationService;

import java.util.List;

@RequiredArgsConstructor
@GrpcService
public class OrganizationServiceHandler extends OrganizationServiceGrpc.OrganizationServiceImplBase {

    private final OrganizationService organizationService;

    @Override
    public void findOrganization(FindOrganizationRequest request, StreamObserver<FindOrganizationResponse> responseObserver) {
        OrganizationEntity organization = organizationService.findById(request.getOrganizationId());

        Organization organizationProto = ProtoMapper.convert(organization);
        FindOrganizationResponse response = FindOrganizationResponse.newBuilder()
                .setOrganization(organizationProto)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void findOrganizationWithUsers(FindOrganizationWithUsersRequest request, StreamObserver<FindOrganizationWithUsersResponse> responseObserver) {
        OrganizationEntity organization = organizationService.findByIdWithUsers(request.getOrganizationId());

        OrganizationWithUsers organizationProto = ProtoMapper.convertWithUsers(organization);
        FindOrganizationWithUsersResponse response = FindOrganizationWithUsersResponse.newBuilder()
                .setOrganization(organizationProto)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getOrganizations(GetOrganizationsRequest request, StreamObserver<GetOrganizationsResponse> responseObserver) {
        OrganizationSortType sortType = switch (request.getSort().getOrderField()) {
            case ORGANIZATION_ORDER_FIELD_NAME -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    OrganizationSortType.NAME_ASC : OrganizationSortType.NAME_DESC;
            default -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    OrganizationSortType.CREATED_AT_ASC : OrganizationSortType.CREATED_AT_DESC;
        };
        PaginationType paginationType;
        switch (request.getPagination().getType()) {
            case PAGINATION_TYPE_CURSOR -> paginationType = PaginationType.CURSOR;
            case PAGINATION_TYPE_OFFSET -> paginationType = PaginationType.OFFSET;
            default -> paginationType = PaginationType.NONE;
        }

        PlanFilter planFilter = new PlanFilter(
                request.getFilterPlan().getHasValue(), request.getFilterPlan().getPlansList()
        );

        OwnerFilter ownerFilter = new OwnerFilter(
                request.getFilterOwner().getHasValue(), request.getFilterOwner().getOwnerIdsList()
        );

        UsersFilter usersFilter = new UsersFilter(
                request.getFilterUser().getHasValue(), request.getFilterUser().getAny(), request.getFilterUser().getUserIdsList()
        );

        ListEntityWithCount<OrganizationEntity> organizations = organizationService.get(
                paginationType, request.getPagination().getLimit(), request.getPagination().getOffset(),
                request.getPagination().getCursor(), sortType, request.getWithCount(), planFilter, ownerFilter, usersFilter);

        List<Organization> organizationProtos = organizations.getData().stream()
                .map(ProtoMapper::convert).toList();
        GetOrganizationsResponse response = GetOrganizationsResponse.newBuilder()
                .addAllOrganizations(organizationProtos)
                .setCount(
                        Count.newBuilder().setIsValid(request.getWithCount())
                                .setCount(organizations.getCount()).build()
                )
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getOrganizationsWithUsers(GetOrganizationsWithUsersRequest request, StreamObserver<GetOrganizationsWithUsersResponse> responseObserver) {
        OrganizationWithUsersSortType sortType = switch (request.getSort().getOrderField()) {
            case ORGANIZATION_WITH_USERS_ORDER_FIELD_NAME -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    OrganizationWithUsersSortType.NAME_ASC : OrganizationWithUsersSortType.NAME_DESC;
            default -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    OrganizationWithUsersSortType.CREATED_AT_ASC : OrganizationWithUsersSortType.CREATED_AT_DESC;
        };
        PaginationType paginationType;
        switch (request.getPagination().getType()) {
            case PAGINATION_TYPE_CURSOR -> paginationType = PaginationType.CURSOR;
            case PAGINATION_TYPE_OFFSET -> paginationType = PaginationType.OFFSET;
            default -> paginationType = PaginationType.NONE;
        }

        PlanFilter planFilter = new PlanFilter(
                request.getFilterPlan().getHasValue(), request.getFilterPlan().getPlansList()
        );

        OwnerFilter ownerFilter = new OwnerFilter(
                request.getFilterOwner().getHasValue(), request.getFilterOwner().getOwnerIdsList()
        );

        UsersFilter usersFilter = new UsersFilter(
                request.getFilterUser().getHasValue(), request.getFilterUser().getAny(), request.getFilterUser().getUserIdsList()
        );

        ListEntityWithCount<OrganizationEntity> organizations = organizationService.getWithUsers(
                paginationType, request.getPagination().getLimit(), request.getPagination().getOffset(),
                request.getPagination().getCursor(), sortType, request.getWithCount(), planFilter, ownerFilter, usersFilter);

        List<OrganizationWithUsers> organizationProtos = organizations.getData().stream()
                .map(ProtoMapper::convertWithUsers).toList();
        GetOrganizationsWithUsersResponse response = GetOrganizationsWithUsersResponse.newBuilder()
                .addAllOrganizations(organizationProtos)
                .setCount(
                        Count.newBuilder().setIsValid(request.getWithCount())
                                .setCount(organizations.getCount()).build()
                )
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getPluralOrganizations(GetPluralOrganizationsRequest request, StreamObserver<GetPluralOrganizationsResponse> responseObserver) {
        OrganizationSortType sortType = switch (request.getSort().getOrderField()) {
            case ORGANIZATION_ORDER_FIELD_NAME -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    OrganizationSortType.NAME_ASC : OrganizationSortType.NAME_DESC;
            default -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    OrganizationSortType.CREATED_AT_ASC : OrganizationSortType.CREATED_AT_DESC;
        };
        List<Organization> organizationProtos = this.organizationService.getByOrganizationIds(
                        request.getOrganizationIdsList(), sortType).stream()
                .map(ProtoMapper::convert).toList();
        GetPluralOrganizationsResponse response = GetPluralOrganizationsResponse.newBuilder()
                .addAllOrganizations(organizationProtos)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    @Override
    public void getPluralOrganizationsWithUsers(GetPluralOrganizationsWithUsersRequest request, StreamObserver<GetPluralOrganizationsWithUsersResponse> responseObserver) {
        OrganizationWithUsersSortType sortType = switch (request.getSort().getOrderField()) {
            case ORGANIZATION_WITH_USERS_ORDER_FIELD_NAME -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    OrganizationWithUsersSortType.NAME_ASC : OrganizationWithUsersSortType.NAME_DESC;
            default -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    OrganizationWithUsersSortType.CREATED_AT_ASC : OrganizationWithUsersSortType.CREATED_AT_DESC;
        };
        List<OrganizationWithUsers> organizationProtos = this.organizationService.getByOrganizationIdsWithUsers(
                        request.getOrganizationIdsList(), sortType).stream()
                .map(ProtoMapper::convertWithUsers).toList();
        GetPluralOrganizationsWithUsersResponse response = GetPluralOrganizationsWithUsersResponse.newBuilder()
                .addAllOrganizations(organizationProtos)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getUsersOnOrganization(GetUsersOnOrganizationRequest request, StreamObserver<GetUsersOnOrganizationResponse> responseObserver) {
        UserOnOrganizationSortType sortType = switch (request.getSort().getOrderField()) {
            default -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    UserOnOrganizationSortType.ADD_AT_ASC : UserOnOrganizationSortType.ADD_AT_DESC;
        };
        PaginationType paginationType;
        switch (request.getPagination().getType()) {
            case PAGINATION_TYPE_CURSOR -> paginationType = PaginationType.CURSOR;
            case PAGINATION_TYPE_OFFSET -> paginationType = PaginationType.OFFSET;
            default -> paginationType = PaginationType.NONE;
        }

        ListEntityWithCount<OrganizationUserEntity> organizations = organizationService.getUsersOnOrganization(
                request.getOrganizationId(), paginationType, request.getPagination().getLimit(), request.getPagination().getOffset(),
                request.getPagination().getCursor(), sortType, request.getWithCount());

        List<UserOnOrganization> userOnOrganizations = organizations.getData().stream()
                .map(ProtoMapper::convert).toList();

        GetUsersOnOrganizationResponse response = GetUsersOnOrganizationResponse.newBuilder()
                .addAllUsers(userOnOrganizations)
                .setCount(
                        Count.newBuilder().setIsValid(request.getWithCount())
                                .setCount(organizations.getCount()).build()
                )
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getOrganizationsOnUser(GetOrganizationsOnUserRequest request, StreamObserver<GetOrganizationsOnUserResponse> responseObserver) {

        OrganizationOnUserSortType sortType = switch (request.getSort().getOrderField()) {
            case ORGANIZATION_ON_USER_ORDER_FIELD_CREATE -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    OrganizationOnUserSortType.CREATED_AT_ASC : OrganizationOnUserSortType.CREATED_AT_DESC;
            case ORGANIZATION_ON_USER_ORDER_FIELD_NAME -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    OrganizationOnUserSortType.NAME_ASC : OrganizationOnUserSortType.NAME_DESC;
            default -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    OrganizationOnUserSortType.ADD_AT_ASC : OrganizationOnUserSortType.ADD_AT_DESC;
        };

        PaginationType paginationType;
        switch (request.getPagination().getType()) {
            case PAGINATION_TYPE_CURSOR -> paginationType = PaginationType.CURSOR;
            case PAGINATION_TYPE_OFFSET -> paginationType = PaginationType.OFFSET;
            default -> paginationType = PaginationType.NONE;
        }

        ListEntityWithCount<OrganizationUserEntity> organizations = organizationService.getOrganizationsOnUser(
                request.getUserId(), paginationType, request.getPagination().getLimit(), request.getPagination().getOffset(),
                request.getPagination().getCursor(), sortType, request.getWithCount());

        List<OrganizationOnUser> organizationOnUsers = organizations.getData().stream()
                .map(ProtoMapper::convertOnUser).toList();

        GetOrganizationsOnUserResponse response = GetOrganizationsOnUserResponse.newBuilder()
                .addAllOrganizations(organizationOnUsers)
                .setCount(
                        Count.newBuilder().setIsValid(request.getWithCount())
                                .setCount(organizations.getCount()).build()
                )
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void createOrganization(CreateOrganizationRequest request, StreamObserver<CreateOrganizationResponse> responseObserver) {
        String operatorId = request.getOperatorId();
        OrganizationEntity organization = new OrganizationEntity();
        organization.setName(request.getName());
        organization.setPlan(request.getPlan());
        List<OrganizationUserEntity> users = request.getUsersList().stream()
                .map(user -> {
                    OrganizationUserEntity userEntity = new OrganizationUserEntity();
                    userEntity.setUserId(user.getUserId());
                    return userEntity;
                })
                .toList();

        String jobId = organizationService.beginCreate(operatorId, organization, users);
        CreateOrganizationResponse response = CreateOrganizationResponse.newBuilder()
                .setJobId(jobId)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void addOrganizationUser(AddOrganizationUserRequest request, StreamObserver<AddOrganizationUserResponse> responseObserver) {
        String operatorId = request.getOperatorId();
        String organizationId = request.getOrganizationId();
        List<OrganizationUserEntity> users = request.getUsersList().stream()
                .map(user -> {
                    OrganizationUserEntity userEntity = new OrganizationUserEntity();
                    userEntity.setUserId(user.getUserId());
                    return userEntity;
                })
                .toList();

        String jobId = organizationService.beginAddUsers(operatorId, organizationId, users);
        AddOrganizationUserResponse response = AddOrganizationUserResponse.newBuilder()
                .setJobId(jobId)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
