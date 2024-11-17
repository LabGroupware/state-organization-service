package org.cresplanex.api.state.organizationservice.exception;

import build.buf.gen.organization.v1.OrganizationServiceErrorCode;

public abstract class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    abstract public OrganizationServiceErrorCode getServiceErrorCode();
    abstract public String getErrorCaption();
}
