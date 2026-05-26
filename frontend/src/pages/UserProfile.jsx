import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, MapPin, Star, Calendar, MessageCircle, Shield } from 'lucide-react';
import ReviewList from '../components/ReviewList';
import api from '../api/axios';

export default function UserProfile({ currentUserId }) {
    const { userId } = useParams();
    const navigate = useNavigate();
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {

        const fetchUser = async () => {
            try {
                setLoading(true);
                const res = await api.get(`/api/user/${userId}`); // Запрос по userId из URL
                setUser(res.data);
                setError('');
            } catch (err) {
                console.error('Ошибка загрузки профиля:', err);
                setError('Пользователь не найден или ошибка загрузки');
            } finally {
                setLoading(false);
            }
        };
        fetchUser();
    }, [userId]);

    if (loading) return <div className="flex h-screen items-center justify-center"><div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600"></div></div>;
    if (error) return <div className="text-center text-red-500 mt-10">{error} <button onClick={() => navigate(-1)} className="block mt-2 text-blue-500 underline">Назад</button></div>;
    if (!user) return null;

    const isOwnProfile = user.id === currentUserId;
    const dateStr = user.createdAt ? new Date(user.createdAt).toLocaleDateString('ru-RU') : 'Недавно';

    return (
        <div className="max-w-4xl mx-auto p-4 sm:p-6 space-y-6">
            <button onClick={() => navigate(-1)} className="flex items-center gap-2 text-gray-600 hover:text-purple-600 transition mb-4">
                <ArrowLeft size={20} /><span className="font-medium">Назад к ленте</span>
            </button>

            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 sm:p-8">
                <div className="flex flex-col sm:flex-row items-start sm:items-center gap-6">
                    <div className="w-24 h-24 bg-gradient-to-br from-purple-500 to-indigo-600 rounded-full flex items-center justify-center text-4xl font-bold text-white shadow-lg">
                        {user.firstName?.charAt(0) || ''}{user.lastName?.charAt(0) || ''}
                    </div>
                    <div className="flex-1">
                        <h1 className="text-2xl sm:text-3xl font-bold text-gray-900 mb-2">{user.firstName} {user.lastName}</h1>
                        <div className="flex flex-wrap items-center gap-4 text-sm text-gray-600">
                            <div className="flex items-center gap-1 text-yellow-600 font-semibold"><Star size={16} fill="currentColor" /> {user.rating || '0.0'}</div>
                            <div className="flex items-center gap-1 text-purple-600 font-semibold">{user.skillCoins || 0} SC</div>
                            {user.city && <div className="flex items-center gap-1"><MapPin size={14} /> {user.city}</div>}
                        </div>
                        <div className="flex items-center gap-2 text-xs text-gray-400 mt-2">
                            <Calendar size={14} /> На платформе с {dateStr}
                        </div>
                    </div>
                    {!isOwnProfile && (
                        <div className="flex gap-3 w-full sm:w-auto">
                            <button onClick={() => navigate(`/chat/${user.id}`)} className="flex-1 sm:flex-none flex items-center justify-center gap-2 bg-purple-600 hover:bg-purple-700 text-white px-5 py-2.5 rounded-xl font-medium transition shadow-md">
                                <MessageCircle size={18} /> Написать
                            </button>
                        </div>
                    )}
                </div>

                {user.bio && (
                    <div className="mt-6 pt-6 border-t border-gray-100">
                        <h2 className="text-lg font-semibold text-gray-800 mb-3">О себе</h2>
                        <p className="text-gray-600 leading-relaxed">{user.bio}</p>
                    </div>
                )}
            </div>

            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
                <h2 className="text-lg font-semibold text-gray-800 mb-4">Отзывы о пользователе</h2>
                <ReviewList
                    userId={user.id}
                    currentUserId={currentUserId}
                    onUpdate={() => {}}
                />
            </div>
        </div>
    );
}