import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './login.css'; 
import LoginForm from './LoginForm';

function Login() {
  const [isRegisteredUser, setRegisteredUser] = useState(true);
  const [user, setUser] = useState(null);
  
  // Registration Form States
  const [regUsername, setRegUsername] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [regMessage, setRegMessage] = useState({ text: '', isError: false });

  // Initial Read: Automatic login check from LocalStorage or SessionStorage
  useEffect(() => {
    const activeSession = localStorage.getItem('user') || sessionStorage.getItem('user');
    if (activeSession) {
      setUser(JSON.parse(activeSession));
    }
  }, []);

  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    setRegMessage({ text: '', isError: false });

    try {
      // WRITE Operation: Posts data to Database via Java Backend
      // FORCE the payload into a clean JSON string for your pure Java backend
const response = await axios.post(
  'http://localhost:5000/api/register', 
  JSON.stringify({ username: regUsername, password: regPassword }), 
  {
    headers: {
      'Content-Type': 'application/json'
    }
  }
);

      setRegMessage({ text: response.data.message || 'Registration successful! Please login.', isError: false });
      setRegUsername('');
      setRegPassword('');
      setTimeout(() => setRegisteredUser(true), 2000); // Redirect to login panel
    } catch (err) {
      setRegMessage({ text: err.response?.data?.error || 'Registration failed.', isError: true });
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('user');
    sessionStorage.removeItem('user');
    setUser(null);
  };

  // If user state is found, skip login view entirely
  if (user) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100vh', gap: '20px' }}>
        <h1>Welcome back, {user.username}! 🎉</h1>
        <p>You have successfully logged in via pure endpoints.</p>
        <button onClick={handleLogout} className="login-btn" style={{ maxWidth: '200px' }}>Log Out</button>
      </div>
    );
  }

  return (
    <div className="portal-wrapper">
      <div className="login-container">
        
        {/* Left Side: High-Energy Travel Showcase Section */}
        <div className="app-showcase">
          <div className="showcase-content">
            <h1>Discover.</h1>
            <h1>Journey.</h1>
            <h1>Unbound.</h1>
            <p className="motive-text">
              Ready to explore? Step past the platform and into your next grand adventure. Log in now to track your transits, unlock premium routes, and see where the lines take you today!
            </p>
          </div>
        </div>

        {/* Right Side: Conditional Toggle Panel */}
        {isRegisteredUser ? (
          <LoginForm 
            onSwitchView={() => setRegisteredUser(false)} 
            onAuthSuccess={(userData) => setUser(userData)}
          />
        ) : (
          <div className="form-section">
            <h2>Create Account</h2>
            {regMessage.text && (
              <p style={{ color: regMessage.isError ? '#ff4d4d' : '#4df0ff', fontSize: '14px', marginBottom: '10px' }}>
                {regMessage.text}
              </p>
            )}
            <form onSubmit={handleRegisterSubmit}>
              <div className="input-group">
                <label>Desired Username</label>
                <input 
                  type="text" 
                  value={regUsername} 
                  onChange={(e) => setRegUsername(e.target.value)} 
                  placeholder="Create username" 
                  required 
                />
              </div>
              <div className="input-group">
                <label>Password</label>
                <input 
                  type="password" 
                  value={regPassword} 
                  onChange={(e) => setRegPassword(e.target.value)} 
                  placeholder="••••••••" 
                  required 
                />
              </div>
              <button type="submit" className="login-btn">Register Account</button>
              
              <button 
                type="button" 
                className="state-button" 
                style={{ marginTop: '15px', display: 'block', width: '100%', textAlign: 'center' }}
                onClick={() => setRegisteredUser(true)}
              >
                Go Back to Login
              </button>
            </form>
          </div>
        )}

      </div>
    </div>
  );
}

export default Login;
