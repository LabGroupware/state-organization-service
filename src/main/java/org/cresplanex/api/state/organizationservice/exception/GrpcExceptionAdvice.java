package org.cresplanex.api.state.organizationservice.exception;

import build.buf.gen.organization.v1.*;
import build.buf.gen.userpreference.v1.*;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.cresplanex.api.state.common.saga.local.organization.InvalidOrganizationPlanException;

@Slf4j
@GrpcAdvice
public class GrpcExceptionAdvice {

     @GrpcExceptionHandler(OrganizationNotFoundException.class)
     public Status handleOrganizationNotFoundException(OrganizationNotFoundException e) {
        OrganizationServiceOrganizationNotFoundError.Builder descriptionBuilder =
                OrganizationServiceOrganizationNotFoundError.newBuilder()
                .setMeta(buildErrorMeta(e));

        switch (e.getFindType()) {
            case BY_ID:
                descriptionBuilder
                        .setFindFieldType(OrganizationUniqueFieldType.ORGANIZATION_UNIQUE_FIELD_TYPE_ORGANIZATION_ID)
                        .setOrganizationId(e.getAggregateId());
                break;
        }

         return Status.NOT_FOUND
                 .withDescription(descriptionBuilder.build().toString())
                 .withCause(e);
     }

     private OrganizationServiceErrorMeta buildErrorMeta(ServiceException e) {
         return OrganizationServiceErrorMeta.newBuilder()
                 .setCode(e.getServiceErrorCode())
                 .setMessage(e.getErrorCaption())
                 .build();
     }

    @GrpcExceptionHandler
    public Status handleInternal(Throwable e) {
        log.error("Internal error", e);

        String message = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";

         OrganizationServiceInternalError.Builder descriptionBuilder =
                 OrganizationServiceInternalError.newBuilder()
                         .setMeta(OrganizationServiceErrorMeta.newBuilder()
                                 .setCode(OrganizationServiceErrorCode.ORGANIZATION_SERVICE_ERROR_CODE_INTERNAL)
                                 .setMessage(message)
                                 .build());

         return Status.INTERNAL
                 .withDescription(descriptionBuilder.build().toString())
                 .withCause(e);
    }
}
