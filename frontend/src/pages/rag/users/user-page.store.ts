import {inject, Injectable} from '@angular/core';

import {UserControllerService} from '@/api/backend/services/user-controller.service';
import {UserResponse} from '@/api/backend/models/user-response';
import {CreateUserRequest} from '@/api/backend/models/create-user-request';
import {UpdateUserRequest} from '@/api/backend/models/update-user-request';
import {WorkspaceSelectionService} from '@/services/workspace-selection.service';
import {PagedResult, CrudPaginatedStore} from '@/stores/crud-paginated.store';

export interface UserFilters {
  name: string;
  email: string;
  role: 'OWNER' | 'EDITOR' | 'VIEWER';
}

@Injectable()
export class UserPageStore extends CrudPaginatedStore<UserResponse, CreateUserRequest, UpdateUserRequest, UserFilters> {
  private userController = inject(UserControllerService);
  private workspaceSelectionService = inject(WorkspaceSelectionService);

  protected override initialSort() { return 'lastName,asc'; }

  protected override async doFetch(page: number, size: number, sort?: string, filters?: Partial<UserFilters>): Promise<PagedResult<UserResponse>> {
    const workspaceId = this.workspaceSelectionService.selectedId()!;
    return await this.userController.listUsers({
      workspaceId,
      page,
      size,
      sort: sort ? [sort] : undefined,
      name: filters?.name || undefined,
      email: filters?.email || undefined,
      role: filters?.role || undefined,
    });
  }

  protected override doCreate(request: CreateUserRequest) {
    const workspaceId = this.workspaceSelectionService.selectedId()!;
    return this.userController.createUser({workspaceId, body: request});
  }

  protected override doUpdate(id: string, request: UpdateUserRequest) {
    const workspaceId = this.workspaceSelectionService.selectedId()!;
    return this.userController.updateUser({workspaceId, id, body: request});
  }

  protected override doDelete(id: string) {
    const workspaceId = this.workspaceSelectionService.selectedId()!;
    return this.userController.deleteUser({workspaceId, id});
  }
}
