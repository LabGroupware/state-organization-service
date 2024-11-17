package org.cresplanex.api.state.organizationservice.handler;

import build.buf.gen.organization.v1.*;
import build.buf.gen.userpreference.v1.*;
import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.mapper.proto.ProtoMapper;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

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
}
