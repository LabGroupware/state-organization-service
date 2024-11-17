package org.cresplanex.api.state.organizationservice.repository;

import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, String> {

    /**
     * Organizationを取得し、OrganizationUserをJOINした状態で取得。
     *
     * @param organizationId 組織ID
     * @return Organizationオプショナルオブジェクト
     */
    @Query("SELECT o FROM OrganizationEntity o LEFT JOIN FETCH o.organizationUsers WHERE o.organizationId = :organizationId")
    Optional<OrganizationEntity> findByIdWithUsers(String organizationId);

    /**
     * Owner IDでOrganizationを取得。
     *
     * @param ownerId オーナーID
     * @return Organizationのリスト
     */
    @Query("SELECT o FROM OrganizationEntity o WHERE o.ownerId = :ownerId")
    Optional<OrganizationEntity> findByOwnerId(String ownerId);
}
