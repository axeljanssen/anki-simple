// Component prop types
import { ReactNode } from 'react'
import { VocabularyCard, Tag, VocabularyFormData, TagFormData } from './models'
import { SortableField, SortDirection } from './api'

export interface ProtectedRouteProps {
  children: ReactNode
}

export interface VocabularyListProps {
  cards: VocabularyCard[]
  onEdit: (card: VocabularyCard) => void
  onDelete: (id: number) => void
}

export interface VocabularyTableProps {
  cards: VocabularyCard[]
  onEdit: (card: VocabularyCard) => void
  onDelete: (id: number) => void
  sortBy?: SortableField
  sortDirection?: SortDirection
  onSort: (field: SortableField) => void
  loading?: boolean
}

export interface VocabularyFormProps {
  card: VocabularyCard | null
  tags: Tag[]
  onSave: (data: VocabularyFormData) => void
  onCancel: () => void
}

export interface TagFormProps {
  tag: Tag | null
  onSave: (data: TagFormData) => void
  onCancel: () => void
}
