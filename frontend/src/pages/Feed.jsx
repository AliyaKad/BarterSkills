import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Search, Star, Plus, Handshake } from 'lucide-react';
import { fetchOffers, fetchRequests } from '../api/marketplace';
import { CATEGORIES, CATEGORY_LABELS } from '../constants';

export default function Feed({ user }) {
    const navigate = useNavigate();
    const [searchTerm, setSearchTerm] = useState('');
    const [typeFilter, setTypeFilter] = useState('all');
    const [categoryFilter, setCategoryFilter] = useState('');
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const load = async () => {
            setLoading(true);
            try {
                const filters = { q: searchTerm || undefined, category: categoryFilter || undefined };
                const [offersRes, requestsRes] = await Promise.all([
                    typeFilter !== 'need' ? fetchOffers(filters) : Promise.resolve({ data: [] }),
                    typeFilter !== 'offer' ? fetchRequests(filters) : Promise.resolve({ data: [] }),
                ]);
                const merged = [...offersRes.data, ...requestsRes.data]
                    .sort((a, b) => (b.id || 0) - (a.id || 0));
                setItems(merged);
            } catch {
                setItems([]);
            } finally {
                setLoading(false);
            }
        };
        const t = setTimeout(load, 300);
        return () => clearTimeout(t);
    }, [searchTerm, typeFilter, categoryFilter]);

    const goToProfile = (authorId, e) => {
        e.stopPropagation();
        navigate(`/user/${authorId}`);
    };

    const openItem = (item) => {
        navigate(item.kind === 'offer' ? `/offers/${item.id}` : `/requests/${item.id}`);
    };

    return (
        <div className="space-y-8">
            <div className="bg-gradient-to-r from-purple-600 to-indigo-600 rounded-2xl p-6 md:p-8 text-white shadow-lg relative overflow-hidden">
                <div className="relative z-10">
                    <h1 className="text-2xl md:text-3xl font-bold mb-2">Привет, {user.firstName}!</h1>
                    <p className="text-purple-100 mb-6 max-w-xl">Находи услуги, предлагай навыки, зарабатывай SkillCoin.</p>
                    <div className="flex flex-wrap gap-3">
                        <div className="bg-white/10 backdrop-blur-sm rounded-xl px-4 py-3 border border-white/20">
                            <p className="text-xs text-purple-200">Баланс</p>
                            <p className="text-xl font-bold">{user.skillCoins || 0} SC</p>
                        </div>
                        <div className="bg-white/10 backdrop-blur-sm rounded-xl px-4 py-3 border border-white/20">
                            <p className="text-xs text-purple-200">Рейтинг</p>
                            <p className="text-xl font-bold">{user.rating?.toFixed?.(1) || user.rating || '0.0'}</p>
                        </div>
                        <Link to="/requests/new" className="bg-white text-purple-600 px-5 py-2.5 rounded-xl font-semibold hover:bg-purple-50 flex items-center gap-2">
                            <Plus size={18} /> Заявка
                        </Link>
                        <Link to="/offers/new" className="bg-white/20 text-white px-5 py-2.5 rounded-xl font-semibold hover:bg-white/30 flex items-center gap-2 border border-white/30">
                            <Plus size={18} /> Предложение
                        </Link>
                        <Link to="/deals" className="bg-white/20 text-white px-5 py-2.5 rounded-xl font-semibold hover:bg-white/30 flex items-center gap-2 border border-white/30">
                            <Handshake size={18} /> Сделки
                        </Link>
                    </div>
                </div>
            </div>

            <div className="bg-white rounded-xl shadow-sm border p-4 flex flex-col md:flex-row gap-4">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                    <input type="text" placeholder="Поиск..." className="w-full pl-10 pr-4 py-2.5 bg-gray-50 border rounded-lg outline-none focus:ring-2 focus:ring-purple-500"
                           value={searchTerm} onChange={e => setSearchTerm(e.target.value)} />
                </div>
                <div className="flex gap-2 flex-wrap">
                    {[
                        { id: 'all', label: 'Все' },
                        { id: 'need', label: 'Заявки' },
                        { id: 'offer', label: 'Предложения' },
                    ].map(f => (
                        <button key={f.id} onClick={() => setTypeFilter(f.id)}
                                className={`px-4 py-2 rounded-lg text-sm font-medium ${typeFilter === f.id ? 'bg-purple-600 text-white' : 'bg-gray-100 text-gray-600'}`}>
                            {f.label}
                        </button>
                    ))}
                </div>
                <select className="px-3 py-2 border rounded-lg text-sm" value={categoryFilter}
                        onChange={e => setCategoryFilter(e.target.value)}>
                    {CATEGORIES.map(c => (
                        <option key={c.value} value={c.value}>{c.label}</option>
                    ))}
                </select>
            </div>

            {loading ? (
                <div className="flex justify-center py-16"><div className="animate-spin h-10 w-10 border-b-2 border-purple-600 rounded-full" /></div>
            ) : items.length === 0 ? (
                <p className="text-center text-gray-500 py-16">Ничего не найдено</p>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {items.map(item => (
                        <div key={`${item.kind}-${item.id}`} onClick={() => openItem(item)}
                             className="bg-white rounded-2xl border hover:border-purple-200 hover:shadow-md cursor-pointer overflow-hidden">
                            <div className="p-5">
                                <div className="flex justify-between mb-3">
                                    <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${item.kind === 'need' ? 'bg-orange-100 text-orange-700' : 'bg-emerald-100 text-emerald-700'}`}>
                                        {item.kind === 'need' ? 'Ищу' : 'Предлагаю'}
                                    </span>
                                    <span className="flex items-center gap-1 text-yellow-500 text-sm font-bold">
                                        <Star size={14} fill="currentColor" /> {item.authorRating || '0.0'}
                                    </span>
                                </div>
                                <h3 className="font-bold text-lg line-clamp-2">{item.title}</h3>
                                <p className="text-gray-600 text-sm mt-2 line-clamp-2">{item.description}</p>
                                <p className="text-xs text-gray-400 mt-2">{CATEGORY_LABELS[item.category] || item.category}</p>
                            </div>
                            <div className="bg-gray-50 px-5 py-4 border-t flex justify-between items-center">
                                <button onClick={e => goToProfile(item.authorId, e)}
                                        className="flex items-center gap-2 text-sm font-medium">
                                    <span className="w-8 h-8 rounded-full bg-purple-100 text-purple-600 flex items-center justify-center text-xs font-bold">
                                        {item.authorName?.charAt(0)}
                                    </span>
                                    {item.authorName}
                                </button>
                                <span className="bg-purple-100 text-purple-700 px-3 py-1 rounded-lg font-bold text-sm">
                                    {item.price != null ? `${item.kind === 'need' ? 'до ' : ''}${item.price} SC` : '—'}
                                </span>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
