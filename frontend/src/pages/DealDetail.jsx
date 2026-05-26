import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import {
    fetchDeal, acceptDeal, cancelDeal, confirmDeal, disputeDeal,
} from '../api/marketplace';
import { DEAL_STATUS_LABELS } from '../constants';

export default function DealDetail({ user }) {
    const { id } = useParams();
    const [deal, setDeal] = useState(null);
    const [loading, setLoading] = useState(true);
    const [actionLoading, setActionLoading] = useState(false);
    const [message, setMessage] = useState('');

    const load = () => {
        fetchDeal(id)
            .then(res => setDeal(res.data))
            .catch(() => setDeal(null))
            .finally(() => setLoading(false));
    };

    useEffect(() => { load(); }, [id]);

    const runAction = async (fn, successMsg) => {
        setActionLoading(true);
        setMessage('');
        try {
            const res = await fn(id);
            setDeal(res.data);
            setMessage(successMsg);
        } catch (err) {
            setMessage(err.response?.data?.error || 'Ошибка');
        } finally {
            setActionLoading(false);
        }
    };

    if (loading) return <div className="flex justify-center py-20"><div className="animate-spin h-10 w-10 border-b-2 border-purple-600 rounded-full" /></div>;
    if (!deal) return <p className="text-red-500">Сделка не найдена</p>;

    const otherId = deal.role === 'customer' ? deal.executorId : deal.customerId;
    const otherName = deal.role === 'customer' ? deal.executorName : deal.customerName;

    return (
        <div className="max-w-2xl mx-auto space-y-6">
            <Link to="/deals" className="flex items-center gap-2 text-gray-600 hover:text-purple-600">
                <ArrowLeft size={20} /> Все сделки
            </Link>
            <div className="bg-white rounded-2xl border p-6 shadow-sm space-y-4">
                <div className="flex justify-between items-start">
                    <h1 className="text-xl font-bold">Сделка #{deal.id}</h1>
                    <span className="text-sm px-2 py-1 rounded-full bg-purple-50 text-purple-700 font-medium">
                        {DEAL_STATUS_LABELS[deal.status] || deal.status}
                    </span>
                </div>
                <p className="text-2xl font-bold text-purple-700">{deal.amount} SC</p>
                {deal.offerTitle && <p><span className="text-gray-500">Предложение:</span> {deal.offerTitle}</p>}
                {deal.requestTitle && <p><span className="text-gray-500">Заявка:</span> {deal.requestTitle}</p>}
                <p><span className="text-gray-500">Заказчик:</span> {deal.customerName}</p>
                <p><span className="text-gray-500">Исполнитель:</span> {deal.executorName}</p>
                {deal.coinsHeld && <p className="text-sm text-amber-700 bg-amber-50 p-2 rounded-lg">Монеты заморожены у заказчика</p>}
                {(deal.status === 'IN_PROGRESS' || deal.status === 'ACCEPTED') && (
                    <p className="text-sm text-gray-600">
                        Подтверждение: заказчик {deal.customerConfirmed ? '✅' : '⏳'},
                        исполнитель {deal.executorConfirmed ? '✅' : '⏳'}
                    </p>
                )}
                {message && <p className="text-sm text-green-700 bg-green-50 p-2 rounded-lg">{message}</p>}
                <div className="flex flex-wrap gap-2 pt-2">
                    {deal.canAccept && (
                        <button disabled={actionLoading} onClick={() => runAction(acceptDeal, 'Сделка принята!')}
                                className="bg-green-600 text-white px-4 py-2 rounded-xl text-sm font-medium">
                            Принять сделку
                        </button>
                    )}
                    {deal.canConfirmCompletion && (
                        <button disabled={actionLoading} onClick={() => runAction(confirmDeal, deal.status === 'COMPLETED' ? 'Завершено!' : 'Подтверждение отправлено')}
                                className="bg-purple-600 text-white px-4 py-2 rounded-xl text-sm font-medium">
                            Подтвердить завершение
                        </button>
                    )}
                    {deal.canDispute && (
                        <button disabled={actionLoading}
                                onClick={() => { if (confirm('Открыть спор?')) runAction(disputeDeal, 'Спор открыт'); }}
                                className="bg-orange-500 text-white px-4 py-2 rounded-xl text-sm font-medium">
                            Открыть спор
                        </button>
                    )}
                    {deal.canCancel && (
                        <button disabled={actionLoading}
                                onClick={() => { if (confirm('Отменить сделку?')) runAction(cancelDeal, 'Сделка отменена'); }}
                                className="bg-red-100 text-red-700 px-4 py-2 rounded-xl text-sm font-medium">
                            Отменить
                        </button>
                    )}
                </div>
                <Link to={`/user/${otherId}`} className="inline-block text-purple-600 text-sm hover:underline">
                    Профиль: {otherName}
                </Link>
            </div>
        </div>
    );
}
