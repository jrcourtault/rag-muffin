import {DestroyRef, inject, Injectable} from '@angular/core';

import {DocumentControllerService} from '@/api/backend/services/document-controller.service';
import {DocumentResponse} from '@/api/backend/models/document-response';
import {UpdateDocumentRequest} from '@/api/backend/models/update-document-request';
import {UploadDocumentRequest} from '@/api/backend/models/upload-document-request';
import {WorkspaceSelectionService} from '@/services/workspace-selection.service';
import {PagedResult, CrudPaginatedStore} from '@/stores/crud-paginated.store';
import {INDEXING_DOCUMENTS_POLL_INTERVAL_MS} from '@/constants';

export interface DocumentFilters {
  name: string;
  extension: string;
  status: 'PENDING' | 'INDEXED' | 'ERROR';
}

@Injectable()
export class DocumentPageStore extends CrudPaginatedStore<DocumentResponse, UploadDocumentRequest & {file: Blob}, UpdateDocumentRequest, DocumentFilters> {
  private documentController = inject(DocumentControllerService);
  private workspaceSelectionService = inject(WorkspaceSelectionService);
  private destroyRef = inject(DestroyRef);

  private pollTimer: ReturnType<typeof setInterval> | null = null;

  constructor() {
    super();
    this.destroyRef.onDestroy(() => this.stopPolling());
  }

  protected override initialSort() { return 'name,asc'; }

  protected override async doFetch(page: number, size: number, sort?: string, filters?: Partial<DocumentFilters>): Promise<PagedResult<DocumentResponse>> {
    const workspaceId = this.workspaceSelectionService.selectedId()!;
    const result = await this.documentController.listDocuments({
      workspaceId,
      page,
      size,
      sort: sort ? [sort] : undefined,
      name: filters?.name || undefined,
      extension: filters?.extension || undefined,
      status: filters?.status || undefined,
    });
    this.updatePolling(result.content);
    return result;
  }

  protected override doCreate(request: UploadDocumentRequest & {file: Blob}) {
    const workspaceId = this.workspaceSelectionService.selectedId()!;
    return this.documentController.uploadDocument({
      workspaceId,
      body: {file: request.file, request: {name: request.name}},
    });
  }

  protected override doUpdate(id: string, request: UpdateDocumentRequest) {
    const workspaceId = this.workspaceSelectionService.selectedId()!;
    return this.documentController.updateDocument({workspaceId, id, body: request});
  }

  protected override doDelete(id: string) {
    const workspaceId = this.workspaceSelectionService.selectedId()!;
    return this.documentController.deleteDocument({workspaceId, id});
  }

  async download(doc: DocumentResponse) {
    const workspaceId = this.workspaceSelectionService.selectedId()!;
    const blob = await this.documentController.downloadDocument({workspaceId, id: doc.id!});
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = doc.fileName ?? 'download';
    a.click();
    URL.revokeObjectURL(url);
  }

  private updatePolling(content?: DocumentResponse[]) {
    const hasPending = content?.some((d) => d.status === 'PENDING') ?? false;
    if (hasPending && !this.pollTimer) {
      this.startPolling();
    } else if (!hasPending) {
      this.stopPolling();
    }
  }

  private startPolling() {
    this.pollTimer = setInterval(() => this.loadPage(), INDEXING_DOCUMENTS_POLL_INTERVAL_MS);
  }

  private stopPolling() {
    if (this.pollTimer !== null) {
      clearInterval(this.pollTimer);
      this.pollTimer = null;
    }
  }
}
