import React, { useState } from 'react';
import axios from 'axios';
import './login.css';

function LoginForm({ onSwitchView, onAuthSuccess }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');

    try {
      // READ operation: sending parameters to Java server to read validation records
      const response = await axios.post('http://localhost:5000/api/login', { username, password });
      const userData = response.data; // Server returns { username: "...", token: "..." }

      // WRITE operation to browser storages based on user preference
      if (rememberMe) {
        localStorage.setItem('user', JSON.stringify(userData));
      } else {
        sessionStorage.setItem('user', JSON.stringify(userData));
      }

      onAuthSuccess(userData);
    } catch (err) {
      setError(err.response?.data?.error || 'Invalid credentials or connection error');
    }
  };

  return (
    <div className="form-section">
      <h2>Welcome Back</h2>
      {error && <p style={{ color: '#ff4d4d', fontSize: '14px', marginBottom: '10px' }}>{error}</p>}
      
      <form onSubmit={handleLogin}>
        <div className="input-group">
          <label htmlFor="username">Username</label>
          <input 
            type="text" 
            id="username" 
            placeholder="Enter your username" 
            value={username} 
            onChange={(e) => setUsername(e.target.value)} 
            required 
          />
        </div>
        <div className="input-group">
          <label htmlFor="password">Password</label>
          <input 
            type="password" 
            id="password" 
            placeholder="••••••••" 
            value={password} 
            onChange={(e) => setPassword(e.target.value)} 
            required 
          />
        </div>

        {/* LocalStorage / SessionStorage Choice Hook */}
        <div className="input-group-checkbox" style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '15px' }}>
          <input 
            type="checkbox" 
            id="rememberMe" 
            checked={rememberMe} 
            onChange={(e) => setRememberMe(e.target.checked)} 
          />
          <label htmlFor="rememberMe" style={{ fontSize: '13px', cursor: 'pointer' }}>Keep me logged in (LocalStorage)</label>
        </div>

        <button type="submit" className="login-btn">Start Exploring</button>
        
        <p className="switch-text">
          Don't have an account?{' '}
          <button type="button" className="state-button" onClick={onSwitchView}>
            Register Now
          </button>
        </p>
      </form>
    </div>
  );
}

export default LoginForm;
