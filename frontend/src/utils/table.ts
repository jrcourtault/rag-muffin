import {TableLazyLoadEvent} from 'primeng/table';

export function buildSortFromEvent(event: TableLazyLoadEvent): string | undefined {
  if (event.sortField && typeof event.sortField === 'string') {
    const direction = event.sortOrder === -1 ? 'desc' : 'asc';
    return `${event.sortField},${direction}`;
  }
  return undefined;
}