import React, { useState, FormEvent, ChangeEvent } from 'react'
import type { TagFormProps, TagFormData } from '@/types'

const TagForm = ({ tag, onSave, onCancel }: TagFormProps): React.JSX.Element => {
  const [formData, setFormData] = useState<TagFormData>(() => {
    if (tag) {
      return {
        name: tag.name,
        color: tag.color || '#3B82F6',
      }
    }
    return {
      name: '',
      color: '#3B82F6', // Default blue
    }
  })
  const [prevTagId, setPrevTagId] = useState<number | undefined>(tag?.id)

  // Reset form when switching between different tags
  if (tag?.id !== prevTagId) {
    setPrevTagId(tag?.id)
    setFormData(
      tag
        ? { name: tag.name, color: tag.color || '#3B82F6' }
        : { name: '', color: '#3B82F6' }
    )
  }

  const handleSubmit = (e: FormEvent<HTMLFormElement>): void => {
    e.preventDefault()
    if (!formData.name.trim()) {
      return
    }
    onSave(formData)
  }

  const handleChange = (e: ChangeEvent<HTMLInputElement>): void => {
    const { name, value } = e.target
    setFormData({ ...formData, [name]: value })
  }

  return (
    <div className="bg-white p-8 rounded-xl shadow-md">
      <h3 className="mt-0 mb-5 text-gray-800 text-xl font-semibold">
        {tag ? 'Edit Tag' : 'Create New Tag'}
      </h3>
      <form onSubmit={handleSubmit}>
        <div className="mb-5">
          <label htmlFor="name" className="block mb-2 text-gray-600 font-medium text-sm">
            Tag Name *
          </label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
            className="w-full px-3 py-3 border border-gray-300 rounded-lg text-sm transition-colors focus:outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-200"
            placeholder="Enter tag name"
          />
        </div>

        <div className="mb-5">
          <label htmlFor="color" className="block mb-2 text-gray-600 font-medium text-sm">
            Color
          </label>
          <div className="flex items-center gap-3">
            <input
              type="color"
              id="color"
              name="color"
              value={formData.color}
              onChange={handleChange}
              className="w-16 h-12 border border-gray-300 rounded-lg cursor-pointer"
            />
            <span className="text-sm text-gray-600">{formData.color}</span>
          </div>
        </div>

        <div className="mb-5">
          <div className="block mb-2 text-gray-600 font-medium text-sm">Preview</div>
          <span
            className="inline-block px-3 py-1 rounded-full text-xs font-medium text-white"
            style={{ backgroundColor: formData.color }}
          >
            {formData.name || 'Tag Name'}
          </span>
        </div>

        <div className="flex gap-3 justify-end">
          <button
            type="button"
            onClick={onCancel}
            className="px-6 py-3 bg-gray-100 text-gray-600 border border-gray-300 rounded-lg text-sm font-semibold cursor-pointer transition-colors hover:bg-gray-200"
          >
            Cancel
          </button>
          <button
            type="submit"
            className="px-6 py-3 bg-gradient-to-r from-blue-600 to-blue-700 text-white rounded-lg text-sm font-semibold cursor-pointer transition-transform hover:-translate-y-0.5 shadow-md"
          >
            {tag ? 'Update Tag' : 'Create Tag'}
          </button>
        </div>
      </form>
    </div>
  )
}

export default TagForm
