package org.cresplanex.api.state.organizationservice.repository;

import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.entity.OrganizationUserEntity;
import org.cresplanex.api.state.organizationservice.enums.OrganizationSortType;
import org.cresplanex.api.state.organizationservice.enums.OrganizationWithUsersSortType;
import org.cresplanex.api.state.organizationservice.enums.UserOnOrganizationSortType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, String>, JpaSpecificationExecutor<OrganizationEntity> {

    /**
     * Organizationを取得し、OrganizationUserをJOINした状態で取得。
     *
     * @param organizationId 組織ID
     * @return Organizationオプショナルオブジェクト
     */
    @Query("SELECT o FROM OrganizationEntity o LEFT JOIN FETCH o.organizationUsers WHERE o.organizationId = :organizationId")
    Optional<OrganizationEntity> findByIdWithUsers(String organizationId);

    /**
     * List<OrganizationId>の数を取得
     *
     * @param organizationIds 組織IDリスト
     * @return 組織IDの数
     */
    Optional<Long> countByOrganizationIdIn(List<String> organizationIds);

    @Query("SELECT o FROM OrganizationEntity o")
    List<OrganizationEntity> findList(Specification<OrganizationEntity> specification, Pageable pageable);

    @Query("SELECT o FROM OrganizationEntity o LEFT JOIN FETCH o.organizationUsers")
    List<OrganizationEntity> findListWithUsers(Specification<OrganizationEntity> specification, Pageable pageable);

    @Query("SELECT COUNT(o) FROM OrganizationEntity o")
    int countList(Specification<OrganizationEntity> specification);
}
