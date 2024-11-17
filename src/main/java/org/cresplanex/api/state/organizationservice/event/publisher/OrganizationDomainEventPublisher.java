package org.cresplanex.api.state.organizationservice.event.publisher;

import org.cresplanex.api.state.common.event.EventAggregateType;
import org.cresplanex.api.state.common.event.model.organization.OrganizationDomainEvent;
import org.cresplanex.api.state.common.event.publisher.AggregateDomainEventPublisher;
import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.core.events.publisher.DomainEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class OrganizationDomainEventPublisher extends AggregateDomainEventPublisher<OrganizationEntity, OrganizationDomainEvent> {

    public OrganizationDomainEventPublisher(DomainEventPublisher eventPublisher) {
        super(eventPublisher, OrganizationEntity.class, EventAggregateType.ORGANIZATION);
    }
}
