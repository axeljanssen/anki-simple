// Component prop types
import { ReactNode } from 'react'
import { VocabularyCard, Tag, VocabularyFormData } from './models'

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
}

export interface VocabularyFormProps {
  card: VocabularyCard | null
  tags: Tag[]
  onSave: (data: VocabularyFormData) => void
  onCancel: () => void
}
