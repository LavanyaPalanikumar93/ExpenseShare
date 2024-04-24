package com.lavanya.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Group.
 */
@Entity
@Table(name = "jhi_group")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Group implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "admin_id")
    private Integer adminId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rel_jhi_group__members",
        joinColumns = @JoinColumn(name = "jhi_group_id"),
        inverseJoinColumns = @JoinColumn(name = "members_id")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "groups" }, allowSetters = true)
    private Set<UserProfile> members = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Group id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Group name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAdminId() {
        return this.adminId;
    }

    public Group adminId(Integer adminId) {
        this.setAdminId(adminId);
        return this;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public Set<UserProfile> getMembers() {
        return this.members;
    }

    public void setMembers(Set<UserProfile> userProfiles) {
        this.members = userProfiles;
    }

    public Group members(Set<UserProfile> userProfiles) {
        this.setMembers(userProfiles);
        return this;
    }

    public Group addMembers(UserProfile userProfile) {
        this.members.add(userProfile);
        return this;
    }

    public Group removeMembers(UserProfile userProfile) {
        this.members.remove(userProfile);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Group)) {
            return false;
        }
        return getId() != null && getId().equals(((Group) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Group{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", adminId=" + getAdminId() +
            "}";
    }
}
