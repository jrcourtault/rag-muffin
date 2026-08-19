import {Component, viewChild} from '@angular/core';

import {PageContent} from '@/components/page-content/page-content';
import {DocumentForm} from './components/document-form/document-form';
import {DocumentTable} from './components/document-table/document-table';
import {DocumentToolbar} from './components/document-toolbar/document-toolbar';
import {DocumentPageStore} from '@/pages/rag/documents/document-page.store';
import {DocumentResponse} from '@/api/backend/models/document-response';

@Component({
  selector: 'app-documents',
  templateUrl: './documents.html',
  imports: [PageContent, DocumentTable, DocumentToolbar, DocumentForm],
  providers: [DocumentPageStore],
})
export class Documents {
  readonly formComponent = viewChild.required(DocumentForm);

  onCreate() {
    this.formComponent().open();
  }

  onEdit(document: DocumentResponse) {
    this.formComponent().open(document);
  }
}
