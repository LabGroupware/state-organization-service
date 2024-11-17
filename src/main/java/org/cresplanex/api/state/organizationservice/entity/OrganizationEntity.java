package org.cresplanex.api.state.organizationservice.entity;

import jakarta.persistence.*;
import org.cresplanex.api.state.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.cresplanex.api.state.common.utils.OriginalAutoGenerate;
import org.hibernate.Hibernate;

import java.util.List;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "organizations",
    indexes = {@Index(name = "organizations_owner_id_index", columnList = "owner_id")
})
public class OrganizationEntity extends BaseEntity<OrganizationEntity> {

    @Override
    public void setId(String id) {
        this.organizationId = id;
    }

    @Override
    public String getId() {
        return this.organizationId;
    }

    @Id
    @OriginalAutoGenerate
    @Column(name = "organization_id", length = 100, nullable = false, unique = true)
    private String organizationId;

    @Column(name = "owner_id", length = 100)
    private String ownerId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "plan", length = 50, nullable = false)
    private String plan;

    @Column(name = "site_url")
    @Lob
    private String siteUrl;

    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrganizationUserEntity> organizationUsers;

    @Override
    public OrganizationEntity clone() {
        OrganizationEntity cloned = super.clone();

        // FetchされているもしくはすでにSetされている場合のみクローンを作成する
        if (this.organizationUsers != null && Hibernate.isInitialized(this.organizationUsers)) {
            cloned.organizationUsers = this.organizationUsers.stream()
                    .map(OrganizationUserEntity::clone)
                    .toList();
        }

        return cloned;
    }
}
