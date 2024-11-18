package org.cresplanex.api.state.organizationservice.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class NotFoundOrganizationException extends RuntimeException {

    private final List<String> organizationIds;

    public NotFoundOrganizationException(List<String> organizationIds) {
        super("Not found organization with organizationIds: " + organizationIds.stream().reduce((a, b) -> a + ", " + b).orElse(""));
        this.organizationIds = organizationIds;
    }
}
