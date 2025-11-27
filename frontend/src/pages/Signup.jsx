import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Signup = () => {
  const [formData, setFormData] = useState({ username: '', email: '', password: '' });
  const [error, setError] = useState('');
  const { signup } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (formData.password.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }

    const result = await signup(formData);
    if (result.success) {
      navigate('/dashboard');
    } else {
      setError(result.error || 'Signup failed');
    }
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  return (
    <div className="min-h-screen flex justify-center items-center bg-gradient-to-br from-blue-600 to-blue-700 p-5">
      <div className="bg-white p-10 rounded-xl shadow-2xl w-full max-w-md">
        <h1 className="mb-8 text-center text-gray-800 text-2xl font-semibold">Sign Up for Simple Anki</h1>
        {error && <div className="bg-red-50 text-red-600 p-3 rounded-lg mb-5 text-center text-sm">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="mb-5">
            <label htmlFor="username" className="block mb-2 text-gray-600 font-medium text-sm">Username</label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              minLength="3"
              required
              className="w-full px-3 py-3 border border-gray-300 rounded-lg text-sm transition-colors focus:outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-200"
            />
          </div>
          <div className="mb-5">
            <label htmlFor="email" className="block mb-2 text-gray-600 font-medium text-sm">Email</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              required
              className="w-full px-3 py-3 border border-gray-300 rounded-lg text-sm transition-colors focus:outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-200"
            />
          </div>
          <div className="mb-5">
            <label htmlFor="password" className="block mb-2 text-gray-600 font-medium text-sm">Password</label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              minLength="6"
              required
              className="w-full px-3 py-3 border border-gray-300 rounded-lg text-sm transition-colors focus:outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-200"
            />
          </div>
          <button type="submit" className="w-full px-3 py-3 bg-gradient-to-r from-blue-600 to-blue-700 text-white rounded-lg text-base font-semibold cursor-pointer transition-transform hover:-translate-y-0.5 shadow-lg">Sign Up</button>
        </form>
        <p className="text-center mt-5 text-gray-600 text-sm">
          Already have an account? <Link to="/login" className="text-blue-600 font-semibold hover:underline">Login</Link>
        </p>
      </div>
    </div>
  );
};

export default Signup;
