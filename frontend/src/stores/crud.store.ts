import {inject, signal} from '@angular/core';

import {ToastService} from '@/services/toast.service';

export abstract class CrudStore<T, CreateReq = never, UpdateReq = never> {
  protected toastService = inject(ToastService);

  readonly items = signal<T[]>([]);
  readonly isProcessing = signal(false);

  async load() {
    this.isProcessing.set(true);
    try {
      this.items.set(await this.doFetch());
    } finally {
      this.isProcessing.set(false);
    }
  }

  async create(request: CreateReq) {
    this.isProcessing.set(true);
    try {
      await this.doCreate(request);
      this.toastService.success();
      await this.load();
    } finally {
      this.isProcessing.set(false);
    }
  }

  async update(id: string, request: UpdateReq) {
    this.isProcessing.set(true);
    try {
      await this.doUpdate(id, request);
      this.toastService.success();
      await this.load();
    } finally {
      this.isProcessing.set(false);
    }
  }

  async delete(id: string) {
    this.isProcessing.set(true);
    try {
      await this.doDelete(id);
      this.toastService.success();
      await this.load();
    } finally {
      this.isProcessing.set(false);
    }
  }

  protected abstract doFetch(): Promise<T[]>;

  protected abstract doCreate(request: CreateReq): Promise<unknown>;

  protected abstract doUpdate(id: string, request: UpdateReq): Promise<unknown>;

  protected abstract doDelete(id: string): Promise<unknown>;
}