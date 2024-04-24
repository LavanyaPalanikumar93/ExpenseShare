package com.lavanya.web.rest;

import static com.lavanya.domain.GroupAsserts.*;
import static com.lavanya.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lavanya.IntegrationTest;
import com.lavanya.domain.Group;
import com.lavanya.repository.GroupRepository;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link GroupResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class GroupResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Integer DEFAULT_ADMIN_ID = 1;
    private static final Integer UPDATED_ADMIN_ID = 2;

    private static final String ENTITY_API_URL = "/api/groups";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private GroupRepository groupRepository;

    @Mock
    private GroupRepository groupRepositoryMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restGroupMockMvc;

    private Group group;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Group createEntity(EntityManager em) {
        Group group = new Group().name(DEFAULT_NAME).adminId(DEFAULT_ADMIN_ID);
        return group;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Group createUpdatedEntity(EntityManager em) {
        Group group = new Group().name(UPDATED_NAME).adminId(UPDATED_ADMIN_ID);
        return group;
    }

    @BeforeEach
    public void initTest() {
        group = createEntity(em);
    }

    @Test
    @Transactional
    void createGroup() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Group
        var returnedGroup = om.readValue(
            restGroupMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(group)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Group.class
        );

        // Validate the Group in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertGroupUpdatableFieldsEquals(returnedGroup, getPersistedGroup(returnedGroup));
    }

    @Test
    @Transactional
    void createGroupWithExistingId() throws Exception {
        // Create the Group with an existing ID
        group.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restGroupMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(group)))
            .andExpect(status().isBadRequest());

        // Validate the Group in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllGroups() throws Exception {
        // Initialize the database
        groupRepository.saveAndFlush(group);

        // Get all the groupList
        restGroupMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(group.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].adminId").value(hasItem(DEFAULT_ADMIN_ID)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllGroupsWithEagerRelationshipsIsEnabled() throws Exception {
        when(groupRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restGroupMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(groupRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllGroupsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(groupRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restGroupMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(groupRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getGroup() throws Exception {
        // Initialize the database
        groupRepository.saveAndFlush(group);

        // Get the group
        restGroupMockMvc
            .perform(get(ENTITY_API_URL_ID, group.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(group.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.adminId").value(DEFAULT_ADMIN_ID));
    }

    @Test
    @Transactional
    void getNonExistingGroup() throws Exception {
        // Get the group
        restGroupMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingGroup() throws Exception {
        // Initialize the database
        groupRepository.saveAndFlush(group);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the group
        Group updatedGroup = groupRepository.findById(group.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedGroup are not directly saved in db
        em.detach(updatedGroup);
        updatedGroup.name(UPDATED_NAME).adminId(UPDATED_ADMIN_ID);

        restGroupMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedGroup.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedGroup))
            )
            .andExpect(status().isOk());

        // Validate the Group in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedGroupToMatchAllProperties(updatedGroup);
    }

    @Test
    @Transactional
    void putNonExistingGroup() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        group.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGroupMockMvc
            .perform(put(ENTITY_API_URL_ID, group.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(group)))
            .andExpect(status().isBadRequest());

        // Validate the Group in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchGroup() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        group.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGroupMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(group))
            )
            .andExpect(status().isBadRequest());

        // Validate the Group in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamGroup() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        group.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGroupMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(group)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Group in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateGroupWithPatch() throws Exception {
        // Initialize the database
        groupRepository.saveAndFlush(group);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the group using partial update
        Group partialUpdatedGroup = new Group();
        partialUpdatedGroup.setId(group.getId());

        partialUpdatedGroup.name(UPDATED_NAME);

        restGroupMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGroup.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGroup))
            )
            .andExpect(status().isOk());

        // Validate the Group in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertGroupUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedGroup, group), getPersistedGroup(group));
    }

    @Test
    @Transactional
    void fullUpdateGroupWithPatch() throws Exception {
        // Initialize the database
        groupRepository.saveAndFlush(group);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the group using partial update
        Group partialUpdatedGroup = new Group();
        partialUpdatedGroup.setId(group.getId());

        partialUpdatedGroup.name(UPDATED_NAME).adminId(UPDATED_ADMIN_ID);

        restGroupMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGroup.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGroup))
            )
            .andExpect(status().isOk());

        // Validate the Group in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertGroupUpdatableFieldsEquals(partialUpdatedGroup, getPersistedGroup(partialUpdatedGroup));
    }

    @Test
    @Transactional
    void patchNonExistingGroup() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        group.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGroupMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, group.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(group))
            )
            .andExpect(status().isBadRequest());

        // Validate the Group in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchGroup() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        group.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGroupMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(group))
            )
            .andExpect(status().isBadRequest());

        // Validate the Group in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamGroup() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        group.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGroupMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(group)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Group in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteGroup() throws Exception {
        // Initialize the database
        groupRepository.saveAndFlush(group);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the group
        restGroupMockMvc
            .perform(delete(ENTITY_API_URL_ID, group.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return groupRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Group getPersistedGroup(Group group) {
        return groupRepository.findById(group.getId()).orElseThrow();
    }

    protected void assertPersistedGroupToMatchAllProperties(Group expectedGroup) {
        assertGroupAllPropertiesEquals(expectedGroup, getPersistedGroup(expectedGroup));
    }

    protected void assertPersistedGroupToMatchUpdatableProperties(Group expectedGroup) {
        assertGroupAllUpdatablePropertiesEquals(expectedGroup, getPersistedGroup(expectedGroup));
    }
}
