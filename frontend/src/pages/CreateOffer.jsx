import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { createOffer } from '../api/marketplace';
import { CATEGORIES } from '../constants';

export default function CreateOffer() {
    const navigate = useNavigate();
    const [form, setForm] = useState({
        title: '', description: '', category: 'OTHER', priceInSkillCoins: '',
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            const res = await createOffer({
                title: form.title,
                description: form.description,
                category: form.category,
                priceInSkillCoins: form.priceInSkillCoins || 0,
            });
            navigate(`/offers/${res.data.id}`);
        } catch (err) {
            setError(err.response?.data?.error || 'Не удалось создать предложение');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-xl mx-auto">
            <Link to="/" className="flex items-center gap-2 text-gray-600 hover:text-purple-600 mb-6">
                <ArrowLeft size={20} /> Назад к ленте
            </Link>
            <h1 className="text-2xl font-bold text-gray-900 mb-6">Создать предложение</h1>
            <form onSubmit={handleSubmit} className="bg-white rounded-2xl border border-gray-100 p-6 space-y-4 shadow-sm">
                {error && <div className="bg-red-50 text-red-700 p-3 rounded-lg text-sm">{error}</div>}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Название *</label>
                    <input className="input w-full" required value={form.title}
                           onChange={e => setForm({ ...form, title: e.target.value })} />
                </div>
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Описание</label>
                    <textarea className="input w-full" rows={4} value={form.description}
                              onChange={e => setForm({ ...form, description: e.target.value })} />
                </div>
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Категория</label>
                    <select className="input w-full" value={form.category}
                            onChange={e => setForm({ ...form, category: e.target.value })}>
                        {CATEGORIES.filter(c => c.value).map(c => (
                            <option key={c.value} value={c.value}>{c.label}</option>
                        ))}
                    </select>
                </div>
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Цена (SC) *</label>
                    <input type="number" min="0" required className="input w-full" value={form.priceInSkillCoins}
                           onChange={e => setForm({ ...form, priceInSkillCoins: e.target.value })} />
                </div>
                <button type="submit" disabled={loading} className="btn-primary w-full py-3">
                    {loading ? 'Создание...' : 'Опубликовать предложение'}
                </button>
            </form>
        </div>
    );
}
