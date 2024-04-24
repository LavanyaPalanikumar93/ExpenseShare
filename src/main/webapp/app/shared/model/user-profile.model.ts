import { IGroup } from 'app/shared/model/group.model';

export interface IUserProfile {
  id?: number;
  email?: string | null;
  groups?: IGroup[] | null;
}

export const defaultValue: Readonly<IUserProfile> = {};
