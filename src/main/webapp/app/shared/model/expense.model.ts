import { IUserProfile } from 'app/shared/model/user-profile.model';
import { IGroup } from 'app/shared/model/group.model';

export interface IExpense {
  id?: number;
  amount?: string | null;
  user?: IUserProfile | null;
  group?: IGroup | null;
}

export const defaultValue: Readonly<IExpense> = {};
