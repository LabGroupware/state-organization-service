package org.cresplanex.api.state.organizationservice.handler;

import build.buf.gen.organization.v1.*;
import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.entity.OrganizationUserEntity;
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

    // TODO: pagination + with count
    @Override
    public void getOrganizations(GetOrganizationsRequest request, StreamObserver<GetOrganizationsResponse> responseObserver) {
        List<OrganizationEntity> organizations = organizationService.get();

        List<Organization> organizationProtos = organizations.stream()
                .map(ProtoMapper::convert).toList();
        GetOrganizationsResponse response = GetOrganizationsResponse.newBuilder()
                .addAllOrganizations(organizationProtos)
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
