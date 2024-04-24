package com.lavanya.domain;

import static com.lavanya.domain.GroupTestSamples.*;
import static com.lavanya.domain.UserProfileTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.lavanya.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GroupTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Group.class);
        Group group1 = getGroupSample1();
        Group group2 = new Group();
        assertThat(group1).isNotEqualTo(group2);

        group2.setId(group1.getId());
        assertThat(group1).isEqualTo(group2);

        group2 = getGroupSample2();
        assertThat(group1).isNotEqualTo(group2);
    }

    @Test
    void membersTest() throws Exception {
        Group group = getGroupRandomSampleGenerator();
        UserProfile userProfileBack = getUserProfileRandomSampleGenerator();

        group.addMembers(userProfileBack);
        assertThat(group.getMembers()).containsOnly(userProfileBack);

        group.removeMembers(userProfileBack);
        assertThat(group.getMembers()).doesNotContain(userProfileBack);

        group.members(new HashSet<>(Set.of(userProfileBack)));
        assertThat(group.getMembers()).containsOnly(userProfileBack);

        group.setMembers(new HashSet<>());
        assertThat(group.getMembers()).doesNotContain(userProfileBack);
    }
}
