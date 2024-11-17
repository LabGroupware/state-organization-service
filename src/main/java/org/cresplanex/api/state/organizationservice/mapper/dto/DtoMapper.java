package org.cresplanex.api.state.organizationservice.mapper.dto;

import org.cresplanex.api.state.common.dto.organization.OrganizationDto;
import org.cresplanex.api.state.common.dto.organization.OrganizationWithUsersDto;
import org.cresplanex.api.state.common.dto.organization.UserOnOrganizationDto;
import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.entity.OrganizationUserEntity;

import java.util.List;

public class DtoMapper {

    public static OrganizationDto convert(OrganizationEntity organizationEntity) {
        return OrganizationDto.builder()
                .organizationId(organizationEntity.getOrganizationId())
                .ownerId(organizationEntity.getOwnerId())
                .name(organizationEntity.getName())
                .plan(organizationEntity.getPlan())
                .siteUrl(organizationEntity.getSiteUrl())
                .build();
    }

    public static UserOnOrganizationDto convert(OrganizationUserEntity organizationUserEntity) {
        return UserOnOrganizationDto.builder()
                .userOrganizationId(organizationUserEntity.getOrganizationUserId())
                .userOrganizationId(organizationUserEntity.getOrganizationId())
                .userId(organizationUserEntity.getUserId())
                .build();
    }

    public static List<UserOnOrganizationDto> convert(List<OrganizationUserEntity> organizationUserEntities) {
        return organizationUserEntities.stream()
                .map(DtoMapper::convert)
                .toList();
    }

    public static OrganizationWithUsersDto convert(OrganizationEntity organizationEntity, List<OrganizationUserEntity> organizationUserEntities) {
        return OrganizationWithUsersDto.builder()
                .organization(DtoMapper.convert(organizationEntity))
                .users(convert(organizationUserEntities))
                .build();
    }
}
