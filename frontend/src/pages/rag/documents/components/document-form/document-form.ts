import {Component, computed, inject, output, signal} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {toSignal} from '@angular/core/rxjs-interop';
import {map} from 'rxjs';
import {TranslocoPipe} from '@jsverse/transloco';
import {Drawer} from 'primeng/drawer';
import {InputText} from 'primeng/inputtext';
import {ButtonModule} from 'primeng/button';
import {FileUpload, FileUploadHandlerEvent} from 'primeng/fileupload';
import {Spinner} from '@/components/spinner/spinner';
import {DocumentPageStore} from '@/pages/rag/documents/document-page.store';
import {DocumentResponse} from '@/api/backend/models/document-response';
import {DocumentControllerService} from '@/api/backend/services/document-controller.service';
import {WorkspaceSelectionService} from '@/services/workspace-selection.service';
import {DOCUMENTS_UPLOAD_ACCEPTED_EXTENSIONS, DOCUMENTS_UPLOAD_MAX_FILE_SIZE} from '@/constants';

@Component({
  selector: 'app-document-form',
  templateUrl: './document-form.html',
  imports: [ReactiveFormsModule, TranslocoPipe, Drawer, InputText, ButtonModule, FileUpload, Spinner],
})
export class DocumentForm {
  protected store = inject(DocumentPageStore);
  private documentController = inject(DocumentControllerService);
  private workspaceSelection = inject(WorkspaceSelectionService);

  readonly visible = signal(false);
  readonly loading = signal(false);
  readonly isEdit = signal(false);
  readonly closed = output<void>();

  private id?: string;

  readonly acceptedExtensions = DOCUMENTS_UPLOAD_ACCEPTED_EXTENSIONS
    .split(',')
    .map(e => '.' + e)
    .reduce((prv, curr) => prv + ',' + curr);
  readonly maxFileSize = DOCUMENTS_UPLOAD_MAX_FILE_SIZE;

  readonly form = new FormGroup({
    name: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
  });

  readonly isValid = toSignal(
    this.form.statusChanges.pipe(map(() => this.form.valid)),
    {initialValue: this.form.valid},
  );

  readonly canSave = computed(() => this.isEdit() && this.isValid());

  open(document?: DocumentResponse) {
    this.form.reset();
    const isEdit = document != undefined;
    this.isEdit.set(isEdit);
    this.id = document?.id;
    if (isEdit) {
      this.loadData(document!.id!);
    }
    this.visible.set(true);
  }

  private async loadData(id: string) {
    this.loading.set(true);
    const workspaceId = this.workspaceSelection.selectedId()!;
    const document = await this.documentController.getDocument({workspaceId, id});
    this.form.patchValue({
      name: document.name ?? ''
    });
    this.loading.set(false);
  }

  async onUpload(event: FileUploadHandlerEvent, fileUpload: FileUpload) {
    for (const file of event.files) {
      const name = file.name.replace(/\.[^.]+$/, '');
      await this.store.create({name, file});
    }
    fileUpload.clear();
    this.close();
  }

  async onSave() {
    if (!this.canSave()) return;
    const {name} = this.form.getRawValue();
    await this.store.update(this.id!, {name});
    this.close();
  }

  close() {
    this.visible.set(false);
    this.closed.emit();
  }
}
