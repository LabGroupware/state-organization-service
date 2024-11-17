package org.cresplanex.api.state.organizationservice.saga.state.organization;

import lombok.*;
import org.cresplanex.api.state.common.dto.userpreference.UserPreferenceDto;
import org.cresplanex.api.state.common.saga.state.SagaState;
import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.saga.model.organization.AddUsersOrganizationSaga;

@Getter
@Setter
@NoArgsConstructor
public class AddUsersOrganizationSagaState
        extends SagaState<AddUsersOrganizationSaga.Action, OrganizationEntity> {
    private InitialData initialData;
    private UserPreferenceDto userPreferenceDto = UserPreferenceDto.empty();
    private UserPreferenceDto prevUserPreferenceDto = UserPreferenceDto.empty();
    private String operatorId;

    @Override
    public String getId() {
        return initialData.userPreferenceId;
    }

    @Override
    public Class<OrganizationEntity> getEntityClass() {
        return OrganizationEntity.class;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InitialData {
        private String userPreferenceId;
        private String theme;
        private String language;
        private String timezone;
    }

    public AddUsersOrganizationCommand.Exec makeAddUsersOrganizationCommand() {
        return new AddUsersOrganizationCommand.Exec(
                this.operatorId,
                initialData.getUserPreferenceId(),
                initialData.getTheme(),
                initialData.getLanguage(),
                initialData.getTimezone()
        );
    }

    public AddUsersOrganizationCommand.Undo makeUndoAddUsersOrganizationCommand() {
        return new AddUsersOrganizationCommand.Undo(
                userPreferenceDto.getUserPreferenceId(),
                prevUserPreferenceDto.getTheme(),
                prevUserPreferenceDto.getLanguage(),
                prevUserPreferenceDto.getTimezone()
        );
    }
}
