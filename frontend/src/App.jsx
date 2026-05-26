import {Routes, Route, Navigate, Link, useNavigate} from 'react-router-dom';
import { useState, useEffect } from 'react';
import Login from './pages/Login';
import Feed from './pages/Feed';
import Wallet from './pages/Wallet';
import UserProfile from './pages/UserProfile';
import api from './api/axios';
import { Coins, LogOut, Menu, X } from 'lucide-react';

export default function App() {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        api.get('/api/auth/me')
            .then(res => setUser(res.data))
            .catch(() => setUser(null))
            .finally(() => setLoading(false));
    }, []);

    if (loading) {
        return <div className="flex h-screen items-center justify-center bg-gray-50"><div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600"></div></div>;
    }

    return (
        <div className="min-h-screen bg-gray-50 font-sans">
            {/* === NAVBAR === */}
            <nav className="sticky top-0 z-40 bg-white/80 backdrop-blur-md border-b border-gray-200">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex items-center justify-between h-16">
                        <div className="flex items-center gap-2 cursor-pointer" onClick={() => window.location.href = '/'}>
                            <div className="bg-purple-600 text-white p-2 rounded-lg shadow-sm"><Coins size={20} /></div>
                            <span className="text-xl font-bold bg-gradient-to-r from-purple-600 to-indigo-600 bg-clip-text text-transparent">BarterSkills</span>
                        </div>

                        {user && (
                            <div className="flex items-center gap-3">
                                <Link to="/wallet" className="hidden sm:flex items-center gap-2 bg-purple-50 hover:bg-purple-100 px-3 py-1.5 rounded-full border border-purple-100 transition cursor-pointer text-purple-700">
                                    <Coins size={16} />
                                    <span className="font-semibold">{user.skillCoins || 0}</span>
                                    <span className="text-xs font-medium">SC</span>
                                </Link>

                                <button
                                    onClick={() => navigate(`/user/${user.id}`)} // Ведет на твой собственный профиль
                                    className="w-10 h-10 rounded-full bg-purple-100 text-purple-600 flex items-center justify-center font-bold text-sm hover:bg-purple-200 hover:scale-105 transition-all shadow-sm"
                                >
                                    {user.firstName?.[0]}{user.lastName?.[0] || ''}
                                </button>

                                <button onClick={() => setUser(null)}
                                        className="hidden sm:flex items-center gap-1 text-gray-500 hover:text-red-600 transition p-2">
                                    <LogOut size={18}/>
                                </button>

                                <button className="sm:hidden p-2 text-gray-600" onClick={() => setMobileMenuOpen(!mobileMenuOpen)}>
                                    {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
                                </button>
                            </div>
                        )}
                    </div>
                </div>

                {mobileMenuOpen && user && (
                    <div className="sm:hidden bg-white border-t border-gray-100 p-4 space-y-3">
                        <Link to="/wallet" className="flex items-center justify-between bg-purple-50 px-4 py-3 rounded-xl">
                            <span className="font-medium text-purple-800 flex items-center gap-2"><Coins size={18}/> Баланс</span>
                            <span className="font-bold text-purple-600">{user.skillCoins || 0} SC</span>
                        </Link>
                        <button onClick={() => { setUser(null); setMobileMenuOpen(false); }} className="w-full flex items-center justify-center gap-2 text-red-600 bg-red-50 py-2 rounded-lg">
                            <LogOut size={18} /> Выйти
                        </button>
                    </div>
                )}
            </nav>

            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <Routes>
                    <Route path="/login" element={!user ? <Login setUser={setUser} /> : <Navigate to="/" replace />} />
                    <Route path="/" element={user ? <Feed user={user} setUser={setUser} /> : <Navigate to="/login" replace />} />
                    <Route path="/wallet" element={user ? <Wallet user={user} /> : <Navigate to="/login" replace />} />
                    <Route path="/user/:userId" element={user ? <UserProfile currentUserId={user.id} /> : <Navigate to="/login" replace />} />

                    <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
            </main>
        </div>
    );
}