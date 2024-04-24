package com.lavanya.domain;

import static com.lavanya.domain.GroupTestSamples.*;
import static com.lavanya.domain.UserProfileTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.lavanya.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UserProfileTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(UserProfile.class);
        UserProfile userProfile1 = getUserProfileSample1();
        UserProfile userProfile2 = new UserProfile();
        assertThat(userProfile1).isNotEqualTo(userProfile2);

        userProfile2.setId(userProfile1.getId());
        assertThat(userProfile1).isEqualTo(userProfile2);

        userProfile2 = getUserProfileSample2();
        assertThat(userProfile1).isNotEqualTo(userProfile2);
    }

    @Test
    void groupsTest() throws Exception {
        UserProfile userProfile = getUserProfileRandomSampleGenerator();
        Group groupBack = getGroupRandomSampleGenerator();

        userProfile.addGroups(groupBack);
        assertThat(userProfile.getGroups()).containsOnly(groupBack);
        assertThat(groupBack.getMembers()).containsOnly(userProfile);

        userProfile.removeGroups(groupBack);
        assertThat(userProfile.getGroups()).doesNotContain(groupBack);
        assertThat(groupBack.getMembers()).doesNotContain(userProfile);

        userProfile.groups(new HashSet<>(Set.of(groupBack)));
        assertThat(userProfile.getGroups()).containsOnly(groupBack);
        assertThat(groupBack.getMembers()).containsOnly(userProfile);

        userProfile.setGroups(new HashSet<>());
        assertThat(userProfile.getGroups()).doesNotContain(groupBack);
        assertThat(groupBack.getMembers()).doesNotContain(userProfile);
    }
}
