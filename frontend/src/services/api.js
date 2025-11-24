import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export const authAPI = {
  signup: (data) => api.post('/auth/signup', data),
  login: (data) => api.post('/auth/login', data),
};

export const vocabularyAPI = {
  getAll: () => api.get('/vocabulary'),
  getDue: () => api.get('/vocabulary/due'),
  getDueCount: () => api.get('/vocabulary/due/count'),
  create: (data) => api.post('/vocabulary', data),
  update: (id, data) => api.put(`/vocabulary/${id}`, data),
  delete: (id) => api.delete(`/vocabulary/${id}`),
};

export const reviewAPI = {
  review: (data) => api.post('/review', data),
};

export const tagAPI = {
  getAll: () => api.get('/tags'),
  create: (data) => api.post('/tags', data),
  delete: (id) => api.delete(`/tags/${id}`),
};

export default api;
