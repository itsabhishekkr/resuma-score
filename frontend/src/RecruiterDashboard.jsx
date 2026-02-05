import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './App.css';

export default function RecruiterDashboard() {
    const [resumes, setResumes] = useState([]);
    const [filter, setFilter] = useState('all'); // 'all' or 'qualified'
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        fetchResumes();
    }, [filter]);

    const fetchResumes = async () => {
        setLoading(true);
        const token = localStorage.getItem('token');
        const endpoint = filter === 'qualified'
            ? 'http://localhost:8080/api/resumes/qualified?minScore=70'
            : 'http://localhost:8080/api/resumes';

        try {
            const response = await fetch(endpoint, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.status === 401 || response.status === 403) {
                localStorage.removeItem('token');
                localStorage.removeItem('user');
                navigate('/login');
                return;
            }

            if (!response.ok) throw new Error('Failed to fetch resumes');
            const data = await response.json();
            setResumes(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const stats = {
        total: resumes.length,
        qualified: resumes.filter(r => r.matchScore >= 70).length,
        avgScore: resumes.length > 0
            ? Math.round(resumes.reduce((acc, r) => acc + (r.matchScore || 0), 0) / resumes.length)
            : 0
    };

    return (
        <div className="container">
            <header className="hero">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <h1>Recruiter Dashboard</h1>
                    <button onClick={() => navigate('/analyze')} className="upload-btn" style={{ padding: '0.6rem 1.2rem', fontSize: '1rem', textDecoration: 'none' }}>
                        Check Resume
                    </button>
                </div>
                <p>Overview of incoming candidates and their AI evaluation.</p>
            </header>

            <div className="metrics-grid" style={{ marginTop: '2rem' }}>
                <div className="score-card">
                    <h2>{stats.total}</h2>
                    <p className="metric-label">Total Candidates</p>
                </div>
                <div className="score-card">
                    <h2 style={{ color: '#4caf50' }}>{stats.qualified}</h2>
                    <p className="metric-label">Qualified (&gt;70%)</p>
                </div>
                <div className="score-card">
                    <h2>{stats.avgScore}%</h2>
                    <p className="metric-label">Average Match Score</p>
                </div>
            </div>

            <div className="upload-card" style={{ alignItems: 'flex-start' }}>
                <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem' }}>
                    <button
                        className={`secondary-btn ${filter === 'all' ? 'active' : ''}`}
                        style={{ backgroundColor: filter === 'all' ? '#646cff' : '' }}
                        onClick={() => setFilter('all')}
                    >
                        All Candidates
                    </button>
                    <button
                        className={`secondary-btn ${filter === 'qualified' ? 'active' : ''}`}
                        style={{ backgroundColor: filter === 'qualified' ? '#4caf50' : '' }}
                        onClick={() => setFilter('qualified')}
                    >
                        Qualified Only
                    </button>
                </div>

                {loading ? (
                    <p>Loading candidates...</p>
                ) : error ? (
                    <p className="error-message">{error}</p>
                ) : (
                    <div style={{ width: '100%', overflowX: 'auto' }}>
                        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                            <thead>
                                <tr style={{ borderBottom: '1px solid #444' }}>
                                    <th style={{ padding: '1rem' }}>Candidate Email</th>
                                    <th style={{ padding: '1rem' }}>Job Role</th>
                                    <th style={{ padding: '1rem' }}>ATS Score</th>
                                    <th style={{ padding: '1rem' }}>Match Score</th>
                                    <th style={{ padding: '1rem' }}>Interview Score</th>
                                </tr>
                            </thead>
                            <tbody>
                                {resumes.map(resume => (
                                    <tr key={resume.id} style={{ borderBottom: '1px solid #333' }}>
                                        <td style={{ padding: '1rem' }}>{resume.email || "N/A"}</td>
                                        <td style={{ padding: '1rem' }}>{resume.jobDescription ? "Specific Role" : "General"}</td>
                                        <td style={{ padding: '1rem' }}>
                                            <span style={{
                                                color: resume.atsScore >= 70 ? '#4caf50' : '#ff9800',
                                                fontWeight: 'bold'
                                            }}>
                                                {resume.atsScore}
                                            </span>
                                        </td>
                                        <td style={{ padding: '1rem' }}>
                                            {resume.matchScore ? (
                                                <span style={{
                                                    padding: '0.2rem 0.5rem',
                                                    borderRadius: '4px',
                                                    backgroundColor: resume.matchScore >= 70 ? 'rgba(76, 175, 80, 0.2)' : 'rgba(255, 152, 0, 0.2)',
                                                    color: resume.matchScore >= 70 ? '#4caf50' : '#ff9800'
                                                }}>
                                                    {resume.matchScore}%
                                                </span>
                                            ) : "-"}
                                        </td>
                                        <td style={{ padding: '1rem' }}>
                                            {resume.interviewScore ? resume.interviewScore + "/100" : "Pending"}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                        {resumes.length === 0 && <p style={{ padding: '2rem', textAlign: 'center', color: '#666' }}>No candidates found.</p>}
                    </div>
                )}
            </div>
        </div>
    );
}
