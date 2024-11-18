package org.cresplanex.api.state.organizationservice.saga.state.organization;

import lombok.*;
import org.cresplanex.api.state.common.dto.organization.UserOnOrganizationDto;
import org.cresplanex.api.state.common.dto.team.UserOnTeamDto;
import org.cresplanex.api.state.common.saga.command.organization.AddUsersOrganizationCommand;
import org.cresplanex.api.state.common.saga.command.team.AddUsersDefaultTeamCommand;
import org.cresplanex.api.state.common.saga.state.SagaState;
import org.cresplanex.api.state.common.saga.validate.userprofile.UserExistValidateCommand;
import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.saga.model.organization.AddUsersOrganizationSaga;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AddUsersOrganizationSagaState
        extends SagaState<AddUsersOrganizationSaga.Action, OrganizationEntity> {
    private InitialData initialData;
    private List<UserOnOrganizationDto> addedUsers = new ArrayList<>();
    private List<UserOnTeamDto> addedUsersOnTeam = new ArrayList<>();
    private String operatorId;

    @Override
    public String getId() {
        return initialData.organizationId;
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
        private String organizationId;
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
                        .map(AddUsersOrganizationSagaState.InitialData.User::getUserId)
                        .toList());
    }

    public AddUsersOrganizationCommand.Exec makeAddUsersOrganizationCommand() {
        return new AddUsersOrganizationCommand.Exec(
                this.operatorId,
                initialData.getOrganizationId(),
                initialData.getUsers().stream()
                        .map(user -> new AddUsersOrganizationCommand.Exec.User(user.getUserId()))
                        .toList()
        );
    }

    public AddUsersOrganizationCommand.Undo makeUndoAddUsersOrganizationCommand() {
        return new AddUsersOrganizationCommand.Undo(
                addedUsers.stream()
                        .map(UserOnOrganizationDto::getUserOrganizationId)
                        .toList()
        );
    }

    public AddUsersDefaultTeamCommand.Exec makeAddUsersDefaultTeamCommand() {
        return new AddUsersDefaultTeamCommand.Exec(
                this.operatorId,
                initialData.getOrganizationId(),
                initialData.getUsers().stream()
                        .map(user -> new AddUsersDefaultTeamCommand.Exec.User(user.getUserId()))
                        .toList()
        );
    }

    public AddUsersDefaultTeamCommand.Undo makeUndoAddUsersDefaultTeamCommand() {
        return new AddUsersDefaultTeamCommand.Undo(
                addedUsersOnTeam.stream()
                        .map(UserOnTeamDto::getUserTeamId)
                        .toList()
        );
    }
}
