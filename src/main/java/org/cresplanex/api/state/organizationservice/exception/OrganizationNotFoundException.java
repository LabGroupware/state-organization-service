package org.cresplanex.api.state.organizationservice.exception;

import build.buf.gen.organization.v1.OrganizationServiceErrorCode;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrganizationNotFoundException extends ServiceException {

    private final FindType findType;
    private final String aggregateId;

    public OrganizationNotFoundException(FindType findType, String aggregateId) {
        this(findType, aggregateId, "Model not found: " + findType.name() + " with id " + aggregateId);
    }

    public OrganizationNotFoundException(FindType findType, String aggregateId, String message) {
        super(message);
        this.findType = findType;
        this.aggregateId = aggregateId;
    }

    public OrganizationNotFoundException(FindType findType, String aggregateId, String message, Throwable cause) {
        super(message, cause);
        this.findType = findType;
        this.aggregateId = aggregateId;
    }

    public enum FindType {
        BY_ID
    }

    @Override
    public OrganizationServiceErrorCode getServiceErrorCode() {
        return OrganizationServiceErrorCode.ORGANIZATION_SERVICE_ERROR_CODE_ORGANIZATION_NOT_FOUND;
    }

    @Override
    public String getErrorCaption() {
        return switch (findType) {
            case BY_ID -> "Organization not found (ID = %s)".formatted(aggregateId);
            default -> "Organization not found";
        };
    }
}
