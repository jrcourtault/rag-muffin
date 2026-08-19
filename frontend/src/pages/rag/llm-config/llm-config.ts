import {Component, effect, inject, resource, signal} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {TranslocoPipe} from '@jsverse/transloco';
import {InputText} from 'primeng/inputtext';
import {ButtonModule} from 'primeng/button';
import {PageContent} from '@/components/page-content/page-content';
import {Spinner} from '@/components/spinner/spinner';
import {LlmConfigControllerService} from '@/api/backend/services/llm-config-controller.service';
import {LlmConfigResponse} from '@/api/backend/models/llm-config-response';
import {WorkspaceSelectionService} from '@/services/workspace-selection.service';
import {ToastService} from '@/services/toast.service';

@Component({
  selector: 'app-llm-config',
  templateUrl: './llm-config.html',
  imports: [ReactiveFormsModule, TranslocoPipe, InputText, ButtonModule, PageContent, Spinner],
})
export class LlmConfig {
  private llmConfigController = inject(LlmConfigControllerService);
  private workspaceSelectionService = inject(WorkspaceSelectionService);
  private toastService = inject(ToastService);

  readonly saving = signal(false);
  readonly apiKeyConfigured = signal(false);

  readonly form = new FormGroup({
    baseUrl: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    apiKey: new FormControl('', {nonNullable: true}),
    model: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
  });

  readonly configResource = resource<LlmConfigResponse | null, string | undefined>({
    params: () => this.workspaceSelectionService.selectedId(),
    loader: async ({params: workspaceId}) => {
      if (!workspaceId) return null;
      return this.llmConfigController.getLlmConfig({workspaceId});
    },
  });

  constructor() {
    effect(() => {
      const config = this.configResource.value();
      if (config) {
        this.apiKeyConfigured.set(config.apiKeyConfigured ?? false);
        this.form.patchValue({
          baseUrl: config.baseUrl ?? '',
          model: config.model ?? '',
        });
      }
    });
  }

  async onSave() {
    if (this.form.invalid) return;
    const workspaceId = this.workspaceSelectionService.selectedId();
    if (!workspaceId) return;
    this.saving.set(true);
    try {
      const {baseUrl, apiKey, model} = this.form.getRawValue();
      await this.llmConfigController.updateLlmConfig({workspaceId, body: {baseUrl, apiKey: apiKey || undefined, model}});
      this.toastService.success();
    } finally {
      this.saving.set(false);
    }
  }
}
