package org.cresplanex.api.state.organizationservice.saga.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.common.constants.UserPreferenceServiceApplicationCode;
import org.cresplanex.api.state.common.entity.EntityWithPrevious;
import org.cresplanex.api.state.common.saga.LockTargetType;
import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.command.userpreference.CreateUserPreferenceCommand;
import org.cresplanex.api.state.common.saga.command.userpreference.UpdateUserPreferenceCommand;
import org.cresplanex.api.state.common.saga.reply.userpreference.CreateUserPreferenceReply;
import org.cresplanex.api.state.common.saga.reply.userpreference.UpdateUserPreferenceReply;
import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
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

import static org.cresplanex.core.commands.consumer.CommandHandlerReplyBuilder.withException;
import static org.cresplanex.core.commands.consumer.CommandHandlerReplyBuilder.withFailure;
import static org.cresplanex.core.commands.consumer.CommandHandlerReplyBuilder.withSuccess;
import static org.cresplanex.core.saga.participant.SagaReplyMessageBuilder.withLock;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPreferenceSagaCommandHandlers {

    private final OrganizationService organizationService;

    public CommandHandlers commandHandlers() {
        return SagaCommandHandlersBuilder
                .fromChannel(SagaCommandChannel.USER_PREFERENCE)
                .onMessage(CreateUserPreferenceCommand.Exec.class,
                        CreateUserPreferenceCommand.Exec.TYPE,
                        this::handleCreateUserPreferenceCommand
                )
                .onMessage(CreateUserPreferenceCommand.Undo.class,
                        CreateUserPreferenceCommand.Undo.TYPE,
                        this::handleUndoCreateUserPreferenceCommand
                )
                .withPreLock(this::undoCreateUserPreferencePreLock)

                .onMessage(UpdateUserPreferenceCommand.Exec.class,
                        UpdateUserPreferenceCommand.Exec.TYPE,
                        this::handleUpdateUserPreferenceCommand
                )
                .withPreLock(this::updateUserPreferencePreLock)
                .onMessage(UpdateUserPreferenceCommand.Undo.class,
                        UpdateUserPreferenceCommand.Undo.TYPE,
                        this::handleUndoUpdateUserPreferenceCommand
                )
                .withPreLock(this::undoUpdateUserPreferencePreLock)
                .build();
    }

    private LockTarget undoCreateUserPreferencePreLock(
            CommandMessage<CreateUserPreferenceCommand.Undo> cmd, PathVariables pvs) {
        return new LockTarget(LockTargetType.USER_PREFERENCE, cmd.getCommand().getUserPreferenceId());
    }

    private Message handleCreateUserPreferenceCommand(CommandMessage<CreateUserPreferenceCommand.Exec> cmd) {
        try {
            CreateUserPreferenceCommand.Exec command = cmd.getCommand();
            OrganizationEntity userPreference = new OrganizationEntity();
            userPreference.setUserId(command.getUserId());
            userPreference = organizationService.create(userPreference);
            CreateUserPreferenceReply.Success reply = new CreateUserPreferenceReply.Success(
                    new CreateUserPreferenceReply.Success.Data(DtoMapper.convert(userPreference)),
                    UserPreferenceServiceApplicationCode.SUCCESS,
                    "User preference created successfully",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

            return withLock(LockTargetType.USER_PREFERENCE, userPreference.getUserPreferenceId())
                    .withSuccess(reply, CreateUserPreferenceReply.Success.TYPE);
        } catch (Exception e) {
            CreateUserPreferenceReply.Failure reply = new CreateUserPreferenceReply.Failure(
                    null,
                    UserPreferenceServiceApplicationCode.INTERNAL_SERVER_ERROR,
                    "Failed to create user preference",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withException(reply, CreateUserPreferenceReply.Failure.TYPE);
        }
    }

    private Message handleUndoCreateUserPreferenceCommand(CommandMessage<CreateUserPreferenceCommand.Undo> cmd) {
        try {
            CreateUserPreferenceCommand.Undo command = cmd.getCommand();
            String userPreferenceId = command.getUserPreferenceId();
            organizationService.undoCreate(userPreferenceId);
            return withSuccess();
        } catch (Exception e) {
            return withFailure();
        }
    }

    private LockTarget updateUserPreferencePreLock(
            CommandMessage<UpdateUserPreferenceCommand.Exec> cmd,
            PathVariables pathVariables
    ) {
        return new LockTarget(LockTargetType.USER_PREFERENCE, cmd.getCommand().getUserPreferenceId());
    }

    private LockTarget undoUpdateUserPreferencePreLock(
            CommandMessage<UpdateUserPreferenceCommand.Undo> cmd,
            PathVariables pathVariables
    ) {
        return new LockTarget(LockTargetType.USER_PREFERENCE, cmd.getCommand().getUserPreferenceId());
    }

    private Message handleUpdateUserPreferenceCommand(CommandMessage<UpdateUserPreferenceCommand.Exec> cmd) {
        try {
            UpdateUserPreferenceCommand.Exec command = cmd.getCommand();
            OrganizationEntity userPreference = new OrganizationEntity();
            userPreference.setLanguage(command.getLanguage());
            userPreference.setTheme(command.getTheme());
            userPreference.setTimezone(command.getTimezone());
            EntityWithPrevious<OrganizationEntity> entityWithPrev =
                    organizationService.update(command.getUserPreferenceId(), userPreference);
            UpdateUserPreferenceReply.Success reply = new UpdateUserPreferenceReply.Success(
                    new UpdateUserPreferenceReply.Success.Data(
                            DtoMapper.convert(entityWithPrev.getCurrent()),
                            DtoMapper.convert(entityWithPrev.getPrevious())
                    ),
                    UserPreferenceServiceApplicationCode.SUCCESS,
                    "User preference updated successfully",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withSuccess(reply, UpdateUserPreferenceReply.Success.TYPE);
        } catch (Exception e) {
            UpdateUserPreferenceReply.Failure reply = new UpdateUserPreferenceReply.Failure(
                    null,
                    UserPreferenceServiceApplicationCode.INTERNAL_SERVER_ERROR,
                    "Failed to create user preference",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withFailure(reply, UpdateUserPreferenceReply.Failure.TYPE);
        }
    }

    private Message handleUndoUpdateUserPreferenceCommand(CommandMessage<UpdateUserPreferenceCommand.Undo> cmd) {
        try {
            UpdateUserPreferenceCommand.Undo command = cmd.getCommand();
            String userPreferenceId = command.getUserPreferenceId();
            OrganizationEntity userPreference = new OrganizationEntity();
            userPreference.setLanguage(command.getOriginLanguage());
            userPreference.setTheme(command.getOriginTheme());
            userPreference.setTimezone(command.getOriginTimezone());
            organizationService.undoUpdate(userPreferenceId, userPreference);
            return withSuccess();
        } catch (Exception e) {
            return withFailure();
        }
    }
}
