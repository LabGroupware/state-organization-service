package org.cresplanex.api.state.organizationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cresplanex.api.state.common.entity.BaseEntity;
import org.cresplanex.api.state.common.utils.OriginalAutoGenerate;
import org.hibernate.Hibernate;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "organization_user", indexes = {
        @Index(name = "organization_user_organization_id_index", columnList = "organization_id"),
        @Index(name = "organization_user_user_id_index", columnList = "user_id"),
        @Index(name = "organization_user_organization_id_user_id_index", columnList = "organization_id, user_id", unique = true)
})
public class OrganizationUserEntity extends BaseEntity<OrganizationUserEntity> {

    @Override
    public void setId(String id) {
        this.organizationUserId = id;
    }

    @Override
    public String getId() {
        return this.organizationUserId;
    }

    @Id
    @OriginalAutoGenerate
    @Column(name = "organization_user_id", length = 100, nullable = false, unique = true)
    private String organizationUserId;

    @Column(name = "organization_id", length = 100, nullable = false,
            insertable = false, updatable = false)
    private String organizationId;

    @Column(name = "user_id", length = 100, nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organization;

    @Override
    public OrganizationUserEntity clone() {
        OrganizationUserEntity cloned = super.clone();
        // FetchされているもしくはすでにSetされている場合のみクローンを作成する
        if (this.organization != null && Hibernate.isInitialized(this.organization)) {
            cloned.organization = this.organization.clone();
        }

        return cloned;
    }
}
