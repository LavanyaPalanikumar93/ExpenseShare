package com.lavanya.domain;

import static com.lavanya.domain.ExpenseTestSamples.*;
import static com.lavanya.domain.GroupTestSamples.*;
import static com.lavanya.domain.UserProfileTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.lavanya.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ExpenseTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Expense.class);
        Expense expense1 = getExpenseSample1();
        Expense expense2 = new Expense();
        assertThat(expense1).isNotEqualTo(expense2);

        expense2.setId(expense1.getId());
        assertThat(expense1).isEqualTo(expense2);

        expense2 = getExpenseSample2();
        assertThat(expense1).isNotEqualTo(expense2);
    }

    @Test
    void userTest() throws Exception {
        Expense expense = getExpenseRandomSampleGenerator();
        UserProfile userProfileBack = getUserProfileRandomSampleGenerator();

        expense.setUser(userProfileBack);
        assertThat(expense.getUser()).isEqualTo(userProfileBack);

        expense.user(null);
        assertThat(expense.getUser()).isNull();
    }

    @Test
    void groupTest() throws Exception {
        Expense expense = getExpenseRandomSampleGenerator();
        Group groupBack = getGroupRandomSampleGenerator();

        expense.setGroup(groupBack);
        assertThat(expense.getGroup()).isEqualTo(groupBack);

        expense.group(null);
        assertThat(expense.getGroup()).isNull();
    }
}
