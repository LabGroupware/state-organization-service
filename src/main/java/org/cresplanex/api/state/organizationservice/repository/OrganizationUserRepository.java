package org.cresplanex.api.state.organizationservice.repository;

import org.cresplanex.api.state.organizationservice.entity.OrganizationUserEntity;
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
}
