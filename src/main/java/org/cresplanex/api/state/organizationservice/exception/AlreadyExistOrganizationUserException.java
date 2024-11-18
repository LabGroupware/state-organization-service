package org.cresplanex.api.state.organizationservice.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class AlreadyExistOrganizationUserException extends RuntimeException {

    private final List<String> userIds;

    public AlreadyExistOrganizationUserException(List<String> userIds) {
        super("Already exist organization user with userIds: " + userIds.stream().reduce((a, b) -> a + ", " + b).orElse(""));
        this.userIds = userIds;
    }
}
