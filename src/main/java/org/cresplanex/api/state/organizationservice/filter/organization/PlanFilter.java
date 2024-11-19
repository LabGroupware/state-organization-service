package org.cresplanex.api.state.organizationservice.filter.organization;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PlanFilter {

    private boolean isValid;
    private List<String> plans;
}
