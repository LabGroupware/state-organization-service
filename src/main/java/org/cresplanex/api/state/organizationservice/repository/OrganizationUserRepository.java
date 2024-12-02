package org.cresplanex.api.state.organizationservice.repository;

import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.entity.OrganizationUserEntity;
import org.cresplanex.api.state.organizationservice.enums.OrganizationOnUserSortType;
import org.cresplanex.api.state.organizationservice.enums.UserOnOrganizationSortType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationUserRepository extends JpaRepository<OrganizationUserEntity, String>, JpaSpecificationExecutor<OrganizationUserEntity> {

    /**
     * 特定のorganizationIdとuserIdsに紐づくOrganizationUserEntityのリストを取得。
     *
     * @param organizationId 組織ID
     * @param userIds ユーザーIDリスト
     * @return OrganizationUserEntityのリスト
     */
    @Query("SELECT ou FROM OrganizationUserEntity ou WHERE ou.organizationId = :organizationId AND ou.userId IN :userIds")
    List<OrganizationUserEntity> findAllByOrganizationIdAndUserIds(String organizationId, List<String> userIds);

    @Query("SELECT ou FROM OrganizationUserEntity ou")
    List<OrganizationUserEntity> findList(Specification<OrganizationUserEntity> specification, Pageable pageable);

    @Query("SELECT ou FROM OrganizationUserEntity ou JOIN FETCH ou.organization")
    List<OrganizationUserEntity> findListWithOrganization(Specification<OrganizationUserEntity> specification, Pageable pageable);

    @Query("SELECT COUNT(ou) FROM OrganizationUserEntity ou")
    int countList(Specification<OrganizationUserEntity> specification);
}
