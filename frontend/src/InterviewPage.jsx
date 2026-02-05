import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import './App.css';

function InterviewPage() {
    const { token } = useParams();
    const navigate = useNavigate();
    const videoRef = useRef(null);
    const recognition = useRef(null);
    const shouldListen = useRef(false); // Flag for hands-free loop

    // Session State
    const [messages, setMessages] = useState([]);
    const [isListening, setIsListening] = useState(false);
    const [isSpeaking, setIsSpeaking] = useState(false);
    const [transcript, setTranscript] = useState("");
    const [status, setStatus] = useState("Initializing...");
    const [result, setResult] = useState(null); // { score, feedback }
    const [hasStarted, setHasStarted] = useState(false);

    // Speech Recognition Setup
    useEffect(() => {
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        if (SpeechRecognition) {
            recognition.current = new SpeechRecognition();
            recognition.current.continuous = false; // We restart manually for better control
            recognition.current.lang = 'en-US';

            recognition.current.onstart = () => {
                setIsListening(true);
                setStatus("Listening... Speak now");
            };

            recognition.current.onresult = (event) => {
                const text = event.results[0][0].transcript;
                setTranscript(text);
                // Auto-submit after silence or short pause? 
                // For now, let's wait for the "end" event or a specific silence timeout. 
                // Actually, standard SpeechRecognition stops automatically when user stops speaking.
                // We will handle the submission in onend if transcript is present.
            };

            recognition.current.onerror = (event) => {
                console.error("Speech Error:", event.error);
                setIsListening(false);
                if (event.error === 'no-speech' && shouldListen.current) {
                    // unexpected silence, restart
                    try { recognition.current.start(); } catch (e) { }
                }
            };

            recognition.current.onend = () => {
                setIsListening(false);
                // If we have a transcript, submit it.
                // NOTE: handling state inside event listeners is tricky with closures. 
                // We might need a ref for the latest transcript or just rely on the event.
                // But onresult fires before onend.
            };
        } else {
            alert("Speech Recognition not supported. Please use Chrome.");
        }

        startCamera();
        // Check session validity?
        checkSession();

        return () => stopCamera();
    }, []);

    // Effect to handle submission when listening stops and we have text
    useEffect(() => {
        if (!isListening && transcript && shouldListen.current) {
            // User stopped speaking, submit answer
            handleUserResponse(transcript);
            setTranscript("");
        } else if (!isListening && shouldListen.current && !isSpeaking) {
            // Restart listening if nothing was captured (and we should be listening)
            // setTimeout(() => { try { recognition.current.start(); } catch(e){} }, 500);
        }
    }, [isListening]);


    const startCamera = async () => {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false });
            if (videoRef.current) {
                videoRef.current.srcObject = stream;
            }
        } catch (err) {
            console.error("Camera Error:", err);
            setStatus("Camera access denied.");
        }
    };

    const stopCamera = () => {
        if (videoRef.current && videoRef.current.srcObject) {
            videoRef.current.srcObject.getTracks().forEach(track => track.stop());
            videoRef.current.srcObject = null;
        }
    };

    const checkSession = async () => {
        try {
            const res = await fetch(`http://localhost:8080/api/public/interview/${token}`);
            if (!res.ok) {
                setStatus("Invalid or Expired Session");
                return;
            }
            const data = await res.json();
            if (data.status === 'COMPLETED') {
                setResult({ score: data.overallScore || 'N/A', feedback: "Interview Completed" });
            } else {
                setStatus("Ready to Start");
            }
        } catch (err) {
            setStatus("Error connecting to server");
        }
    };

    const startInterview = async () => {
        setHasStarted(true);
        try {
            const res = await fetch(`http://localhost:8080/api/public/interview/${token}/start`, {
                method: 'POST'
            });
            if (!res.ok) throw new Error("Failed to start");
            const data = await res.json();
            addMessage('AI', data.message);
            speak(data.message);
        } catch (err) {
            console.error(err);
            setStatus("Error starting interview.");
        }
    };

    const handleUserResponse = async (text) => {
        shouldListen.current = false; // Stop listening loop
        addMessage('User', text);
        setStatus("AI Thinking...");

        try {
            const res = await fetch(`http://localhost:8080/api/public/interview/${token}/answer`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ answer: text })
            });
            const data = await res.json();
            addMessage('AI', data.message);
            speak(data.message);
        } catch (err) {
            console.error(err);
            setStatus("Error sending message.");
        }
    };

    const addMessage = (sender, text) => {
        setMessages(prev => [...prev, { sender, text }]);
    };

    const speak = (text) => {
        if ('speechSynthesis' in window) {
            setIsSpeaking(true);
            const utterance = new SpeechSynthesisUtterance(text);
            // Select a good voice if available
            const voices = window.speechSynthesis.getVoices();
            const preferredVoice = voices.find(v => v.name.includes('Google US English')) || voices[0];
            if (preferredVoice) utterance.voice = preferredVoice;

            utterance.onend = () => {
                setIsSpeaking(false);
                // User turn to speak
                shouldListen.current = true;
                setStatus("Listening...");
                try { recognition.current.start(); } catch (e) { }
            };
            window.speechSynthesis.speak(utterance);
        }
    };

    if (result) {
        return (
            <div className="interview-container" style={{ textAlign: 'center', color: 'white' }}>
                <h2>Interview Complete 🏁</h2>
                <div className="score-card" style={{ background: '#1a1a1a', padding: '2rem', borderRadius: '12px' }}>
                    <p style={{ fontSize: '1.2rem', margin: '1rem 0' }}>Thank you for completing the interview.</p>
                    <p>The recruiter will Review your performance.</p>
                </div>
            </div>
        );
    }

    return (
        <div className="interview-container">
            <header className="interview-header">
                <h2>AI Video Interview</h2>
            </header>

            <div className="video-area">
                <video ref={videoRef} autoPlay playsInline muted className="webcam-feed" />
                <div className={`ai-avatar ${isSpeaking ? 'speaking' : ''}`}>
                    <div className="pulse-ring"></div>
                    <span>AI Interviewer</span>
                </div>
            </div>

            <div className="controls-area">
                <div className="status-bar">{status}</div>
                {transcript && <div className="transcript-preview">"{transcript}"</div>}

                {!hasStarted ? (
                    <button className="primary-btn" onClick={startInterview}>Start Interview</button>
                ) : (
                    <div style={{ marginTop: '10px' }}>
                        {!isListening && !isSpeaking && (
                            <button className="mic-btn" onClick={() => {
                                shouldListen.current = true;
                                try { recognition.current.start(); } catch (e) { }
                            }}>
                                🎤 Resume Speaking (Tap if stuck)
                            </button>
                        )}
                    </div>
                )}
            </div>

            <div className="chat-log">
                {messages.map((msg, idx) => (
                    <div key={idx} className={`message ${msg.sender.toLowerCase()}`}>
                        <strong>{msg.sender}:</strong> {msg.text}
                    </div>
                ))}
            </div>
        </div>
    );
}

export default InterviewPage;
