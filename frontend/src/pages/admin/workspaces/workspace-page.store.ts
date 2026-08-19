import {inject, Injectable} from '@angular/core';

import {WorkspaceSelectionService} from '@/services/workspace-selection.service';
import {PagedResult, CrudPaginatedStore} from '@/stores/crud-paginated.store';
import {CreateWorkspaceWithOwnerRequest} from '@/api/backend/models/create-workspace-with-owner-request';
import {UpdateWorkspaceWithOwnerRequest} from '@/api/backend/models/update-workspace-with-owner-request';
import {WorkspaceControllerService} from '@/api/backend/services/workspace-controller.service';
import {WorkspaceWithVerticalResponse} from '@/api/backend/models/workspace-with-vertical-response';

export interface WorkspaceFilters {
  name: string;
  verticalId: string;
  active: boolean;
}

@Injectable()
export class WorkspacePageStore extends CrudPaginatedStore<WorkspaceWithVerticalResponse, CreateWorkspaceWithOwnerRequest, UpdateWorkspaceWithOwnerRequest, WorkspaceFilters> {
  private workspaceController = inject(WorkspaceControllerService);
  private workspaceSelectionService = inject(WorkspaceSelectionService);

  protected override initialSort() { return 'name,asc'; }

  protected override async doFetch(page: number, size: number, sort?: string, filters?: Partial<WorkspaceFilters>): Promise<PagedResult<WorkspaceWithVerticalResponse>> {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    return await this.workspaceController.listWorkspaces({
      page,
      size,
      sort: sort ? [sort] : undefined,
      name: filters?.name || undefined,
      verticalId: filters?.verticalId || undefined,
      active: filters?.active,
    } as any);
  }

  protected override async doCreate(request: CreateWorkspaceWithOwnerRequest) {
    await this.workspaceController.createWorkspace({body: request});
    await this.workspaceSelectionService.load();
  }

  protected override async doUpdate(id: string, request: UpdateWorkspaceWithOwnerRequest) {
    await this.workspaceController.updateWorkspace({id, body: request});
    await this.workspaceSelectionService.load();
  }

  protected override async doDelete(id: string) {
    await this.workspaceController.deleteWorkspace({id});
    await this.workspaceSelectionService.load();
  }
}
