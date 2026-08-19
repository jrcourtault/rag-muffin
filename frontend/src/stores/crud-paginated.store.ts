import {inject, signal} from '@angular/core';

import {TABLE_DEFAULT_PAGE_SIZE} from '@/constants';
import {ToastService} from '@/services/toast.service';

export interface PagedResult<T> {
  content?: T[];
  page?: { totalElements?: number };
}

export abstract class CrudPaginatedStore<T, CreateReq = never, UpdateReq = never, Filters = object> {
  protected toastService = inject(ToastService);

  readonly items = signal<T[]>([]);
  readonly totalRecords = signal(0);
  readonly isProcessing = signal(false);

  protected currentPage = 0;
  protected currentSize = TABLE_DEFAULT_PAGE_SIZE;
  protected currentSort?: string = this.initialSort();
  protected currentFilters: Partial<Filters> = {};

  protected initialSort(): string | undefined {
    return undefined;
  }

  async loadPage() {
    this.isProcessing.set(true);
    try {
      const result = await this.doFetch(this.currentPage, this.currentSize, this.currentSort, this.currentFilters);
      this.items.set(result.content ?? []);
      this.totalRecords.set(result.page?.totalElements ?? 0);
    } finally {
      this.isProcessing.set(false);
    }
  }

  async changePage(page: number = 0, size: number = TABLE_DEFAULT_PAGE_SIZE, sort?: string) {
    this.currentPage = page;
    this.currentSize = size;
    this.currentSort = sort ?? this.currentSort;
    await this.loadPage();
  }

  async changeFilter(filters: Partial<Filters>) {
    this.currentPage = 0;
    this.currentFilters = filters;
    await this.loadPage();
  }

  async create(request: CreateReq) {
    this.isProcessing.set(true);
    try {
      await this.doCreate(request);
      this.toastService.success();
      await this.loadPage();
    } finally {
      this.isProcessing.set(false);
    }
  }

  async update(id: string, request: UpdateReq) {
    this.isProcessing.set(true);
    try {
      await this.doUpdate(id, request);
      this.toastService.success();
      await this.loadPage();
    } finally {
      this.isProcessing.set(false);
    }
  }

  async delete(id: string) {
    this.isProcessing.set(true);
    try {
      await this.doDelete(id);
      this.toastService.success();
      await this.loadPage();
    } finally {
      this.isProcessing.set(false);
    }
  }

  protected abstract doFetch(page: number, size: number, sort?: string, filters?: Partial<Filters>): Promise<PagedResult<T>>;

  protected abstract doCreate(request: CreateReq): Promise<unknown>;

  protected abstract doUpdate(id: string, request: UpdateReq): Promise<unknown>;

  protected abstract doDelete(id: string): Promise<unknown>;
}
