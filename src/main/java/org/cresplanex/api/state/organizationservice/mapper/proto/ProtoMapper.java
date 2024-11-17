package org.cresplanex.api.state.organizationservice.mapper.proto;

import build.buf.gen.organization.v1.Organization;
import build.buf.gen.organization.v1.OrganizationWithUsers;
import build.buf.gen.organization.v1.UserOnOrganization;
import org.cresplanex.api.state.common.utils.ValueFromNullable;
import org.cresplanex.api.state.organizationservice.entity.OrganizationEntity;
import org.cresplanex.api.state.organizationservice.entity.OrganizationUserEntity;

import java.util.List;

public class ProtoMapper {

    public static Organization convert(OrganizationEntity organizationEntity) {

        return Organization.newBuilder()
                .setOrganizationId(organizationEntity.getOrganizationId())
                .setOwnerId(organizationEntity.getOwnerId())
                .setName(organizationEntity.getName())
                .setPlan(organizationEntity.getPlan())
                .setSiteUrl(ValueFromNullable.toNullableString(organizationEntity.getSiteUrl()))
                .build();
    }

    public static UserOnOrganization convert(OrganizationUserEntity userOnOrganizationEntity) {
        return UserOnOrganization.newBuilder()
                .setUserId(userOnOrganizationEntity.getUserId())
                .build();
    }

    public static List<UserOnOrganization> convert(List<OrganizationUserEntity> userOnOrganizationEntities) {
        return userOnOrganizationEntities.stream()
                .map(ProtoMapper::convert)
                .toList();
    }

    public static OrganizationWithUsers convert(OrganizationEntity organizationEntity, List<OrganizationUserEntity> userOnOrganizationEntities) {
        return OrganizationWithUsers.newBuilder()
                .setOrganization(convert(organizationEntity))
                .addAllUsers(convert(userOnOrganizationEntities))
                .build();
    }
}
