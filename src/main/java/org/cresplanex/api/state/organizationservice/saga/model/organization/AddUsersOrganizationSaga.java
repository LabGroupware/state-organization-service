package org.cresplanex.api.state.organizationservice.saga.model.organization;

import org.checkerframework.checker.units.qual.A;
import org.cresplanex.api.state.common.constants.OrganizationServiceApplicationCode;
import org.cresplanex.api.state.common.event.model.organization.OrganizationAddedUsers;
import org.cresplanex.api.state.common.event.model.organization.OrganizationDomainEvent;
import org.cresplanex.api.state.common.event.publisher.AggregateDomainEventPublisher;
import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.data.organization.AddUsersOrganizationResultData;
import org.cresplanex.api.state.common.saga.local.organization.InvalidOrganizationPlanException;
import org.cresplanex.api.state.common.saga.local.organization.NotFoundOrganizationException;
import org.cresplanex.api.state.common.saga.local.organization.NotFoundOrganizationException;
import org.cresplanex.api.state.common.saga.model.SagaModel;
import org.cresplanex.api.state.common.saga.reply.organization.AddUsersOrganizationReply;
import org.cresplanex.api.state.common.saga.reply.organization.CreateOrganizationAndAddInitialOrganizationUserReply;
import org.cresplanex.api.state.common.saga.reply.team.AddUsersDefaultTeamReply;
import org.cresplanex.api.state.common.saga.reply.team.CreateDefaultTeamAndAddInitialDefaultTeamUserReply;
import org.cresplanex.api.state.common.saga.reply.userprofile.UserProfileExistValidateReply;
import org.cresplanex.api.state.common.saga.type.OrganizationSagaType;
import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.event.publisher.OrganizationDomainEventPublisher;
import org.cresplanex.api.state.organizationservice.saga.proxy.OrganizationServiceProxy;
import org.cresplanex.api.state.organizationservice.saga.proxy.TeamServiceProxy;
import org.cresplanex.api.state.organizationservice.saga.proxy.UserProfileServiceProxy;
import org.cresplanex.api.state.organizationservice.saga.state.organization.AddUsersOrganizationSagaState;
import org.cresplanex.api.state.organizationservice.saga.state.organization.CreateOrganizationSagaState;
import org.cresplanex.api.state.organizationservice.service.OrganizationService;
import org.cresplanex.core.saga.orchestration.SagaDefinition;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AddUsersOrganizationSaga extends SagaModel<
        OrganizationEntity,
        OrganizationDomainEvent,
        AddUsersOrganizationSaga.Action,
        AddUsersOrganizationSagaState> {

    private final SagaDefinition<AddUsersOrganizationSagaState> sagaDefinition;
    private final OrganizationDomainEventPublisher domainEventPublisher;
    private final OrganizationService organizationLocalService;

    public AddUsersOrganizationSaga(
            OrganizationService organizationLocalService,
            OrganizationServiceProxy organizationService,
            TeamServiceProxy teamService,
            UserProfileServiceProxy userProfileService,
            OrganizationDomainEventPublisher domainEventPublisher
    ) {
        this.sagaDefinition = step()
                .invokeLocal(this::validateOrganization)
                .onException(NotFoundOrganizationException.class, this::handleNotFoundOrganizationException)
                .onExceptionRollback(NotFoundOrganizationException.class)
                .step()
                .invokeParticipant(
                        userProfileService.userProfileExistValidate,
                        AddUsersOrganizationSagaState::makeUserProfileExistValidateCommand
                )
                .onReply(
                        UserProfileExistValidateReply.Success.class,
                        UserProfileExistValidateReply.Success.TYPE,
                        this::handleUserProfileExistValidateReply
                )
                .onReply(
                        UserProfileExistValidateReply.Failure.class,
                        UserProfileExistValidateReply.Failure.TYPE,
                        this::handleFailureReply
                )
                .step()
                .invokeParticipant(
                        organizationService.addUsersOrganization,
                        AddUsersOrganizationSagaState::makeAddUsersOrganizationCommand
                )
                .onReply(
                        AddUsersOrganizationReply.Success.class,
                        AddUsersOrganizationReply.Success.TYPE,
                        this::handleAddUsersOrganizationReply
                )
                .onReply(
                        AddUsersOrganizationReply.Failure.class,
                        AddUsersOrganizationReply.Failure.TYPE,
                        this::handleFailureReply
                )
                .withCompensation(
                        organizationService.undoAddUsersOrganization,
                        AddUsersOrganizationSagaState::makeUndoAddUsersOrganizationCommand
                )
                .step()
                .invokeParticipant(
                        teamService.addUsersDefaultTeam,
                        AddUsersOrganizationSagaState::makeAddUsersDefaultTeamCommand
                )
                .onReply(
                        AddUsersDefaultTeamReply.Success.class,
                        AddUsersDefaultTeamReply.Success.TYPE,
                        this::handleAddUsersDefaultTeamReply
                )
                .onReply(
                        AddUsersDefaultTeamReply.Failure.class,
                        AddUsersDefaultTeamReply.Failure.TYPE,
                        this::handleFailureReply
                )
                .withCompensation(
                        teamService.undoAddUsersDefaultTeam,
                        AddUsersOrganizationSagaState::makeUndoAddUsersDefaultTeamCommand
                )
                .build();
        this.organizationLocalService = organizationLocalService;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    protected AggregateDomainEventPublisher<OrganizationEntity, OrganizationDomainEvent>
    getDomainEventPublisher() {
        return domainEventPublisher;
    }

    @Override
    protected Action[] getActions() {
        return Action.values();
    }

    @Override
    protected String getBeginEventType() {
        return OrganizationAddedUsers.BeginJobDomainEvent.TYPE;
    }

    @Override
    protected String getProcessedEventType() {
        return OrganizationAddedUsers.ProcessedJobDomainEvent.TYPE;
    }

    @Override
    protected String getFailedEventType() {
        return OrganizationAddedUsers.FailedJobDomainEvent.TYPE;
    }

    @Override
    protected String getSuccessfullyEventType() {
        return OrganizationAddedUsers.SuccessJobDomainEvent.TYPE;
    }

    private void validateOrganization(AddUsersOrganizationSagaState state)
            throws NotFoundOrganizationException {
        this.organizationLocalService.validateOrganizations(
                List.of(state.getInitialData().getOrganizationId())
        );

        this.localProcessedEventPublish(
                state, OrganizationServiceApplicationCode.SUCCESS, "Organization validated"
        );
    }

    private void handleNotFoundOrganizationException(
            AddUsersOrganizationSagaState state, NotFoundOrganizationException e
    ) {
        this.failureLocalExceptionPublish(state, e);
    }

    private void handleUserProfileExistValidateReply(
            AddUsersOrganizationSagaState state, UserProfileExistValidateReply.Success reply) {
        this.processedEventPublish(state, reply);
    }


    private void handleAddUsersOrganizationReply(
            AddUsersOrganizationSagaState state, AddUsersOrganizationReply.Success reply) {
        AddUsersOrganizationReply.Success.Data data = reply.getData();
        state.setAddedUsers(data.getAddedUsers());
        this.processedEventPublish(state, reply);
    }

    private void handleAddUsersDefaultTeamReply(
            AddUsersOrganizationSagaState state, AddUsersDefaultTeamReply.Success reply) {
        AddUsersDefaultTeamReply.Success.Data data = reply.getData();
        state.setAddedUsersOnTeam(data.getAddedUsers());
        this.processedEventPublish(state, reply);
    }

    @Override
    public void onSagaCompletedSuccessfully(String sagaId, AddUsersOrganizationSagaState data) {
        AddUsersOrganizationResultData resultData = new AddUsersOrganizationResultData(data.getAddedUsers());
        successfullyEventPublish(data, resultData);
    }

    public enum Action {
        VALIDATE_ORGANIZATION,
        VALIDATE_USER_PROFILE,
        ADD_ORGANIZATION_USER,
        ADD_DEFAULT_TEAM_USER
    }

    @Override
    public SagaDefinition<AddUsersOrganizationSagaState> getSagaDefinition() {
        return sagaDefinition;
    }

    @Override
    public String getSagaType() {
        return OrganizationSagaType.ADD_USERS_TO_ORGANIZATION;
    }

    @Override
    public String getSagaCommandSelfChannel() {
        return SagaCommandChannel.ORGANIZATION;
    }
}
