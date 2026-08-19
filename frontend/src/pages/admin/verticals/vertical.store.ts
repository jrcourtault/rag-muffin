import {inject, Injectable} from '@angular/core';

import {CrudStore} from '@/stores/crud.store';
import {VerticalControllerService} from '@/api/backend/services/vertical-controller.service';
import {VerticalResponse} from '@/api/backend/models/vertical-response';
import {CreateVerticalRequest} from '@/api/backend/models/create-vertical-request';
import {UpdateVerticalRequest} from '@/api/backend/models/update-vertical-request';

@Injectable()
export class VerticalStore extends CrudStore<VerticalResponse, CreateVerticalRequest, UpdateVerticalRequest> {
  private verticalController = inject(VerticalControllerService);

  protected override async doFetch() {
    return this.verticalController.listVerticals();
  }

  protected override doCreate(request: CreateVerticalRequest) {
    return this.verticalController.createVertical({body: request});
  }

  protected override doUpdate(id: string, request: UpdateVerticalRequest) {
    return this.verticalController.updateVertical({id, body: request});
  }

  protected override doDelete(id: string) {
    return this.verticalController.deleteVertical({id});
  }
}
