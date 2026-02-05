import { useState } from 'react';
import { GoogleLogin } from '@react-oauth/google';
import { useNavigate, Link } from 'react-router-dom';
import './App.css';

function LoginPage() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch('http://localhost:8080/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password }),
            });

            if (!response.ok) throw new Error('Invalid credentials');

            const data = await response.json();
            localStorage.setItem('token', data.token);
            localStorage.setItem('user', data.name);
            navigate('/');
        } catch (err) {
            setError(err.message);
        }
    };

    const handleGoogleSuccess = (credentialResponse) => {
        // Determine how to handle Google Token.
        // In a real app, send `credentialResponse.credential` to backend 
        // to verify and exchange for app JWT. For this demo, we simulate success.

        // For now, we just redirect as if logged in since we need backend OAuth2 setup to fully verify
        // or we can use the credential to decode and store basic info.
        console.log(credentialResponse);
        localStorage.setItem('token', 'google-mock-token');
        navigate('/');
    };

    return (
        <div className="container" style={{ maxWidth: '400px' }}>
            <h1>Welcome Back</h1>
            <form onSubmit={handleLogin} className="auth-form">
                <input
                    type="email"
                    placeholder="Email"
                    value={email} onChange={(e) => setEmail(e.target.value)}
                    className="auth-input"
                    required
                />
                <input
                    type="password"
                    placeholder="Password"
                    value={password} onChange={(e) => setPassword(e.target.value)}
                    className="auth-input"
                    required
                />
                <button type="submit" className="upload-btn">Login</button>
            </form>

            {error && <p className="error-message">{error}</p>}

            <div className="divider">OR</div>

            <div className="google-btn-wrapper">
                <GoogleLogin
                    onSuccess={handleGoogleSuccess}
                    onError={() => setError('Google Login Failed')}
                />
            </div>

            <p style={{ marginTop: '2rem' }}>
                Don't have an account? <Link to="/signup">Sign up</Link>
            </p>
        </div>
    );
}

export default LoginPage;
