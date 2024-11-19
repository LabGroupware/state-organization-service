package org.cresplanex.api.state.organizationservice.saga.state.organization;

import lombok.*;
import org.cresplanex.api.state.common.dto.organization.OrganizationWithUsersDto;
import org.cresplanex.api.state.common.dto.team.TeamDto;
import org.cresplanex.api.state.common.saga.command.organization.CreateOrganizationAndAddInitialOrganizationUserCommand;
import org.cresplanex.api.state.common.saga.command.team.CreateDefaultTeamAndAddInitialDefaultTeamUserCommand;
import org.cresplanex.api.state.common.saga.state.SagaState;
import org.cresplanex.api.state.common.saga.validate.userprofile.UserExistValidateCommand;
import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.saga.model.organization.CreateOrganizationSaga;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class CreateOrganizationSagaState
        extends SagaState<CreateOrganizationSaga.Action, OrganizationEntity> {
    private InitialData initialData;
    private OrganizationWithUsersDto organizationWithUsersDto = OrganizationWithUsersDto.empty();
    private TeamDto teamDto = TeamDto.empty();
    private String operatorId;

    @Override
    public String getId() {
        return organizationWithUsersDto.getOrganization().getOrganizationId();
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
        private String name;
        private String plan;
        private List<User> users;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class User{
            private String userId;
        }
    }

    public UserExistValidateCommand makeUserExistValidateCommand() {
        return new UserExistValidateCommand(
                initialData.getUsers().stream()
                .map(InitialData.User::getUserId)
                .toList());
    }

    public CreateOrganizationAndAddInitialOrganizationUserCommand.Exec makeCreateOrganizationAndAddInitialOrganizationUserCommand() {
        return new CreateOrganizationAndAddInitialOrganizationUserCommand.Exec(
                this.operatorId,
                initialData.getName(),
                initialData.getPlan(),
                initialData.getUsers().stream()
                        .map(user -> new CreateOrganizationAndAddInitialOrganizationUserCommand.Exec.User(user.getUserId()))
                        .toList()
        );
    }

    public CreateOrganizationAndAddInitialOrganizationUserCommand.Undo makeUndoCreateOrganizationAndAddInitialOrganizationUserCommand() {
        return new CreateOrganizationAndAddInitialOrganizationUserCommand.Undo(organizationWithUsersDto.getOrganization().getOrganizationId());
    }

    public CreateDefaultTeamAndAddInitialDefaultTeamUserCommand.Exec makeCreateDefaultTeamAndAddInitialDefaultTeamUserCommand() {
        return new CreateDefaultTeamAndAddInitialDefaultTeamUserCommand.Exec(
                this.operatorId,
                organizationWithUsersDto.getOrganization().getOrganizationId(),
                organizationWithUsersDto.getOrganization().getName(),
                initialData.getUsers().stream()
                        .map(user -> new CreateDefaultTeamAndAddInitialDefaultTeamUserCommand.Exec.User(user.getUserId()))
                        .toList()
        );
    }

    public CreateDefaultTeamAndAddInitialDefaultTeamUserCommand.Undo makeUndoCreateDefaultTeamAndAddInitialDefaultTeamUserCommand() {
        return new CreateDefaultTeamAndAddInitialDefaultTeamUserCommand.Undo(teamDto.getTeamId());
    }
}
