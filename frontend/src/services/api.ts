import axios, { AxiosInstance, InternalAxiosRequestConfig } from 'axios'
import {
  AuthResponse,
  VocabularyCard,
  Tag,
  LoginCredentials,
  SignupData,
  VocabularyFormData,
  ReviewSubmission,
  VocabularyQueryParams,
} from '@/types'

const API_BASE_URL = 'http://localhost:8080/api/v1'

const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

export const authAPI = {
  signup: (data: SignupData) => api.post<AuthResponse>('/auth/signup', data),
  login: (data: LoginCredentials) => api.post<AuthResponse>('/auth/login', data),
}

export const vocabularyAPI = {
  getAll: (params?: VocabularyQueryParams) => {
    const queryParams = new URLSearchParams()
    if (params?.sortBy) queryParams.append('sortBy', params.sortBy)
    if (params?.sortDirection) queryParams.append('sortDirection', params.sortDirection)
    if (params?.searchTerm) queryParams.append('searchTerm', params.searchTerm)

    const queryString = queryParams.toString()
    return api.get<VocabularyCard[]>(`/vocabulary${queryString ? `?${queryString}` : ''}`)
  },
  getById: (id: number) => api.get<VocabularyCard>(`/vocabulary/${id}`),
  getDue: () => api.get<VocabularyCard[]>('/vocabulary/due'),
  getDueCount: () => api.get<number>('/vocabulary/due/count'),
  getTotalCount: () => api.get<number>('/vocabulary/count'),
  create: (data: VocabularyFormData) => api.post<VocabularyCard>('/vocabulary', data),
  update: (id: number, data: VocabularyFormData) =>
    api.put<VocabularyCard>(`/vocabulary/${id}`, data),
  delete: (id: number) => api.delete<void>(`/vocabulary/${id}`),
}

export const reviewAPI = {
  review: (data: ReviewSubmission) => api.post<void>('/review', data),
}

export const tagAPI = {
  getAll: () => api.get<Tag[]>('/tags'),
  create: (data: { name: string; color: string }) => api.post<Tag>('/tags', data),
  update: (id: number, data: { name: string; color: string }) => api.put<Tag>(`/tags/${id}`, data),
  delete: (id: number) => api.delete<void>(`/tags/${id}`),
}

export default api
