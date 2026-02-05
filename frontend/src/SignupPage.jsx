import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import './App.css';

function SignupPage() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [name, setName] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleSignup = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch('http://localhost:8080/api/auth/signup', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password, name }),
            });

            if (!response.ok) throw new Error('Registration failed');

            alert("Account created! Please login.");
            navigate('/login');
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <div className="container" style={{ maxWidth: '400px' }}>
            <h1>Create Account</h1>
            <form onSubmit={handleSignup} className="auth-form">
                <input
                    type="text"
                    placeholder="Full Name"
                    value={name} onChange={(e) => setName(e.target.value)}
                    className="auth-input"
                    required
                />
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
                <button type="submit" className="upload-btn">Sign Up</button>
            </form>

            {error && <p className="error-message">{error}</p>}

            <p style={{ marginTop: '2rem' }}>
                Already user? <Link to="/login">Login</Link>
            </p>
        </div>
    );
}

export default SignupPage;
