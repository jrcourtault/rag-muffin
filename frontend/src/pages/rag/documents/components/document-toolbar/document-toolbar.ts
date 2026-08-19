import {Component, inject, output} from '@angular/core';
import {TranslocoPipe} from '@jsverse/transloco';
import {InputText} from 'primeng/inputtext';
import {Select} from 'primeng/select';
import {Toolbar} from 'primeng/toolbar';
import {AddButton} from '@/components/buttons/add-button/add-button';
import {RefreshButton} from '@/components/buttons/refresh-button/refresh-button';
import {DOCUMENTS_UPLOAD_ACCEPTED_EXTENSIONS} from '@/constants';
import {DOCUMENT_STATUS_OPTIONS, DocumentStatus} from '@/models/enum-options';
import {DocumentFilters, DocumentPageStore} from '@/pages/rag/documents/document-page.store';
import {SecurityService} from '@/services/security.service';

@Component({
  selector: 'app-document-toolbar',
  templateUrl: './document-toolbar.html',
  imports: [TranslocoPipe, InputText, Select, Toolbar, AddButton, RefreshButton],
})
export class DocumentToolbar {
  readonly store = inject(DocumentPageStore);
  readonly security = inject(SecurityService);

  readonly createRequested = output<void>();

  readonly statusOptions = DOCUMENT_STATUS_OPTIONS;
  readonly extensionOptions = DOCUMENTS_UPLOAD_ACCEPTED_EXTENSIONS.split(',').sort();

  private filters: Partial<DocumentFilters> = {};

  onFilterName(event: Event) {
    const value = (event.target as HTMLInputElement).value;
    this.filters = {...this.filters, name: value || undefined};
    this.store.changeFilter(this.filters);
  }

  onFilterExtension(value: string | null) {
    this.filters = {...this.filters, extension: value ?? undefined};
    this.store.changeFilter(this.filters);
  }

  onFilterStatus(value: DocumentStatus | null) {
    this.filters = {...this.filters, status: value ?? undefined};
    this.store.changeFilter(this.filters);
  }
}
