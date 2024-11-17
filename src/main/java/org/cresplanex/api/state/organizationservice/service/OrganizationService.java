package org.cresplanex.api.state.organizationservice.service;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.common.saga.local.exception.organization.InvalidOrganizationPlanException;
import org.cresplanex.api.state.common.service.BaseService;
import org.cresplanex.api.state.organizationservice.constants.PlanType;
import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.exception.OrganizationNotFoundException;
import org.cresplanex.api.state.organizationservice.repository.OrganizationRepository;
import org.cresplanex.api.state.organizationservice.repository.OrganizationUserRepository;
import org.cresplanex.api.state.organizationservice.saga.model.organization.AddUsersOrganizationSaga;
import org.cresplanex.api.state.organizationservice.saga.model.organization.CreateOrganizationSaga;
import org.cresplanex.core.saga.orchestration.SagaInstanceFactory;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrganizationService extends BaseService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationUserRepository organizationUserRepository;
    private final SagaInstanceFactory sagaInstanceFactory;

    private final CreateOrganizationSaga createOrganizationSaga;
    private final AddUsersOrganizationSaga addUsersOrganizationSaga;

    @Transactional(readOnly = true)
    public OrganizationEntity findById(String organizationId) {
        return internalFindById(organizationId);
    }

    private OrganizationEntity internalFindById(String organizationId) {
        return organizationRepository.findById(organizationId).orElseThrow(() -> new OrganizationNotFoundException(
                OrganizationNotFoundException.FindType.BY_ID,
                organizationId
        ));
    }

    @Transactional(readOnly = true)
    public List<OrganizationEntity> get() {
        return organizationRepository.findAll();
    }

    @Transactional
    public String beginCreate(String operatorId, OrganizationEntity organization) {
        CreateOrganizationSagaState.InitialData initialData = CreateOrganizationSagaState.InitialData.builder()
                .organizationId(organization.getUserPreferenceId())
                .language(organization.getLanguage())
                .theme(organization.getTheme())
                .timezone(organization.getTimezone())
                .build();
        CreateOrganizationSagaState state = new CreateOrganizationSagaState();
        state.setInitialData(initialData);
        state.setOperatorId(operatorId);

        String jobId = getJobId();
        state.setJobId(jobId);

        // 一つ目がlocalStepであるため, 一つ目のLocal Exceptionが発生する場合,
        // ここで処理する必要がある.
        // ただし, rollbackExceptionに登録する必要がある.
        sagaInstanceFactory.create(createOrganizationSaga, state);

        return jobId;
    }

    public void validateCreatedOrganization(String name, String plan)
            throws InvalidOrganizationPlanException {
        if (!Arrays.asList(PlanType.ALL).contains(plan)) {
            throw new InvalidOrganizationPlanException(plan);
        }
    }

    public OrganizationEntity create(OrganizationEntity organization) {
        return organizationRepository.save(organization);
    }

    public void undoCreate(String organizationId) {
        OrganizationEntity organization = internalFindById(organizationId);
        organizationRepository.delete(organization);
    }
}
