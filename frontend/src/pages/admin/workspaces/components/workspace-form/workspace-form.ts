import {Component, effect, inject, input, output, signal} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {toSignal} from '@angular/core/rxjs-interop';
import {map} from 'rxjs';
import {TranslocoPipe} from '@jsverse/transloco';
import {Drawer} from 'primeng/drawer';
import {InputText} from 'primeng/inputtext';
import {InputNumber} from 'primeng/inputnumber';
import {Select} from 'primeng/select';
import {ToggleSwitch} from 'primeng/toggleswitch';
import {ButtonModule} from 'primeng/button';
import {Spinner} from '@/components/spinner/spinner';
import {FormSectionTitle} from '@/components/form-section-title/form-section-title';
import {Langue, LANGUE_OPTIONS} from '@/models/enum-options';
import {
  WORKSPACE_DEFAULT_CHUNK_OVERLAP,
  WORKSPACE_DEFAULT_CHUNK_SIZE,
  WORKSPACE_DEFAULT_PREFETCH_SIZE,
  WORKSPACE_DEFAULT_TOP_K,
  WORKSPACE_DEFAULT_VERTICAL_NAME,
} from '@/constants';
import {WorkspacePageStore} from '@/pages/admin/workspaces/workspace-page.store';
import {WorkspaceControllerService} from '@/api/backend/services/workspace-controller.service';
import {VerticalResponse} from '@/api/backend/models/vertical-response';

@Component({
  selector: 'app-workspace-form',
  templateUrl: './workspace-form.html',
  imports: [ReactiveFormsModule, TranslocoPipe, Drawer, InputText, InputNumber, Select, ToggleSwitch, ButtonModule, Spinner, FormSectionTitle],
})
export class WorkspaceForm {
  protected store = inject(WorkspacePageStore);
  private workspaceController = inject(WorkspaceControllerService);

  readonly verticals = input<VerticalResponse[]>([]);
  readonly visible = signal(false);
  readonly loading = signal(false);
  readonly isEdit = signal(false);
  readonly closed = output<void>();

  readonly langues = LANGUE_OPTIONS;

  private id?: string;

  readonly form = new FormGroup({
    name: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    verticalId: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    active: new FormControl(true, {nonNullable: true}),
    chunkSize: new FormControl(WORKSPACE_DEFAULT_CHUNK_SIZE, {nonNullable: true, validators: [Validators.required, Validators.min(1)]}),
    chunkOverlap: new FormControl(WORKSPACE_DEFAULT_CHUNK_OVERLAP, {nonNullable: true, validators: [Validators.required, Validators.min(0)]}),
    prefetchSize: new FormControl(WORKSPACE_DEFAULT_PREFETCH_SIZE, {nonNullable: true, validators: [Validators.required, Validators.min(1)]}),
    rerank: new FormControl(true, {nonNullable: true}),
    topK: new FormControl(WORKSPACE_DEFAULT_TOP_K, {nonNullable: true, validators: [Validators.required, Validators.min(1)]}),
    ownerEmail: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.email]}),
    ownerLangue: new FormControl<Langue>('fr', {nonNullable: true}),
    ownerFirstName: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    ownerLastName: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
  });

  readonly isValid = toSignal(
    this.form.statusChanges.pipe(map(() => this.form.valid)),
    {initialValue: this.form.valid},
  );

  private readonly selectedVerticalId = toSignal(
    this.form.controls.verticalId.valueChanges,
    {initialValue: ''},
  );

  constructor() {
    effect(() => {
      const verticalId = this.selectedVerticalId();
      if (!verticalId || this.isEdit()) return;
      const vertical = this.verticals().find(v => v.id === verticalId);
      if (vertical) {
        this.form.patchValue({
          chunkSize: WORKSPACE_DEFAULT_CHUNK_SIZE,
          chunkOverlap: WORKSPACE_DEFAULT_CHUNK_OVERLAP,
          prefetchSize: WORKSPACE_DEFAULT_PREFETCH_SIZE,
          rerank: true,
          topK: WORKSPACE_DEFAULT_TOP_K,
        });
      }
    });
  }

  open(workspace?: { id?: string }) {
    this.form.reset();
    const isEdit = workspace != undefined;
    this.isEdit.set(isEdit);
    this.id = workspace?.id;
    if (isEdit) {
      this.form.controls.chunkSize.disable();
      this.form.controls.chunkOverlap.disable();
      this.loadData(workspace.id!);
    } else {
      this.form.controls.chunkSize.enable();
      this.form.controls.chunkOverlap.enable();
      const defaultVertical = this.verticals().find(v => v.name === WORKSPACE_DEFAULT_VERTICAL_NAME);
      if (defaultVertical?.id) {
        this.form.patchValue({verticalId: defaultVertical.id});
      }
    }
    this.visible.set(true);
  }

  private async loadData(id: string) {
    this.loading.set(true);
    const workspaceWithOwner = await this.workspaceController.getWorkspace({id});
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const workspace = workspaceWithOwner.workspace as any;
    const owner = workspaceWithOwner.owner;
    this.form.patchValue({
      name: workspace?.name ?? '',
      verticalId: workspace?.verticalId ?? '',
      active: workspace?.active ?? true,
      chunkSize: workspace?.chunkSize ?? WORKSPACE_DEFAULT_CHUNK_SIZE,
      chunkOverlap: workspace?.chunkOverlap ?? WORKSPACE_DEFAULT_CHUNK_OVERLAP,
      prefetchSize: workspace?.prefetchSize ?? WORKSPACE_DEFAULT_PREFETCH_SIZE,
      rerank: workspace?.rerank ?? true,
      topK: workspace?.topK ?? WORKSPACE_DEFAULT_TOP_K,
      ownerEmail: owner?.email ?? '',
      ownerLangue: (owner?.langue as Langue) ?? 'fr',
      ownerFirstName: owner?.firstName ?? '',
      ownerLastName: owner?.lastName ?? '',
    });
    this.loading.set(false);
  }

  async onSave() {
    if (this.form.invalid) return;
    const {
      name, verticalId, active, chunkSize, chunkOverlap, prefetchSize, rerank, topK,
      ownerEmail, ownerLangue, ownerFirstName, ownerLastName
    } = this.form.getRawValue();
    const ownerRequest = {email: ownerEmail, langue: ownerLangue, firstName: ownerFirstName, lastName: ownerLastName};
    if (this.isEdit()) {
      await this.store.update(this.id!, {
        updateWorkspaceRequest: {name, verticalId, active, prefetchSize, rerank, topK},
        updateOwnerRequest: ownerRequest,
      });
    } else {
      await this.store.create(
        {
          createWorkspaceRequest: {name, verticalId, active, chunkSize, chunkOverlap, prefetchSize, rerank, topK},
          createOwnerRequest: ownerRequest
        }
      );
    }
    this.close();
  }

  close() {
    this.visible.set(false);
    this.closed.emit();
  }
}
