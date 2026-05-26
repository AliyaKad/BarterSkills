import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Handshake } from 'lucide-react';
import { fetchDeals } from '../api/marketplace';
import { DEAL_STATUS_LABELS } from '../constants';

export default function Deals() {
    const [deals, setDeals] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchDeals()
            .then(res => setDeals(res.data))
            .catch(() => setDeals([]))
            .finally(() => setLoading(false));
    }, []);

    return (
        <div className="max-w-3xl mx-auto space-y-6">
            <h1 className="text-2xl font-bold flex items-center gap-2">
                <Handshake className="text-purple-600" /> Мои сделки
            </h1>
            {loading ? (
                <div className="flex justify-center py-12"><div className="animate-spin h-8 w-8 border-b-2 border-purple-600 rounded-full" /></div>
            ) : deals.length === 0 ? (
                <p className="text-gray-500 text-center py-12 bg-white rounded-xl border">Сделок пока нет</p>
            ) : (
                <div className="space-y-3">
                    {deals.map(deal => (
                        <Link key={deal.id} to={`/deals/${deal.id}`}
                              className="block bg-white rounded-xl border border-gray-100 p-4 hover:border-purple-200 hover:shadow-sm transition">
                            <div className="flex justify-between items-start">
                                <div>
                                    <p className="font-semibold text-gray-900">
                                        {deal.offerTitle || deal.requestTitle || `Сделка #${deal.id}`}
                                    </p>
                                    <p className="text-sm text-gray-500 mt-1">
                                        {deal.role === 'customer' ? 'Вы заказчик' : 'Вы исполнитель'} · {deal.amount} SC
                                    </p>
                                </div>
                                <span className="text-xs font-medium px-2 py-1 rounded-full bg-purple-50 text-purple-700">
                                    {DEAL_STATUS_LABELS[deal.status] || deal.status}
                                </span>
                            </div>
                        </Link>
                    ))}
                </div>
            )}
        </div>
    );
}
