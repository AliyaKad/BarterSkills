import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Star, Coins } from 'lucide-react';
import { fetchRequest, createDealFromRequest } from '../api/marketplace';
import { CATEGORY_LABELS } from '../constants';

export default function RequestDetail({ user }) {
    const { id } = useParams();
    const navigate = useNavigate();
    const [request, setRequest] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [actionLoading, setActionLoading] = useState(false);

    useEffect(() => {
        fetchRequest(id)
            .then(res => setRequest(res.data))
            .catch(() => setError('Заявка не найдена'))
            .finally(() => setLoading(false));
    }, [id]);

    const handleRespond = async () => {
        setActionLoading(true);
        try {
            const res = await createDealFromRequest(request.id);
            navigate(`/deals/${res.data.id}`);
        } catch (err) {
            alert(err.response?.data?.error || 'Ошибка отклика');
        } finally {
            setActionLoading(false);
        }
    };

    if (loading) return <div className="flex justify-center py-20"><div className="animate-spin h-10 w-10 border-b-2 border-purple-600 rounded-full" /></div>;
    if (error) return <p className="text-red-500">{error}</p>;

    const isOwn = request.authorId === user.id;

    return (
        <div className="max-w-2xl mx-auto space-y-6">
            <Link to="/" className="flex items-center gap-2 text-gray-600 hover:text-purple-600">
                <ArrowLeft size={20} /> Назад
            </Link>
            <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-sm">
                <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-orange-100 text-orange-700">Заявка</span>
                <h1 className="text-2xl font-bold mt-3">{request.title}</h1>
                <p className="text-gray-600 mt-3">{request.description}</p>
                <div className="flex flex-wrap gap-4 mt-4 text-sm text-gray-500">
                    <span>{CATEGORY_LABELS[request.category] || request.category}</span>
                    {request.price != null && (
                        <span className="flex items-center gap-1 text-purple-700 font-bold">
                            <Coins size={16} /> до {request.price} SC
                        </span>
                    )}
                </div>
                <div className="mt-6 pt-6 border-t flex items-center justify-between">
                    <button onClick={() => navigate(`/user/${request.authorId}`)}
                            className="flex items-center gap-2 hover:text-purple-600">
                        <div className="w-10 h-10 rounded-full bg-orange-100 text-orange-600 flex items-center justify-center font-bold">
                            {request.authorName?.charAt(0)}
                        </div>
                        <div className="text-left">
                            <p className="font-medium">{request.authorName}</p>
                            <p className="text-xs text-yellow-600 flex items-center gap-1">
                                <Star size={12} fill="currentColor" /> {request.authorRating || '0.0'}
                            </p>
                        </div>
                    </button>
                    {!isOwn && request.status === 'OPEN' && (
                        <button onClick={handleRespond} disabled={actionLoading}
                                className="bg-purple-600 hover:bg-purple-700 text-white px-6 py-2.5 rounded-xl font-medium">
                            {actionLoading ? '...' : 'Откликнуться'}
                        </button>
                    )}
                    {isOwn && <span className="text-sm text-gray-500">Это ваша заявка</span>}
                </div>
            </div>
        </div>
    );
}
