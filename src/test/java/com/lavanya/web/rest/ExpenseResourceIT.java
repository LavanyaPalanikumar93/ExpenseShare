package com.lavanya.web.rest;

import static com.lavanya.domain.ExpenseAsserts.*;
import static com.lavanya.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lavanya.IntegrationTest;
import com.lavanya.domain.Expense;
import com.lavanya.repository.ExpenseRepository;
import jakarta.persistence.EntityManager;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ExpenseResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ExpenseResourceIT {

    private static final String DEFAULT_AMOUNT = "AAAAAAAAAA";
    private static final String UPDATED_AMOUNT = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/expenses";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restExpenseMockMvc;

    private Expense expense;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Expense createEntity(EntityManager em) {
        Expense expense = new Expense().amount(DEFAULT_AMOUNT);
        return expense;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Expense createUpdatedEntity(EntityManager em) {
        Expense expense = new Expense().amount(UPDATED_AMOUNT);
        return expense;
    }

    @BeforeEach
    public void initTest() {
        expense = createEntity(em);
    }

    @Test
    @Transactional
    void createExpense() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Expense
        var returnedExpense = om.readValue(
            restExpenseMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(expense)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Expense.class
        );

        // Validate the Expense in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertExpenseUpdatableFieldsEquals(returnedExpense, getPersistedExpense(returnedExpense));
    }

    @Test
    @Transactional
    void createExpenseWithExistingId() throws Exception {
        // Create the Expense with an existing ID
        expense.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restExpenseMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(expense)))
            .andExpect(status().isBadRequest());

        // Validate the Expense in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllExpenses() throws Exception {
        // Initialize the database
        expenseRepository.saveAndFlush(expense);

        // Get all the expenseList
        restExpenseMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(expense.getId().intValue())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT)));
    }

    @Test
    @Transactional
    void getExpense() throws Exception {
        // Initialize the database
        expenseRepository.saveAndFlush(expense);

        // Get the expense
        restExpenseMockMvc
            .perform(get(ENTITY_API_URL_ID, expense.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(expense.getId().intValue()))
            .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT));
    }

    @Test
    @Transactional
    void getNonExistingExpense() throws Exception {
        // Get the expense
        restExpenseMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingExpense() throws Exception {
        // Initialize the database
        expenseRepository.saveAndFlush(expense);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the expense
        Expense updatedExpense = expenseRepository.findById(expense.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedExpense are not directly saved in db
        em.detach(updatedExpense);
        updatedExpense.amount(UPDATED_AMOUNT);

        restExpenseMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedExpense.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedExpense))
            )
            .andExpect(status().isOk());

        // Validate the Expense in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedExpenseToMatchAllProperties(updatedExpense);
    }

    @Test
    @Transactional
    void putNonExistingExpense() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        expense.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restExpenseMockMvc
            .perform(put(ENTITY_API_URL_ID, expense.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(expense)))
            .andExpect(status().isBadRequest());

        // Validate the Expense in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchExpense() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        expense.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExpenseMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(expense))
            )
            .andExpect(status().isBadRequest());

        // Validate the Expense in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamExpense() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        expense.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExpenseMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(expense)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Expense in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateExpenseWithPatch() throws Exception {
        // Initialize the database
        expenseRepository.saveAndFlush(expense);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the expense using partial update
        Expense partialUpdatedExpense = new Expense();
        partialUpdatedExpense.setId(expense.getId());

        partialUpdatedExpense.amount(UPDATED_AMOUNT);

        restExpenseMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedExpense.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedExpense))
            )
            .andExpect(status().isOk());

        // Validate the Expense in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertExpenseUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedExpense, expense), getPersistedExpense(expense));
    }

    @Test
    @Transactional
    void fullUpdateExpenseWithPatch() throws Exception {
        // Initialize the database
        expenseRepository.saveAndFlush(expense);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the expense using partial update
        Expense partialUpdatedExpense = new Expense();
        partialUpdatedExpense.setId(expense.getId());

        partialUpdatedExpense.amount(UPDATED_AMOUNT);

        restExpenseMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedExpense.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedExpense))
            )
            .andExpect(status().isOk());

        // Validate the Expense in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertExpenseUpdatableFieldsEquals(partialUpdatedExpense, getPersistedExpense(partialUpdatedExpense));
    }

    @Test
    @Transactional
    void patchNonExistingExpense() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        expense.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restExpenseMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, expense.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(expense))
            )
            .andExpect(status().isBadRequest());

        // Validate the Expense in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchExpense() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        expense.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExpenseMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(expense))
            )
            .andExpect(status().isBadRequest());

        // Validate the Expense in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamExpense() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        expense.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExpenseMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(expense)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Expense in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteExpense() throws Exception {
        // Initialize the database
        expenseRepository.saveAndFlush(expense);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the expense
        restExpenseMockMvc
            .perform(delete(ENTITY_API_URL_ID, expense.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return expenseRepository.count();
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

    protected Expense getPersistedExpense(Expense expense) {
        return expenseRepository.findById(expense.getId()).orElseThrow();
    }

    protected void assertPersistedExpenseToMatchAllProperties(Expense expectedExpense) {
        assertExpenseAllPropertiesEquals(expectedExpense, getPersistedExpense(expectedExpense));
    }

    protected void assertPersistedExpenseToMatchUpdatableProperties(Expense expectedExpense) {
        assertExpenseAllUpdatablePropertiesEquals(expectedExpense, getPersistedExpense(expectedExpense));
    }
}
