package org.cresplanex.api.state.organizationservice.filter.organization;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OwnerFilter {

    private boolean isValid;
    private List<String> ownerIds;
}
