package org.cresplanex.api.state.organizationservice.saga.model.organization;

import org.cresplanex.api.state.common.constants.OrganizationServiceApplicationCode;
import org.cresplanex.api.state.common.dto.organization.OrganizationWithUsersDto;
import org.cresplanex.api.state.common.event.model.organization.OrganizationCreated;
import org.cresplanex.api.state.common.event.model.organization.OrganizationDomainEvent;
import org.cresplanex.api.state.common.event.publisher.AggregateDomainEventPublisher;
import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.data.organization.CreateOrganizationResultData;
import org.cresplanex.api.state.common.saga.local.organization.InvalidOrganizationPlanException;
import org.cresplanex.api.state.common.saga.model.SagaModel;
import org.cresplanex.api.state.common.saga.reply.organization.CreateOrganizationAndAddInitialOrganizationUserReply;
import org.cresplanex.api.state.common.saga.reply.team.CreateDefaultTeamAndAddInitialDefaultTeamUserReply;
import org.cresplanex.api.state.common.saga.type.OrganizationSagaType;
import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.event.publisher.OrganizationDomainEventPublisher;
import org.cresplanex.api.state.organizationservice.saga.proxy.OrganizationServiceProxy;
import org.cresplanex.api.state.organizationservice.saga.proxy.TeamServiceProxy;
import org.cresplanex.api.state.organizationservice.saga.proxy.UserProfileServiceProxy;
import org.cresplanex.api.state.organizationservice.saga.state.organization.CreateOrganizationSagaState;
import org.cresplanex.api.state.organizationservice.service.OrganizationService;
import org.cresplanex.core.saga.orchestration.SagaDefinition;
import org.cresplanex.api.state.common.saga.reply.userprofile.UserExistValidateReply;
import org.springframework.stereotype.Component;

@Component
public class CreateOrganizationSaga extends SagaModel<
        OrganizationEntity,
        OrganizationDomainEvent,
        CreateOrganizationSaga.Action,
        CreateOrganizationSagaState> {

    private final SagaDefinition<CreateOrganizationSagaState> sagaDefinition;
    private final OrganizationDomainEventPublisher domainEventPublisher;
    private final OrganizationService organizationLocalService;

    public CreateOrganizationSaga(
            OrganizationService organizationLocalService,
            OrganizationServiceProxy organizationService,
            TeamServiceProxy teamService,
            UserProfileServiceProxy userProfileService,
            OrganizationDomainEventPublisher domainEventPublisher
    ) {
        this.sagaDefinition = step()
                .invokeLocal(this::validateOrganization)
                .onException(InvalidOrganizationPlanException.class, this::failureLocalExceptionPublish)
                .step()
                .invokeParticipant(
                        userProfileService.userExistValidate,
                        CreateOrganizationSagaState::makeUserExistValidateCommand
                )
                .onReply(
                        UserExistValidateReply.Success.class,
                        UserExistValidateReply.Success.TYPE,
                        this::processedEventPublish
                )
                .onReply(
                        UserExistValidateReply.Failure.class,
                        UserExistValidateReply.Failure.TYPE,
                        this::handleFailureReply
                )
                .step()
                .invokeParticipant(
                        organizationService.createOrganizationAndAddInitialOrganizationUser,
                        CreateOrganizationSagaState::makeCreateOrganizationAndAddInitialOrganizationUserCommand
                )
                .onReply(
                        CreateOrganizationAndAddInitialOrganizationUserReply.Success.class,
                        CreateOrganizationAndAddInitialOrganizationUserReply.Success.TYPE,
                        this::handleCreateOrganizationAndAddInitialOrganizationUserReply
                )
                .onReply(
                        CreateOrganizationAndAddInitialOrganizationUserReply.Failure.class,
                        CreateOrganizationAndAddInitialOrganizationUserReply.Failure.TYPE,
                        this::handleFailureReply
                )
                .withCompensation(
                        organizationService.undoCreateOrganizationAndAddInitialOrganizationUser,
                        CreateOrganizationSagaState::makeUndoCreateOrganizationAndAddInitialOrganizationUserCommand
                )
                .step()
                .invokeParticipant(
                        teamService.createDefaultTeamAndAddInitialDefaultTeamUser,
                        CreateOrganizationSagaState::makeCreateDefaultTeamAndAddInitialDefaultTeamUserCommand
                )
                .onReply(
                        CreateDefaultTeamAndAddInitialDefaultTeamUserReply.Success.class,
                        CreateDefaultTeamAndAddInitialDefaultTeamUserReply.Success.TYPE,
                        this::handleCreateDefaultTeamAndAddInitialDefaultTeamUser
                )
                .onReply(
                        CreateDefaultTeamAndAddInitialDefaultTeamUserReply.Failure.class,
                        CreateDefaultTeamAndAddInitialDefaultTeamUserReply.Failure.TYPE,
                        this::handleFailureReply
                )
                .withCompensation(
                        teamService.undoCreateDefaultTeamAndAddInitialDefaultTeamUser,
                        CreateOrganizationSagaState::makeUndoCreateDefaultTeamAndAddInitialDefaultTeamUserCommand
                )
                .build();
        this.domainEventPublisher = domainEventPublisher;
        this.organizationLocalService = organizationLocalService;
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
        return OrganizationCreated.BeginJobDomainEvent.TYPE;
    }

    @Override
    protected String getProcessedEventType() {
        return OrganizationCreated.ProcessedJobDomainEvent.TYPE;
    }

    @Override
    protected String getFailedEventType() {
        return OrganizationCreated.FailedJobDomainEvent.TYPE;
    }

    @Override
    protected String getSuccessfullyEventType() {
        return OrganizationCreated.SuccessJobDomainEvent.TYPE;
    }

    private void validateOrganization(CreateOrganizationSagaState state)
    throws InvalidOrganizationPlanException {
        this.organizationLocalService.validateCreatedOrganization(
                state.getInitialData().getName(),
                state.getInitialData().getPlan()
        );

        this.localProcessedEventPublish(
                state, OrganizationServiceApplicationCode.SUCCESS, "Organization validated"
        );
    }

    private void handleCreateOrganizationAndAddInitialOrganizationUserReply(
            CreateOrganizationSagaState state,
            CreateOrganizationAndAddInitialOrganizationUserReply.Success reply
    ) {
        CreateOrganizationAndAddInitialOrganizationUserReply.Success.Data data = reply.getData();
        OrganizationWithUsersDto organizationWithUsersDto =
                new OrganizationWithUsersDto(data.getOrganization(), data.getUsers());
        state.setOrganizationWithUsersDto(organizationWithUsersDto);
        this.processedEventPublish(state, reply);
    }

    private void handleCreateDefaultTeamAndAddInitialDefaultTeamUser(
            CreateOrganizationSagaState state, CreateDefaultTeamAndAddInitialDefaultTeamUserReply.Success reply) {
        CreateDefaultTeamAndAddInitialDefaultTeamUserReply.Success.Data data = reply.getData();
        state.setTeamDto(data.getTeam());
        this.processedEventPublish(state, reply);
    }

    @Override
    public void onSagaCompletedSuccessfully(String sagaId, CreateOrganizationSagaState data) {
        CreateOrganizationResultData resultData = new CreateOrganizationResultData(
                data.getOrganizationWithUsersDto(),
                data.getTeamDto()
        );
        successfullyEventPublish(data, resultData);
    }

    public enum Action {
        VALIDATE_ORGANIZATION,
        VALIDATE_USER,
        CREATE_ORGANIZATION_AND_ADD_INITIAL_ORGANIZATION_USER,
        CREATE_DEFAULT_TEAM_AND_ADD_INITIAL_DEFAULT_TEAM_USER
    }

    @Override
    public SagaDefinition<CreateOrganizationSagaState> getSagaDefinition() {
        return sagaDefinition;
    }

    @Override
    public String getSagaType() {
        return OrganizationSagaType.CREATE_ORGANIZATION;
    }

    @Override
    public String getSagaCommandSelfChannel() {
        return SagaCommandChannel.ORGANIZATION;
    }
}
