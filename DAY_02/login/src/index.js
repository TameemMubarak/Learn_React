  import React from 'react';
  import ReactDOM from 'react-dom/client';
  import './index.css';

  import Login from './Login'; // Adjusted structural name match

  const root = ReactDOM.createRoot(document.getElementById('root'));
  root.render(
    <React.StrictMode>
      <Login />
    </React.StrictMode>
  );
