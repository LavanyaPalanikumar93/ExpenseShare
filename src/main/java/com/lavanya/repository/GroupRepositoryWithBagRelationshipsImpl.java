package com.lavanya.repository;

import com.lavanya.domain.Group;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class GroupRepositoryWithBagRelationshipsImpl implements GroupRepositoryWithBagRelationships {

    private static final String ID_PARAMETER = "id";
    private static final String GROUPS_PARAMETER = "groups";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Group> fetchBagRelationships(Optional<Group> group) {
        return group.map(this::fetchMembers);
    }

    @Override
    public Page<Group> fetchBagRelationships(Page<Group> groups) {
        return new PageImpl<>(fetchBagRelationships(groups.getContent()), groups.getPageable(), groups.getTotalElements());
    }

    @Override
    public List<Group> fetchBagRelationships(List<Group> groups) {
        return Optional.of(groups).map(this::fetchMembers).orElse(Collections.emptyList());
    }

    Group fetchMembers(Group result) {
        return entityManager
            .createQuery("select group from Group group left join fetch group.members where group.id = :id", Group.class)
            .setParameter(ID_PARAMETER, result.getId())
            .getSingleResult();
    }

    List<Group> fetchMembers(List<Group> groups) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, groups.size()).forEach(index -> order.put(groups.get(index).getId(), index));
        List<Group> result = entityManager
            .createQuery("select group from Group group left join fetch group.members where group in :groups", Group.class)
            .setParameter(GROUPS_PARAMETER, groups)
            .getResultList();
        Collections.sort(result, (o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }
}
