import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, MapPin, Star, MessageCircle } from 'lucide-react';
import ReviewList from '../components/ReviewList';
import ChatModal from '../components/ChatModal';
import api from '../api/axios';

export default function UserProfile({ currentUser }) {
    const { userId } = useParams();
    const navigate = useNavigate();
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [chatOpen, setChatOpen] = useState(false);

    useEffect(() => {
        api.get(`/api/user/${userId}`)
            .then(res => { setUser(res.data); setError(''); })
            .catch(() => setError('Пользователь не найден'))
            .finally(() => setLoading(false));
    }, [userId]);

    if (loading) return <div className="flex justify-center py-20"><div className="animate-spin h-10 w-10 border-b-2 border-purple-600 rounded-full" /></div>;
    if (error) return <p className="text-red-500 text-center">{error}</p>;
    if (!user) return null;

    const isOwnProfile = user.id === currentUser.id;
    const fullName = `${user.firstName || ''} ${user.lastName || ''}`.trim();

    return (
        <div className="max-w-4xl mx-auto space-y-6">
            <button onClick={() => navigate(-1)} className="flex items-center gap-2 text-gray-600 hover:text-purple-600">
                <ArrowLeft size={20} /> Назад
            </button>

            <div className="bg-white rounded-2xl border p-6 shadow-sm">
                <div className="flex flex-col sm:flex-row gap-6 items-start">
                    <div className="w-24 h-24 rounded-full bg-gradient-to-br from-purple-500 to-indigo-600 flex items-center justify-center text-3xl font-bold text-white">
                        {user.firstName?.[0]}{user.lastName?.[0] || ''}
                    </div>
                    <div className="flex-1">
                        <h1 className="text-2xl font-bold">{fullName}</h1>
                        <div className="flex flex-wrap gap-4 mt-2 text-sm text-gray-600">
                            <span className="flex items-center gap-1 text-yellow-600 font-semibold">
                                <Star size={16} fill="currentColor" /> {user.rating || '0.0'}
                            </span>
                            <span className="text-purple-600 font-semibold">{user.skillCoins || 0} SC</span>
                            {user.city && <span className="flex items-center gap-1"><MapPin size={14} /> {user.city}</span>}
                        </div>
                        {user.bio && <p className="mt-4 text-gray-600">{user.bio}</p>}
                        {(user.skillsCanOffer?.length > 0 || user.skillsNeeded?.length > 0) && (
                            <div className="mt-4 grid sm:grid-cols-2 gap-4 text-sm">
                                {user.skillsCanOffer?.length > 0 && (
                                    <div>
                                        <p className="font-medium text-gray-700 mb-1">Может предложить</p>
                                        <div className="flex flex-wrap gap-1">
                                            {user.skillsCanOffer.map(s => (
                                                <span key={s} className="bg-emerald-50 text-emerald-700 px-2 py-0.5 rounded">{s}</span>
                                            ))}
                                        </div>
                                    </div>
                                )}
                                {user.skillsNeeded?.length > 0 && (
                                    <div>
                                        <p className="font-medium text-gray-700 mb-1">Ищет</p>
                                        <div className="flex flex-wrap gap-1">
                                            {user.skillsNeeded.map(s => (
                                                <span key={s} className="bg-orange-50 text-orange-700 px-2 py-0.5 rounded">{s}</span>
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                    {!isOwnProfile && (
                        <button onClick={() => setChatOpen(true)}
                                className="flex items-center gap-2 bg-purple-600 text-white px-5 py-2.5 rounded-xl font-medium">
                            <MessageCircle size={18} /> Написать
                        </button>
                    )}
                </div>
            </div>

            <div className="bg-white rounded-2xl border p-6">
                <h2 className="text-lg font-semibold mb-4">Отзывы</h2>
                <ReviewList
                    userId={user.id}
                    currentUserId={currentUser.id}
                    userName={fullName}
                />
            </div>

            <ChatModal
                isOpen={chatOpen}
                onClose={() => setChatOpen(false)}
                recipientId={user.id}
                recipientName={fullName}
                currentUserId={currentUser.id}
            />
        </div>
    );
}
