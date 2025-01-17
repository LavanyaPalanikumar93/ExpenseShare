import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import {} from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './expense.reducer';

export const ExpenseDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const expenseEntity = useAppSelector(state => state.expense.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="expenseDetailsHeading">Expense</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{expenseEntity.id}</dd>
          <dt>
            <span id="amount">Amount</span>
          </dt>
          <dd>{expenseEntity.amount}</dd>
          <dt>User</dt>
          <dd>{expenseEntity.user ? expenseEntity.user.id : ''}</dd>
          <dt>Group</dt>
          <dd>{expenseEntity.group ? expenseEntity.group.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/expense" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/expense/${expenseEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default ExpenseDetail;
