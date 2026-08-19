import {UserRole} from '@/models/enum-options';

export const TABLE_DEFAULT_PAGE_SIZE = 10;
export const TABLE_PAGE_SIZE_OPTIONS = [10, 25, 50, 100];

export const RAG_QUERY_REWRITING_DEFAULT = false;

export const USER_DEFAULT_ROLE: UserRole = 'VIEWER';

export const WORKSPACE_DEFAULT_VERTICAL_NAME = 'Generic';

export const WORKSPACE_DEFAULT_CHUNK_SIZE = 512;
export const WORKSPACE_DEFAULT_CHUNK_OVERLAP = 77;
export const WORKSPACE_DEFAULT_PREFETCH_SIZE = 30;
export const WORKSPACE_DEFAULT_TOP_K = 10;

export const DOCUMENTS_UPLOAD_ACCEPTED_EXTENSIONS = 'pdf,txt,md,csv,tsv,rtf,html,htm,xml,json,yaml,yml,log,docx,doc,docm,dotm,odt,pptx,ppt,pptm,ppsx,pps,ppsm,potm,odp,xlsx,xls,xlsm,xltx,ods';
export const DOCUMENTS_UPLOAD_MAX_FILE_SIZE = 50 * 1024 * 1024; // 50 MB
export const INDEXING_DOCUMENTS_POLL_INTERVAL_MS = 5000; // si un document est en train de s'indexer, on poll toutes les DOCUMENTS_POLL_INTERVAL_MS ms
