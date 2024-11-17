package org.cresplanex.api.state.organizationservice.saga.proxy;

import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.command.team.AddUsersDefaultTeamCommand;
import org.cresplanex.api.state.common.saga.command.team.CreateDefaultTeamAndAddInitialDefaultTeamUserCommand;
import org.cresplanex.core.saga.simpledsl.CommandEndpoint;
import org.cresplanex.core.saga.simpledsl.CommandEndpointBuilder;
import org.springframework.stereotype.Component;

@Component
public class TeamServiceProxy {

    public final CommandEndpoint<CreateDefaultTeamAndAddInitialDefaultTeamUserCommand.Exec> createDefaultTeamAndAddInitialDefaultTeamUser
            = CommandEndpointBuilder
            .forCommand(CreateDefaultTeamAndAddInitialDefaultTeamUserCommand.Exec.class)
            .withChannel(SagaCommandChannel.TEAM)
            .withCommandType(CreateDefaultTeamAndAddInitialDefaultTeamUserCommand.Exec.TYPE)
            .build();

    public final CommandEndpoint<CreateDefaultTeamAndAddInitialDefaultTeamUserCommand.Undo> undoCreateDefaultTeamAndAddInitialDefaultTeamUser
            = CommandEndpointBuilder
            .forCommand(CreateDefaultTeamAndAddInitialDefaultTeamUserCommand.Undo.class)
            .withChannel(SagaCommandChannel.TEAM)
            .withCommandType(CreateDefaultTeamAndAddInitialDefaultTeamUserCommand.Undo.TYPE)
            .build();

    public final CommandEndpoint<AddUsersDefaultTeamCommand.Exec> addUsersDefaultTeam
            = CommandEndpointBuilder
            .forCommand(AddUsersDefaultTeamCommand.Exec.class)
            .withChannel(SagaCommandChannel.TEAM)
            .withCommandType(AddUsersDefaultTeamCommand.Exec.TYPE)
            .build();

    public final CommandEndpoint<AddUsersDefaultTeamCommand.Undo> undoAddUsersDefaultTeam
            = CommandEndpointBuilder
            .forCommand(AddUsersDefaultTeamCommand.Undo.class)
            .withChannel(SagaCommandChannel.TEAM)
            .withCommandType(AddUsersDefaultTeamCommand.Undo.TYPE)
            .build();
}
