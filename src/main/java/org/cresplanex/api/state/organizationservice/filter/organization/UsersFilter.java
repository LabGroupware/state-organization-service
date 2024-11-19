package org.cresplanex.api.state.organizationservice.filter.organization;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UsersFilter {

    private boolean isValid;
    private boolean any;
    private List<String> userIds;
}
