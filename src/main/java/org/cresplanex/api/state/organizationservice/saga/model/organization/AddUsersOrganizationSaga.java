package org.cresplanex.api.state.organizationservice.saga.model.organization;

import org.cresplanex.api.state.common.event.model.userpreference.UserPreferenceDomainEvent;
import org.cresplanex.api.state.common.event.model.userpreference.UserPreferenceUpdated;
import org.cresplanex.api.state.common.event.publisher.AggregateDomainEventPublisher;
import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.data.userpreference.UpdateUserPreferenceResultData;
import org.cresplanex.api.state.common.saga.model.SagaModel;
import org.cresplanex.api.state.common.saga.reply.userpreference.UpdateUserPreferenceReply;
import org.cresplanex.api.state.common.saga.type.UserPreferenceSagaType;
import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.event.publisher.OrganizationDomainEventPublisher;
import org.cresplanex.api.state.organizationservice.saga.proxy.UserProfileServiceProxy;
import org.cresplanex.core.saga.orchestration.SagaDefinition;
import org.springframework.stereotype.Component;

@Component
public class AddUsersOrganizationSaga extends SagaModel<
        OrganizationEntity,
        UserPreferenceDomainEvent,
        AddUsersOrganizationSaga.Action,
        UpdateUserPreferenceSagaState> {

    private final SagaDefinition<UpdateUserPreferenceSagaState> sagaDefinition;
    private final OrganizationDomainEventPublisher domainEventPublisher;

    public AddUsersOrganizationSaga(
            UserProfileServiceProxy userPreferenceService,
            OrganizationDomainEventPublisher domainEventPublisher
    ) {
        this.sagaDefinition = step()
                .invokeParticipant(
                        userPreferenceService.updateUserPreference,
                        UpdateUserPreferenceSagaState::makeUpdateUserPreferenceCommand
                )
                .onReply(
                        UpdateUserPreferenceReply.Success.class,
                        UpdateUserPreferenceReply.Success.TYPE,
                        this::handleUpdateUserPreferenceReply
                )
                .onReply(
                        UpdateUserPreferenceReply.Failure.class,
                        UpdateUserPreferenceReply.Failure.TYPE,
                        this::handleFailureReply
                )
                .withCompensation(
                        userPreferenceService.undoUpdateUserPreference,
                        UpdateUserPreferenceSagaState::makeUndoUpdateUserPreferenceCommand
                )
                .build();
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    protected AggregateDomainEventPublisher<OrganizationEntity, UserPreferenceDomainEvent>
    getDomainEventPublisher() {
        return domainEventPublisher;
    }

    @Override
    protected Action[] getActions() {
        return Action.values();
    }

    @Override
    protected String getBeginEventType() {
        return UserPreferenceUpdated.BeginJobDomainEvent.TYPE;
    }

    @Override
    protected String getProcessedEventType() {
        return UserPreferenceUpdated.ProcessedJobDomainEvent.TYPE;
    }

    @Override
    protected String getFailedEventType() {
        return UserPreferenceUpdated.FailedJobDomainEvent.TYPE;
    }

    @Override
    protected String getSuccessfullyEventType() {
        return UserPreferenceUpdated.SuccessJobDomainEvent.TYPE;
    }

    private void handleUpdateUserPreferenceReply(
            UpdateUserPreferenceSagaState state, UpdateUserPreferenceReply.Success reply) {
        UpdateUserPreferenceReply.Success.Data data = reply.getData();
        state.setUserPreferenceDto(data.getUserPreference());
        state.setPrevUserPreferenceDto(data.getPrevUserPreference());
        this.processedEventPublish(state, reply);
    }

    @Override
    public void onSagaCompletedSuccessfully(String sagaId, UpdateUserPreferenceSagaState data) {
        UpdateUserPreferenceResultData resultData = new UpdateUserPreferenceResultData(data.getPrevUserPreferenceDto());
        successfullyEventPublish(data, resultData);
    }

    public enum Action {
        UPDATE_USER_PREFERENCE
    }

    @Override
    public SagaDefinition<UpdateUserPreferenceSagaState> getSagaDefinition() {
        return sagaDefinition;
    }

    @Override
    public String getSagaType() {
        return UserPreferenceSagaType.UPDATE_USER_PREFERENCE;
    }

    @Override
    public String getSagaCommandSelfChannel() {
        return SagaCommandChannel.USER_PREFERENCE;
    }
}
