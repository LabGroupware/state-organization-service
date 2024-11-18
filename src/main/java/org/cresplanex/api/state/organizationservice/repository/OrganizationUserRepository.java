package org.cresplanex.api.state.organizationservice.repository;

import org.cresplanex.api.state.organizationservice.entity.OrganizationUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationUserRepository extends JpaRepository<OrganizationUserEntity, String> {

    /**
     * 特定のorganizationIdに紐づくOrganizationUserEntityのリストを取得。
     *
     * @param organizationId 組織ID
     * @return OrganizationUserEntityのリスト
     */
    List<OrganizationUserEntity> findAllByOrganizationId(String organizationId);

    /**
     * 特定のuserIdに紐づくOrganizationUserEntityのリストを取得。
     *
     * @param userId ユーザーID
     * @return OrganizationUserEntityのリスト
     */
    List<OrganizationUserEntity> findAllByUserId(String userId);

    /**
     * OrganizationUserEntityを取得し、OrganizationをJOINした状態で取得。
     *
     * @param organizationUserId 組織ユーザーID
     * @return OrganizationUserEntityオプショナルオブジェクト
     */
    @Query("SELECT ou FROM OrganizationUserEntity ou JOIN FETCH ou.organization WHERE ou.organizationUserId = :organizationUserId")
    Optional<OrganizationUserEntity> findByIdWithOrganization(String organizationUserId);

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
