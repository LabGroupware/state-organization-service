package org.cresplanex.api.state.organizationservice.saga.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.common.constants.OrganizationServiceApplicationCode;
import org.cresplanex.api.state.common.saga.LockTargetType;
import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.command.organization.AddUsersOrganizationCommand;
import org.cresplanex.api.state.common.saga.command.organization.CreateOrganizationAndAddInitialOrganizationUserCommand;
import org.cresplanex.api.state.common.saga.reply.organization.AddUsersOrganizationReply;
import org.cresplanex.api.state.common.saga.reply.organization.CreateOrganizationAndAddInitialOrganizationUserReply;
import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.entity.OrganizationUserEntity;
import org.cresplanex.api.state.organizationservice.mapper.dto.DtoMapper;
import org.cresplanex.api.state.organizationservice.service.OrganizationService;
import org.cresplanex.core.commands.consumer.CommandHandlers;
import org.cresplanex.core.commands.consumer.CommandMessage;
import org.cresplanex.core.commands.consumer.PathVariables;
import org.cresplanex.core.messaging.common.Message;
import org.cresplanex.core.saga.lock.LockTarget;
import org.cresplanex.core.saga.participant.SagaCommandHandlersBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.cresplanex.core.commands.consumer.CommandHandlerReplyBuilder.withException;
import static org.cresplanex.core.commands.consumer.CommandHandlerReplyBuilder.withSuccess;
import static org.cresplanex.core.saga.participant.SagaReplyMessageBuilder.withLock;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizationSagaCommandHandlers {

    private final OrganizationService organizationService;

    public CommandHandlers commandHandlers() {
        return SagaCommandHandlersBuilder
                .fromChannel(SagaCommandChannel.ORGANIZATION)
                .onMessage(CreateOrganizationAndAddInitialOrganizationUserCommand.Exec.class,
                        CreateOrganizationAndAddInitialOrganizationUserCommand.Exec.TYPE,
                        this::handleCreateOrganizationAndAddInitialOrganizationUserCommand
                )
                .onMessage(CreateOrganizationAndAddInitialOrganizationUserCommand.Undo.class,
                        CreateOrganizationAndAddInitialOrganizationUserCommand.Undo.TYPE,
                        this::handleUndoCreateOrganizationAndAddInitialOrganizationUserCommand
                )
                .withPreLock(this::undoCreateOrganizationAndAddInitialOrganizationUserPreLock)

                .onMessage(AddUsersOrganizationCommand.Exec.class,
                        AddUsersOrganizationCommand.Exec.TYPE,
                        this::handleAddUsersOrganizationCommand
                )
                .onMessage(AddUsersOrganizationCommand.Undo.class,
                        AddUsersOrganizationCommand.Undo.TYPE,
                        this::handleUndoAddUsersOrganizationCommand
                )
                .build();
    }

    private LockTarget undoCreateOrganizationAndAddInitialOrganizationUserPreLock(
            CommandMessage<CreateOrganizationAndAddInitialOrganizationUserCommand.Undo> cmd,
            PathVariables pathVariables
    ) {
        return new LockTarget(LockTargetType.ORGANIZATION, cmd.getCommand().getOrganizationId());
    }

    private Message handleCreateOrganizationAndAddInitialOrganizationUserCommand(
            CommandMessage<CreateOrganizationAndAddInitialOrganizationUserCommand.Exec> cmd) {
        try {
            CreateOrganizationAndAddInitialOrganizationUserCommand.Exec command = cmd.getCommand();
            OrganizationEntity organization = new OrganizationEntity();
            organization.setName(command.getName());
            organization.setPlan(command.getPlan());
            organization.setOwnerId(command.getOperatorId());
            List<OrganizationUserEntity> users = command.getUsers().stream().map(user -> {
                OrganizationUserEntity userEntity = new OrganizationUserEntity();
                userEntity.setUserId(user.getUserId());
                return userEntity;
            }).toList();
            organization.setOrganizationUsers(users);
            organization = organizationService.createAndAddUsers(command.getOperatorId(), organization);
            CreateOrganizationAndAddInitialOrganizationUserReply.Success reply = new CreateOrganizationAndAddInitialOrganizationUserReply.Success(
                    new CreateOrganizationAndAddInitialOrganizationUserReply.Success.Data(
                            DtoMapper.convert(organization),
                            DtoMapper.convert(organization.getOrganizationUsers())
                    ),
                    OrganizationServiceApplicationCode.SUCCESS,
                    "Organization created successfully",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

            return withLock(LockTargetType.USER_PREFERENCE, organization.getOrganizationId())
                    .withSuccess(reply, CreateOrganizationAndAddInitialOrganizationUserReply.Success.TYPE);
        } catch (Exception e) {
            CreateOrganizationAndAddInitialOrganizationUserReply.Failure reply = new CreateOrganizationAndAddInitialOrganizationUserReply.Failure(
                    null,
                    OrganizationServiceApplicationCode.INTERNAL_SERVER_ERROR,
                    "Failed to create organization",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withException(reply, CreateOrganizationAndAddInitialOrganizationUserReply.Failure.TYPE);
        }
    }

    private Message handleUndoCreateOrganizationAndAddInitialOrganizationUserCommand(
            CommandMessage<CreateOrganizationAndAddInitialOrganizationUserCommand.Undo> cmd
    ) {
        try {
        CreateOrganizationAndAddInitialOrganizationUserCommand.Undo command = cmd.getCommand();
            String organizationId = command.getOrganizationId();
            organizationService.undoCreate(organizationId);
            return withSuccess();
        } catch (Exception e) {
            return withException();
        }
    }

    private Message handleAddUsersOrganizationCommand(
            CommandMessage<AddUsersOrganizationCommand.Exec> cmd
    ) {
        try {
            AddUsersOrganizationCommand.Exec command = cmd.getCommand();
            List<String> users = command.getUsers().stream().map(AddUsersOrganizationCommand.Exec.User::getUserId).toList();

            List<OrganizationUserEntity> organizationUsers = organizationService.addUsers(command.getOperatorId(), command.getOrganizationId(), users);
            AddUsersOrganizationReply.Success reply = new AddUsersOrganizationReply.Success(
                    new AddUsersOrganizationReply.Success.Data(
                            DtoMapper.convert(organizationUsers)
                    ),
                    OrganizationServiceApplicationCode.SUCCESS,
                    "Users added successfully",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withSuccess(reply, AddUsersOrganizationReply.Success.TYPE);
        } catch (Exception e) {
            AddUsersOrganizationReply.Failure reply = new AddUsersOrganizationReply.Failure(
                    null,
                    OrganizationServiceApplicationCode.INTERNAL_SERVER_ERROR,
                    "Failed to add users",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withException(reply, AddUsersOrganizationReply.Failure.TYPE);
        }
    }

    private Message handleUndoAddUsersOrganizationCommand(CommandMessage<AddUsersOrganizationCommand.Undo> cmd) {
        try {
            AddUsersOrganizationCommand.Undo command = cmd.getCommand();
            organizationService.undoAddUsers(command.getUserOrganizationIds());
            return withSuccess();
        } catch (Exception e) {
            return withException();
        }
    }
}
