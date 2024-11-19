package org.cresplanex.api.state.organizationservice.repository;

import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.enums.OrganizationSortType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
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
     * List<OrganizationId>を取得し, OrganizationUserをJOINした状態で取得。
     *
     * @param organizationIds 組織IDリスト
     * @return OrganizationEntityのリスト
     */
    @Query("SELECT o FROM OrganizationEntity o LEFT JOIN FETCH o.organizationUsers WHERE o.organizationId IN :organizationIds")
    List<OrganizationEntity> findAllByIdWithUsers(List<String> organizationIds);

    /**
     * Owner IDでOrganizationを取得。
     *
     * @param ownerId オーナーID
     * @return Organizationのリスト
     */
    Optional<OrganizationEntity> findByOwnerId(String ownerId);

    /**
     * List<OrganizationId>の数を取得
     *
     * @param organizationIds 組織IDリスト
     * @return 組織IDの数
     */
    Optional<Long> countByOrganizationIdIn(List<String> organizationIds);

    @Query("SELECT o FROM OrganizationEntity o WHERE o.organizationId IN :organization ORDER BY " +
            "CASE WHEN :sortType = 'CREATED_AT_ASC' THEN o.createdAt END ASC, " +
            "CASE WHEN :sortType = 'CREATED_AT_DESC' THEN o.createdAt END DESC")
    List<OrganizationEntity> findListByOrganizationIds(List<String> organizationIds, OrganizationSortType sortType);

    @Query("SELECT o FROM OrganizationEntity o ORDER BY " +
            "CASE WHEN :sortType = 'CREATED_AT_ASC' THEN o.createdAt END ASC, " +
            "CASE WHEN :sortType = 'CREATED_AT_DESC' THEN o.createdAt END DESC, " +
            "CASE WHEN :sortType = 'NAME_ASC' THEN o.name END ASC, " +
            "CASE WHEN :sortType = 'NAME_DESC' THEN o.name END DESC")
    List<OrganizationEntity> findList(Specification<OrganizationEntity> specification, OrganizationSortType sortType);

    @Query("SELECT o FROM OrganizationEntity o ORDER BY " +
            "CASE WHEN :sortType = 'CREATED_AT_ASC' THEN o.createdAt END ASC, " +
            "CASE WHEN :sortType = 'CREATED_AT_DESC' THEN o.createdAt END DESC, " +
            "CASE WHEN :sortType = 'NAME_ASC' THEN o.name END ASC, " +
            "CASE WHEN :sortType = 'NAME_DESC' THEN o.name END DESC")
    List<OrganizationEntity> findListWithOffsetPagination(Specification<OrganizationEntity> specification, OrganizationSortType sortType, Pageable pageable);

    @Query("SELECT COUNT(o) FROM OrganizationEntity o")
    int countList(Specification<OrganizationEntity> specification);
}
