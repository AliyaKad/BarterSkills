import { Routes, Route, Navigate, Link, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import Login from './pages/Login';
import Feed from './pages/Feed';
import Wallet from './pages/Wallet';
import UserProfile from './pages/UserProfile';
import CreateRequest from './pages/CreateRequest';
import CreateOffer from './pages/CreateOffer';
import OfferDetail from './pages/OfferDetail';
import RequestDetail from './pages/RequestDetail';
import Deals from './pages/Deals';
import DealDetail from './pages/DealDetail';
import api from './api/axios';
import { fetchMe, logout } from './api/marketplace';
import { Coins, LogOut, Menu, X, Handshake } from 'lucide-react';

export default function App() {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
    const navigate = useNavigate();

    const refreshUser = () =>
        fetchMe().then(res => setUser(res.data)).catch(() => setUser(null));

    useEffect(() => {
        fetchMe()
            .then(res => setUser(res.data))
            .catch(() => setUser(null))
            .finally(() => setLoading(false));
    }, []);

    const handleLogout = async () => {
        try { await logout(); } catch { /* ignore */ }
        setUser(null);
        navigate('/login');
    };

    if (loading) {
        return <div className="flex h-screen items-center justify-center bg-gray-50">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600" />
        </div>;
    }

    return (
        <div className="min-h-screen bg-gray-50 font-sans">
            <nav className="sticky top-0 z-40 bg-white/80 backdrop-blur-md border-b">
                <div className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
                    <Link to="/" className="flex items-center gap-2">
                        <div className="bg-purple-600 text-white p-2 rounded-lg"><Coins size={20} /></div>
                        <span className="text-xl font-bold bg-gradient-to-r from-purple-600 to-indigo-600 bg-clip-text text-transparent">BarterSkills</span>
                    </Link>
                    {user && (
                        <div className="flex items-center gap-3">
                            <Link to="/deals" className="hidden sm:flex items-center gap-1 text-gray-600 hover:text-purple-600 text-sm font-medium">
                                <Handshake size={18} /> Сделки
                            </Link>
                            <Link to="/wallet" className="hidden sm:flex items-center gap-2 bg-purple-50 px-3 py-1.5 rounded-full text-purple-700 font-semibold">
                                <Coins size={16} /> {user.skillCoins || 0} SC
                            </Link>
                            <button onClick={() => navigate(`/user/${user.id}`)}
                                    className="w-10 h-10 rounded-full bg-purple-100 text-purple-600 font-bold text-sm">
                                {user.firstName?.[0]}{user.lastName?.[0] || ''}
                            </button>
                            <button onClick={handleLogout} className="hidden sm:block p-2 text-gray-500 hover:text-red-600">
                                <LogOut size={18} />
                            </button>
                            <button className="sm:hidden p-2" onClick={() => setMobileMenuOpen(!mobileMenuOpen)}>
                                {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
                            </button>
                        </div>
                    )}
                </div>
                {mobileMenuOpen && user && (
                    <div className="sm:hidden border-t p-4 space-y-2">
                        <Link to="/deals" className="block py-2" onClick={() => setMobileMenuOpen(false)}>Сделки</Link>
                        <Link to="/wallet" className="block py-2" onClick={() => setMobileMenuOpen(false)}>Кошелёк ({user.skillCoins} SC)</Link>
                        <button onClick={handleLogout} className="text-red-600">Выйти</button>
                    </div>
                )}
            </nav>

            <main className="max-w-7xl mx-auto px-4 py-8">
                <Routes>
                    <Route path="/login" element={!user ? <Login setUser={setUser} /> : <Navigate to="/" replace />} />
                    <Route path="/" element={user ? <Feed user={user} /> : <Navigate to="/login" replace />} />
                    <Route path="/wallet" element={user ? <Wallet user={user} onUserUpdate={refreshUser} /> : <Navigate to="/login" replace />} />
                    <Route path="/user/:userId" element={user ? <UserProfile currentUser={user} /> : <Navigate to="/login" replace />} />
                    <Route path="/requests/new" element={user ? <CreateRequest /> : <Navigate to="/login" replace />} />
                    <Route path="/offers/new" element={user ? <CreateOffer /> : <Navigate to="/login" replace />} />
                    <Route path="/offers/:id" element={user ? <OfferDetail user={user} /> : <Navigate to="/login" replace />} />
                    <Route path="/requests/:id" element={user ? <RequestDetail user={user} /> : <Navigate to="/login" replace />} />
                    <Route path="/deals" element={user ? <Deals /> : <Navigate to="/login" replace />} />
                    <Route path="/deals/:id" element={user ? <DealDetail user={user} /> : <Navigate to="/login" replace />} />
                    <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
            </main>
        </div>
    );
}
