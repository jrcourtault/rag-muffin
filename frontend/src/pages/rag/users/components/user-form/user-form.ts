import {Component, effect, inject, output, signal} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {toSignal} from '@angular/core/rxjs-interop';
import {map} from 'rxjs';
import {TranslocoPipe} from '@jsverse/transloco';
import {Drawer} from 'primeng/drawer';
import {InputText} from 'primeng/inputtext';
import {Select} from 'primeng/select';
import {ButtonModule} from 'primeng/button';
import {Spinner} from '@/components/spinner/spinner';
import {Langue, LANGUE_OPTIONS, UserRole, USER_ROLE_WITHOUT_OWNER_OPTIONS} from '@/models/enum-options';
import {USER_DEFAULT_ROLE} from '@/constants';
import {UserPageStore} from '@/pages/rag/users/user-page.store';
import {UserControllerService} from '@/api/backend/services/user-controller.service';
import {UserResponse} from '@/api/backend/models/user-response';
import {WorkspaceSelectionService} from '@/services/workspace-selection.service';

@Component({
  selector: 'app-user-form',
  templateUrl: './user-form.html',
  imports: [ReactiveFormsModule, TranslocoPipe, Drawer, InputText, Select, ButtonModule, Spinner],
})
export class UserForm {
  protected store = inject(UserPageStore);
  private userController = inject(UserControllerService);
  private workspaceSelectionService = inject(WorkspaceSelectionService);

  readonly visible = signal(false);
  readonly loading = signal(false);
  readonly isEdit = signal(false);
  readonly closed = output<void>();

  readonly roles = USER_ROLE_WITHOUT_OWNER_OPTIONS;
  readonly langues = LANGUE_OPTIONS;

  private id?: string;

  readonly form = new FormGroup({
    email: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.email]}),
    role: new FormControl<UserRole>(USER_DEFAULT_ROLE, {nonNullable: true}),
    langue: new FormControl<Langue>('fr', {nonNullable: true}),
    firstName: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    lastName: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
  });

  readonly isValid = toSignal(
    this.form.statusChanges.pipe(map(() => this.form.valid)),
    {initialValue: this.form.valid},
  );

  constructor() {
    effect(() => {
      if (this.isEdit()) {
        this.form.controls.email.disable({emitEvent: false});
      } else {
        this.form.controls.email.enable({emitEvent: false});
      }
    });
  }

  open(user?: UserResponse) {
    this.form.reset();
    const isEdit = user != undefined;
    this.isEdit.set(isEdit);
    this.id = user?.id;
    if (isEdit) {
      this.loadData(user.id!);
    }
    this.visible.set(true);
  }

  private async loadData(id: string) {
    this.loading.set(true);
    const workspaceId = this.workspaceSelectionService.selectedId()!;
    const user = await this.userController.getUser({workspaceId, id});
    this.form.patchValue({
      email: user.email ?? '',
      role: (user.role as UserRole) ?? 'VIEWER',
      firstName: user.firstName ?? '',
      lastName: user.lastName ?? '',
    });
    this.loading.set(false);
  }

  async onSave() {
    if (this.form.invalid) return;
    const {email, role, langue, firstName, lastName} = this.form.getRawValue();
    if (this.isEdit()) {
      await this.store.update(this.id!, {role, langue, firstName, lastName});
    } else {
      await this.store.create({email, role, langue, firstName, lastName});
    }
    this.close();
  }

  close() {
    this.visible.set(false);
    this.closed.emit();
  }
}
