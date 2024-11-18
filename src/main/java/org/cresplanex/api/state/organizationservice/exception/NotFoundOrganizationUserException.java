package org.cresplanex.api.state.organizationservice.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class NotFoundOrganizationUserException extends RuntimeException {

    private final List<String> userIds;

    public NotFoundOrganizationUserException(List<String> userIds) {
        super("Not found organization user with userIds: " + userIds.stream().reduce((a, b) -> a + ", " + b).orElse(""));
        this.userIds = userIds;
    }
}
