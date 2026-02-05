import { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { GoogleOAuthProvider } from '@react-oauth/google';
import LoginPage from './LoginPage';
import SignupPage from './SignupPage';
import InterviewPage from './InterviewPage';
import RecruiterDashboard from './RecruiterDashboard';
import './App.css';

// Protected Route Wrapper
const ProtectedRoute = ({ children }) => {
  const token = localStorage.getItem('token');
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return children;
};

// Helper for color coding scores
function getScoreColor(score) {
  if (score >= 80) return '#4caf50'; // Green
  if (score >= 60) return '#ff9800'; // Orange
  return '#f44336'; // Red
}

function ResumeAnalyze() {
  const [file, setFile] = useState(null);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  /* ------------------- NEW STATES FOR ADVANCED FEATURES ------------------- */
  const [jobDescription, setJobDescription] = useState("");
  const [targetRole, setTargetRole] = useState("Software Engineer");
  const [tailoredResume, setTailoredResume] = useState(null);
  const [interviewQuestions, setInterviewQuestions] = useState(null);
  const [loadingAction, setLoadingAction] = useState(false);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  const handleFileChange = (e) => {
    setFile(e.target.files[0]);
    setError(null);
  };

  const handleUpload = async () => {
    if (!file) {
      setError("Please select a file first.");
      return;
    }

    setLoading(true);
    setError(null);
    setResult(null);
    setTailoredResume(null);
    setInterviewQuestions(null);

    const formData = new FormData();
    formData.append('file', file);
    if (jobDescription) {
      formData.append('jobDescription', jobDescription);
    }

    const token = localStorage.getItem('token');

    try {
      const response = await fetch('http://localhost:8080/api/resumes/upload', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formData,
      });

      if (response.status === 401 || response.status === 403) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        navigate('/login');
        return;
      }

      if (!response.ok) {
        throw new Error(`Upload failed: ${response.statusText}`);
      }

      const data = await response.json();
      setResult(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  /* ------------------- NEW HANDLERS ------------------- */
  const handleTailorResume = async () => {
    if (!result || !result.id) return;
    setLoadingAction(true);
    const token = localStorage.getItem('token');
    try {
      const response = await fetch(`http://localhost:8080/api/resumes/${result.id}/tailor?targetRole=${targetRole}`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (!response.ok) throw new Error("Failed to tailor resume");
      const text = await response.text();
      setTailoredResume(text);
      setTimeout(() => document.getElementById('tailored-result')?.scrollIntoView({ behavior: 'smooth' }), 100);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoadingAction(false);
    }
  };

  const handleGenerateQuestions = async () => {
    if (!result || !result.id) return;
    setLoadingAction(true);
    const token = localStorage.getItem('token');
    try {
      const response = await fetch(`http://localhost:8080/api/resumes/${result.id}/interview-questions?targetRole=${targetRole}`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (!response.ok) throw new Error("Failed to generate questions");
      const text = await response.text();
      setInterviewQuestions(text);
      setTimeout(() => document.getElementById('interview-result')?.scrollIntoView({ behavior: 'smooth' }), 100);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoadingAction(false);
    }
  };

  return (
    <div className="container">
      <header className="hero">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h1>Resume Analyser AI</h1>
          <div style={{ display: 'flex', gap: '1rem' }}>
            <button onClick={() => navigate('/')} className="logout-btn">Dashboard</button>
            <button onClick={handleLogout} className="logout-btn">Logout</button>
          </div>
        </div>
        <p>Optimize your resume with AI-powered feedback.</p>
      </header>

      <main>
        <div className="upload-card">
          <div className="upload-section">
            <input
              type="file"
              accept=".pdf,.docx"
              onChange={handleFileChange}
              id="file-upload"
              className="file-input"
            />
            <label htmlFor="file-upload" className="file-label">
              {file ? file.name : "📄 Choose Resume (PDF)"}
            </label>
          </div>

          <textarea
            className="jd-input"
            placeholder="[Optional] Paste Job Description here for checking Match Score..."
            value={jobDescription}
            onChange={(e) => setJobDescription(e.target.value)}
            rows={4}
            style={{ marginTop: '1rem' }}
          />

          <div className="action-row" style={{ marginTop: '1rem' }}>
            <button
              onClick={handleUpload}
              disabled={loading || !file}
              className="upload-btn"
            >
              {loading ? "Analyzing..." : "Analyze Resume"}
            </button>
          </div>
        </div>

        {error && <div className="error-message">{error}</div>}

        {result && (
          <div className="result-section">
            <div className="metrics-grid">
              {/* Internal ATS Score */}
              <div className="score-card">
                <h2>ATS Score</h2>
                <div className="score-circle" style={{
                  background: `conic-gradient(${getScoreColor(result.atsScore)} ${result.atsScore * 3.6}deg, #333 0deg)`
                }}>
                  <span>{result.atsScore}/100</span>
                </div>
                <p className="metric-label">Resume Strength</p>
              </div>

              {/* Match Score (if JD provided) */}
              {result.matchScore !== undefined && result.matchScore !== null && (
                <div className="score-card">
                  <h2>Match Score</h2>
                  <div className="score-circle" style={{
                    background: `conic-gradient(${getScoreColor(result.matchScore)} ${result.matchScore * 3.6}deg, #333 0deg)`
                  }}>
                    <span>{result.matchScore}%</span>
                  </div>
                  <p className="metric-label">JD Match</p>
                </div>
              )}
            </div>

            {/* Missing Skills Alert */}
            {result.missingSkills && (
              <div className="alert-card warning" style={{ marginTop: '1.5rem' }}>
                <h3>⚠️ Missing Skills</h3>
                <p>{result.missingSkills}</p>
              </div>
            )}

            {/* Detailed Feedback Grid */}
            <div className="feedback-grid" style={{ marginTop: '1.5rem' }}>
              <div className="feedback-card">
                <h3>📝 AI Feedback</h3>
                <p>{result.feedback}</p>
              </div>

              <div className="feedback-card">
                <h3>🔍 Insights</h3>
                <ul className="insights-list">
                  <li><strong>Skills Coverage:</strong> {result.skillsCoverage}%</li>
                  <li><strong>Experience Quality:</strong> {result.experienceQuality}</li>
                  {result.formattingIssues && result.formattingIssues !== "None" && (
                    <li><strong>Formatting:</strong> {result.formattingIssues}</li>
                  )}
                </ul>
              </div>
            </div>

            {/* ------------------- ADVANCED FEATURES SECTION ------------------- */}
            <div className="advanced-features">
              <h2>🚀 Advanced Role Customization</h2>

              <div className="role-selector">
                <label>Target Role (for tailoring):</label>
                <select value={targetRole} onChange={(e) => setTargetRole(e.target.value)}>
                  <option value="Software Engineer">Software Engineer</option>
                  <option value="Data Engineer">Data Engineer</option>
                  <option value="ML Engineer">ML Engineer</option>
                  <option value="Data Analyst">Data Analyst</option>
                  <option value="Product Manager">Product Manager</option>
                </select>
              </div>

              <div className="advanced-buttons">
                <button
                  onClick={handleTailorResume}
                  disabled={loadingAction}
                  className="secondary-btn"
                >
                  {loadingAction ? "Processing..." : "✨ Tailor Resume for Role"}
                </button>
              </div>

              {/* Tailored Resume Output */}
              {tailoredResume && (
                <div id="tailored-result" className="output-card">
                  <h3>✨ Tailored Resume Content</h3>
                  <pre className="content-pre">{tailoredResume}</pre>
                  <button className="copy-btn" onClick={() => navigator.clipboard.writeText(tailoredResume)}>Copy to Clipboard</button>
                </div>
              )}

              {/* Automated Interview Questions Output */}
              {result.interviewQuestions && (
                <div id="interview-result" className="output-card">
                  <h3>📚 Recommended Interview Questions (Automated)</h3>
                  <pre className="content-pre">{result.interviewQuestions}</pre>
                </div>
              )}
            </div>

          </div>
        )}
      </main>
    </div>
  );
}

function App() {
  // Replace with your actual Google Client ID
  const GOOGLE_CLIENT_ID = "YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com";

  return (
    <GoogleOAuthProvider clientId={GOOGLE_CLIENT_ID}>
      <Router>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <RecruiterDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/analyze"
            element={
              <ProtectedRoute>
                <ResumeAnalyze />
              </ProtectedRoute>
            }
          />
          <Route
            path="/interview/:token"
            element={<InterviewPage />}
          />
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </Router>
    </GoogleOAuthProvider>
  );
}

export default App;
