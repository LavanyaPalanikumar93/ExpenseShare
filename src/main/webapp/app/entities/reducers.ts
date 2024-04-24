import userProfile from 'app/entities/user-profile/user-profile.reducer';
import group from 'app/entities/group/group.reducer';
import expense from 'app/entities/expense/expense.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const entitiesReducers = {
  userProfile,
  group,
  expense,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
};

export default entitiesReducers;
