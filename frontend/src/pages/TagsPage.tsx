import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import { tagAPI } from '@/services/api'
import type { Tag, TagFormData } from '@/types'
import TagForm from '@/components/TagForm'

const TagsPage = (): React.JSX.Element => {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [tags, setTags] = useState<Tag[]>([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editingTag, setEditingTag] = useState<Tag | null>(null)

  useEffect(() => {
    loadTags()
  }, [])

  const loadTags = async (): Promise<void> => {
    try {
      setLoading(true)
      const response = await tagAPI.getAll()
      setTags(response.data)
    } catch (error) {
      console.error('Error loading tags:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleCreate = (): void => {
    setEditingTag(null)
    setShowForm(true)
  }

  const handleEdit = (tag: Tag): void => {
    setEditingTag(tag)
    setShowForm(true)
  }

  const handleSave = async (data: TagFormData): Promise<void> => {
    try {
      if (editingTag) {
        await tagAPI.update(editingTag.id, data)
      } else {
        await tagAPI.create(data)
      }
      setShowForm(false)
      setEditingTag(null)
      await loadTags()
    } catch (error) {
      console.error('Error saving tag:', error)
    }
  }

  const handleDelete = async (id: number): Promise<void> => {
    if (!confirm('Are you sure you want to delete this tag? It will be removed from all cards.')) {
      return
    }

    try {
      await tagAPI.delete(id)
      await loadTags()
    } catch (error) {
      console.error('Error deleting tag:', error)
    }
  }

  const handleCancel = (): void => {
    setShowForm(false)
    setEditingTag(null)
  }

  const handleOverlayClick = (e: React.MouseEvent<HTMLDivElement>): void => {
    if (e.target === e.currentTarget) {
      handleCancel()
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-gradient-to-r from-blue-600 to-blue-700 text-white px-10 py-5 shadow-md max-md:px-5">
        <div className="flex justify-between items-center mb-5 max-md:flex-col max-md:gap-3 max-md:items-start">
          <h1 className="text-3xl font-bold">Manage Tags</h1>
          <div className="flex gap-3 items-center max-md:w-full max-md:flex-wrap">
            <button
              onClick={() => navigate('/dashboard')}
              className="px-4 py-2 bg-white/20 text-white border border-white rounded-lg text-sm cursor-pointer transition-colors hover:bg-white/30"
            >
              Back to Dashboard
            </button>
            {user && <span className="text-sm">Welcome, {user.username}</span>}
            <button
              onClick={logout}
              className="px-4 py-2 bg-white/20 text-white border border-white rounded-lg text-sm cursor-pointer transition-colors hover:bg-white/30"
            >
              Logout
            </button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-5xl mx-auto px-5 py-8">
        <div className="mb-5">
          <button
            onClick={handleCreate}
            className="px-6 py-3 bg-gradient-to-r from-blue-600 to-blue-700 text-white rounded-lg text-sm font-semibold cursor-pointer transition-transform hover:-translate-y-0.5 shadow-md"
          >
            Add New Tag
          </button>
        </div>

        {loading ? (
          <div className="bg-white py-15 px-5 rounded-lg shadow-sm text-center text-gray-600 text-base">
            Loading...
          </div>
        ) : tags.length === 0 ? (
          <div className="bg-white py-15 px-5 rounded-lg shadow-sm text-center text-gray-500 text-base">
            No tags yet. Create your first tag to organize vocabulary cards.
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow-sm overflow-hidden">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Tag
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Color
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {tags.map((tag) => (
                  <tr key={tag.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span
                        className="inline-block px-3 py-1 rounded-full text-xs font-medium text-white"
                        style={{ backgroundColor: tag.color }}
                      >
                        {tag.name}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                      {tag.color}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <button
                        onClick={() => handleEdit(tag)}
                        className="px-4 py-2 bg-blue-50 text-blue-600 border border-blue-200 rounded-lg text-sm font-medium cursor-pointer transition-colors hover:bg-blue-100 mr-2"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => handleDelete(tag.id)}
                        className="px-4 py-2 bg-red-50 text-red-600 border border-red-200 rounded-lg text-sm font-medium cursor-pointer transition-colors hover:bg-red-100"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal Overlay */}
      {showForm && (
        <div
          className="form-modal-overlay fixed inset-0 bg-black/50 flex justify-center items-center z-[1000]"
          onClick={handleOverlayClick}
        >
          <div
            className="bg-white rounded-lg p-8 max-w-[600px] w-[90%] max-h-[90vh] overflow-y-auto shadow-2xl animate-modal-slide max-md:w-[95%] max-md:p-5"
            onClick={(e) => e.stopPropagation()}
          >
            <TagForm tag={editingTag} onSave={handleSave} onCancel={handleCancel} />
          </div>
        </div>
      )}
    </div>
  )
}

export default TagsPage
