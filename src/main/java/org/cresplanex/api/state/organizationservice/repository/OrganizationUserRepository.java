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

    @Query("SELECT ou FROM OrganizationUserEntity ou WHERE ou.organizationId = :organizationId ORDER BY " +
            "CASE WHEN :sortType = 'ADD_AT_ASC' THEN ou.createdAt END ASC, " +
            "CASE WHEN :sortType = 'ADD_AT_DESC' THEN ou.createdAt END DESC")
    List<OrganizationUserEntity> findUsersListOnOrganization(Specification<OrganizationEntity> specification, String organizationId, UserOnOrganizationSortType sortType);

    @Query("SELECT ou FROM OrganizationUserEntity ou WHERE ou.organizationId = :organizationId ORDER BY " +
            "CASE WHEN :sortType = 'ADD_AT_ASC' THEN ou.createdAt END ASC, " +
            "CASE WHEN :sortType = 'ADD_AT_DESC' THEN ou.createdAt END DESC")
    List<OrganizationUserEntity> findUsersListOnOrganizationWithOffsetPagination(Specification<OrganizationEntity> specification, String organizationId, UserOnOrganizationSortType sortType, Pageable pageable);

    @Query("SELECT ou FROM OrganizationUserEntity ou JOIN FETCH ou.organization WHERE ou.userId = :userId ORDER BY " +
            "CASE WHEN :sortType = 'ADD_AT_ASC' THEN ou.createdAt END ASC, " +
            "CASE WHEN :sortType = 'ADD_AT_DESC' THEN ou.createdAt END DESC, " +
            "CASE WHEN :sortType = 'NAME_ASC' THEN ou.organization.name END ASC, " +
            "CASE WHEN :sortType = 'NAME_DESC' THEN ou.organization.name END DESC, " +
            "CASE WHEN :sortType = 'CREATED_AT_ASC' THEN ou.organization.createdAt END ASC, " +
            "CASE WHEN :sortType = 'CREATED_AT_DESC' THEN ou.organization.createdAt END DESC")
    List<OrganizationUserEntity> findOrganizationsOnUser(Specification<OrganizationEntity> specification, String userId, OrganizationOnUserSortType sortType);

    @Query("SELECT ou FROM OrganizationUserEntity ou JOIN FETCH ou.organization WHERE ou.userId = :userId ORDER BY " +
            "CASE WHEN :sortType = 'ADD_AT_ASC' THEN ou.createdAt END ASC, " +
            "CASE WHEN :sortType = 'ADD_AT_DESC' THEN ou.createdAt END DESC, " +
            "CASE WHEN :sortType = 'NAME_ASC' THEN ou.organization.name END ASC, " +
            "CASE WHEN :sortType = 'NAME_DESC' THEN ou.organization.name END DESC, " +
            "CASE WHEN :sortType = 'CREATED_AT_ASC' THEN ou.organization.createdAt END ASC, " +
            "CASE WHEN :sortType = 'CREATED_AT_DESC' THEN ou.organization.createdAt END DESC")
    List<OrganizationUserEntity> findOrganizationsOnUserWithOffsetPagination(Specification<OrganizationEntity> specification, String userId, OrganizationOnUserSortType sortType, Pageable pageable);

    @Query("SELECT COUNT(ou) FROM OrganizationUserEntity ou WHERE ou.organizationId = :organizationId")
    int countUsersListOnOrganization(Specification<OrganizationEntity> specification, String organizationId);

    @Query("SELECT COUNT(ou) FROM OrganizationUserEntity ou WHERE ou.userId = :userId")
    int countOrganizationsOnUser(Specification<OrganizationEntity> specification, String userId);
}
