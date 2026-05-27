import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Star, Coins } from 'lucide-react';
import { fetchOffer, createDealFromOffer } from '../api/marketplace';
import { CATEGORY_LABELS } from '../constants';

export default function OfferDetail({ user }) {
    const { id } = useParams();
    const navigate = useNavigate();
    const [offer, setOffer] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [actionLoading, setActionLoading] = useState(false);

    useEffect(() => {
        fetchOffer(id)
            .then(res => setOffer(res.data))
            .catch(() => setError('Предложение не найдено'))
            .finally(() => setLoading(false));
    }, [id]);

    const handleOrder = async () => {
        if (!confirm(`Заказать услугу за ${offer.price} SC?`)) return;
        setActionLoading(true);
        try {
            const res = await createDealFromOffer(offer.id);
            navigate(`/deals/${res.data.id}`);
        } catch (err) {
            alert(err.response?.data?.error || 'Ошибка создания сделки');
        } finally {
            setActionLoading(false);
        }
    };

    if (loading) return <div className="flex justify-center py-20"><div className="animate-spin h-10 w-10 border-b-2 border-purple-600 rounded-full" /></div>;
    if (error) return <p className="text-red-500">{error}</p>;

    const isOwn = offer.authorId === user.id;

    return (
        <div className="max-w-2xl mx-auto space-y-6">
            <Link to="/" className="flex items-center gap-2 text-gray-600 hover:text-purple-600">
                <ArrowLeft size={20} /> Назад
            </Link>
            <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-sm">
                <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-700">Предложение</span>
                <h1 className="text-2xl font-bold mt-3">{offer.title}</h1>
                <p className="text-gray-600 mt-3">{offer.description}</p>
                <div className="flex flex-wrap gap-4 mt-4 text-sm text-gray-500">
                    <span>{CATEGORY_LABELS[offer.category] || offer.category}</span>
                    <span className="flex items-center gap-1 text-purple-700 font-bold">
                        <Coins size={16} /> {offer.price} SC
                    </span>
                </div>
                <div className="mt-6 pt-6 border-t flex items-center justify-between">
                    <button onClick={() => navigate(`/user/${offer.authorId}`)}
                            className="flex items-center gap-2 hover:text-purple-600">
                        <div className="w-10 h-10 rounded-full bg-purple-100 text-purple-600 flex items-center justify-center font-bold">
                            {offer.authorName?.charAt(0)}
                        </div>
                        <div className="text-left">
                            <p className="font-medium">{offer.authorName}</p>
                            <p className="text-xs text-yellow-600 flex items-center gap-1">
                                <Star size={12} fill="currentColor" /> {offer.authorRating || '0.0'}
                            </p>
                        </div>
                    </button>
                    {!isOwn && offer.status === 'ACTIVE' && (
                        <button onClick={handleOrder} disabled={actionLoading}
                                className="bg-purple-600 hover:bg-purple-700 text-white px-6 py-2.5 rounded-xl font-medium">
                            {actionLoading ? '...' : 'Заказать услугу'}
                        </button>
                    )}
                    {isOwn && <span className="text-sm text-gray-500">Это ваше предложение</span>}
                </div>
            </div>
        </div>
    );
}
