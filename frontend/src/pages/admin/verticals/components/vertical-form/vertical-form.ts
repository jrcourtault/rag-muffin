import {Component, inject, signal} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {toSignal} from '@angular/core/rxjs-interop';
import {map} from 'rxjs';
import {TranslocoPipe} from '@jsverse/transloco';
import {Drawer} from 'primeng/drawer';
import {InputText} from 'primeng/inputtext';
import {ButtonModule} from 'primeng/button';
import {Textarea} from 'primeng/textarea';

import {FormSectionTitle} from '@/components/form-section-title/form-section-title';
import {VerticalStore} from '@/pages/admin/verticals/vertical.store';
import {VerticalResponse} from '@/api/backend/models/vertical-response';

@Component({
  selector: 'app-vertical-form',
  templateUrl: './vertical-form.html',
  imports: [ReactiveFormsModule, TranslocoPipe, Drawer, InputText, ButtonModule, Textarea, FormSectionTitle],
})
export class VerticalForm {
  protected store = inject(VerticalStore);

  readonly visible = signal(false);
  readonly isEdit = signal(false);

  private id?: string;

  readonly form = new FormGroup({
    name: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    queryRewritePrompt: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    systemPrompt: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
  });

  readonly isValid = toSignal(
    this.form.statusChanges.pipe(map(() => this.form.valid)),
    {initialValue: this.form.valid},
  );

  open(vertical?: VerticalResponse) {
    this.form.reset();
    const isEdit = vertical != undefined;
    this.isEdit.set(isEdit);
    this.id = vertical?.id;
    if (isEdit) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const v = vertical as any;
      this.form.patchValue({
        name: v.name ?? '',
        queryRewritePrompt: v.queryRewritePrompt ?? '',
        systemPrompt: v.systemPrompt ?? '',
      });
    }
    this.visible.set(true);
  }

  async onSave() {
    if (this.form.invalid) return;
    const request = this.form.getRawValue();
    if (this.isEdit()) {
      await this.store.update(this.id!, request);
    } else {
      await this.store.create(request);
    }
    this.close();
  }

  close() {
    this.visible.set(false);
  }
}
