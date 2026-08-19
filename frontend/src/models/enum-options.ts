import {CreateOwnerRequest} from '@/api/backend/models/create-owner-request';

export interface EnumOption<T> {
  value: T;
  i18n: string;
}

export type Langue = CreateOwnerRequest['langue'];
export const LANGUE_OPTIONS: EnumOption<Langue>[] = [
  {value: 'fr', i18n: 'enum.langue.fr'},
  {value: 'en', i18n: 'enum.langue.en'},
];

export const ACTIVE_STATUS_OPTIONS: EnumOption<boolean>[] = [
  {value: true, i18n: 'workspaces.status.active'},
  {value: false, i18n: 'workspaces.status.inactive'},
];

export type DocumentStatus = 'PENDING' | 'INDEXED' | 'ERROR';
export const DOCUMENT_STATUS_OPTIONS: EnumOption<DocumentStatus>[] = [
  {value: 'PENDING', i18n: 'enum.status.PENDING'},
  {value: 'INDEXED', i18n: 'enum.status.INDEXED'},
  {value: 'ERROR', i18n: 'enum.status.ERROR'},
];

export type UserRole = 'OWNER' | 'EDITOR' | 'VIEWER';
export const USER_ROLE_WITHOUT_OWNER_OPTIONS: EnumOption<UserRole>[] = [
  {value: 'EDITOR', i18n: 'enum.role.EDITOR'},
  {value: 'VIEWER', i18n: 'enum.role.VIEWER'},
];
export const USER_ROLE_OPTIONS: EnumOption<UserRole>[] = [
  {value: 'OWNER', i18n: 'enum.role.OWNER'},
  {value: 'EDITOR', i18n: 'enum.role.EDITOR'},
  {value: 'VIEWER', i18n: 'enum.role.VIEWER'},
];
