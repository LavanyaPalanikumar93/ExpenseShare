import { IUserProfile } from 'app/shared/model/user-profile.model';

export interface IGroup {
  id?: number;
  name?: string | null;
  adminId?: number | null;
  members?: IUserProfile[] | null;
}

export const defaultValue: Readonly<IGroup> = {};
