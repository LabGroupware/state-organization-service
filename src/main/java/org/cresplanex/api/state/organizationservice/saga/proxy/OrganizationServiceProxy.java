package org.cresplanex.api.state.organizationservice.saga.proxy;

import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.command.organization.AddUsersOrganizationCommand;
import org.cresplanex.api.state.common.saga.command.organization.CreateOrganizationAndAddInitialOrganizationUserCommand;
import org.cresplanex.core.saga.simpledsl.CommandEndpoint;
import org.cresplanex.core.saga.simpledsl.CommandEndpointBuilder;
import org.springframework.stereotype.Component;

@Component
public class OrganizationServiceProxy {

    public final CommandEndpoint<CreateOrganizationAndAddInitialOrganizationUserCommand.Exec> createOrganizationAndAddInitialOrganizationUser
            = CommandEndpointBuilder
            .forCommand(CreateOrganizationAndAddInitialOrganizationUserCommand.Exec.class)
            .withChannel(SagaCommandChannel.ORGANIZATION)
            .withCommandType(CreateOrganizationAndAddInitialOrganizationUserCommand.Exec.TYPE)
            .build();

    public final CommandEndpoint<CreateOrganizationAndAddInitialOrganizationUserCommand.Undo> undoCreateOrganizationAndAddInitialOrganizationUser
            = CommandEndpointBuilder
            .forCommand(CreateOrganizationAndAddInitialOrganizationUserCommand.Undo.class)
            .withChannel(SagaCommandChannel.ORGANIZATION)
            .withCommandType(CreateOrganizationAndAddInitialOrganizationUserCommand.Undo.TYPE)
            .build();

    public final CommandEndpoint<AddUsersOrganizationCommand.Exec> addUsersOrganization
            = CommandEndpointBuilder
            .forCommand(AddUsersOrganizationCommand.Exec.class)
            .withChannel(SagaCommandChannel.ORGANIZATION)
            .withCommandType(AddUsersOrganizationCommand.Exec.TYPE)
            .build();

    public final CommandEndpoint<AddUsersOrganizationCommand.Undo> undoAddUsersOrganization
            = CommandEndpointBuilder
            .forCommand(AddUsersOrganizationCommand.Undo.class)
            .withChannel(SagaCommandChannel.ORGANIZATION)
            .withCommandType(AddUsersOrganizationCommand.Undo.TYPE)
            .build();
}
