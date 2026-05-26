import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, MapPin, Clock, Star, ArrowRightLeft, Plus } from 'lucide-react';

export default function Feed({ user }) {
    const navigate = useNavigate();
    const [searchTerm, setSearchTerm] = useState('');

    //  MOCK ДАННЫЕ: authorId должен отличаться от твоего ID (например, если ты ID 1, то здесь 2, 3, 4)
    const mockRequests = [
        { id: 1, type: 'need', title: 'Настройка компьютера и установка ПО', desc: 'Нужна помощь с установкой Windows, драйверов и базового софта. Готов отдать SkillCoin.', price: 30, location: 'Москва', deadline: '3 дня', author: 'Алексей К.', authorId: 78, rating: 4.8 },
        { id: 2, type: 'can', title: 'Уроки гитары для начинающих', desc: 'Научу играть любимые песни, аккорды и бой. Опыт 5 лет. За SkillCoin.', price: 20, location: 'Онлайн', deadline: 'Гибко', author: 'Мария С.', authorId: 3, rating: 5.0 },
        { id: 3, type: 'need', title: 'Перевод технической документации (EN-RU)', desc: 'Нужно перевести ~15 страниц инструкции. Срочно.', price: 45, location: 'Удалённо', deadline: 'Сегодня', author: 'Дмитрий В.', authorId: 4, rating: 4.5 },
    ];

    // Функция перехода в профиль ДРУГОГО пользователя
    const goToProfile = (authorId, e) => {
        e.stopPropagation();
        if (!authorId) return;
        navigate(`/user/${authorId}`);
    };

    return (
        <div className="space-y-8">
            {/* Welcome & Stats */}
            <div className="bg-gradient-to-r from-purple-600 to-indigo-600 rounded-2xl p-6 md:p-8 text-white shadow-lg relative overflow-hidden">
                <div className="relative z-10">
                    <h1 className="text-2xl md:text-3xl font-bold mb-2">Привет, {user.firstName}!</h1>
                    <p className="text-purple-100 mb-6 max-w-xl">Находи нужные услуги или предлагай свои навыки. Зарабатывай SkillCoin и трать их без ограничений.</p>
                    <div className="flex flex-wrap gap-4">
                        <div className="bg-white/10 backdrop-blur-sm rounded-xl p-4 flex items-center gap-3 border border-white/20">
                            <div className="bg-white/20 p-2 rounded-lg"><ArrowRightLeft size={20} /></div>
                            <div>
                                <p className="text-xs text-purple-200">Баланс</p>
                                <p className="text-xl font-bold">{user.skillCoins || 0} SC</p>
                            </div>
                        </div>
                        <div className="bg-white/10 backdrop-blur-sm rounded-xl p-4 flex items-center gap-3 border border-white/20">
                            <div className="bg-white/20 p-2 rounded-lg"><Star size={20} /></div>
                            <div>
                                <p className="text-xs text-purple-200">Рейтинг</p>
                                <p className="text-xl font-bold">{user.rating || '0.0'} / 5.0</p>
                            </div>
                        </div>
                        <button className="ml-auto bg-white text-purple-600 px-5 py-2.5 rounded-xl font-semibold hover:bg-purple-50 transition flex items-center gap-2 shadow-md">
                            <Plus size={18} /> Создать заявку
                        </button>
                    </div>
                </div>
                <div className="absolute -top-10 -right-10 w-64 h-64 bg-white/10 rounded-full blur-3xl pointer-events-none"></div>
            </div>

            {/* Search & Filters */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-4 flex flex-col md:flex-row gap-4 items-center">
                <div className="relative flex-1 w-full">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                    <input type="text" placeholder="Поиск услуг или навыков..." className="w-full pl-10 pr-4 py-2.5 bg-gray-50 border border-gray-200 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 outline-none transition" value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />
                </div>
                <div className="flex gap-2 w-full md:w-auto overflow-x-auto pb-1">
                    {['Все', 'Нужно', 'Могу', 'IT', 'Учёба', 'Ремонт'].map((filter, i) => (
                        <button key={i} className={`px-4 py-2 rounded-lg text-sm font-medium whitespace-nowrap transition ${i === 0 ? 'bg-purple-600 text-white shadow-md' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>
                            {filter}
                        </button>
                    ))}
                </div>
            </div>

            {/* Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {mockRequests.map((req) => (
                    <div key={req.id} className="bg-white rounded-2xl shadow-sm border border-gray-100 hover:shadow-md hover:border-purple-200 transition-all duration-200 overflow-hidden group">
                        <div className="p-5">
                            <div className="flex justify-between items-start mb-3">
                <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${req.type === 'need' ? 'bg-orange-100 text-orange-700' : 'bg-emerald-100 text-emerald-700'}`}>
                  {req.type === 'need' ? ' Ищу' : '✅ Предлагаю'}
                </span>
                                <div className="flex items-center gap-1 text-yellow-500 font-bold text-sm">
                                    <Star size={14} fill="currentColor" /> {req.rating}
                                </div>
                            </div>

                            <h3 className="font-bold text-lg text-gray-800 mb-2 line-clamp-2 group-hover:text-purple-600 transition">{req.title}</h3>
                            <p className="text-gray-600 text-sm mb-4 line-clamp-2">{req.desc}</p>

                            <div className="space-y-2 mb-4 text-sm text-gray-500">
                                <div className="flex items-center gap-2"><MapPin size={14} /> {req.location}</div>
                                <div className="flex items-center gap-2"><Clock size={14} /> {req.deadline}</div>
                            </div>
                        </div>

                        <div className="bg-gray-50 px-5 py-4 border-t border-gray-100 flex justify-between items-center">
                            <div className="flex items-center gap-2">
                                {/* 🔥 АВАТАРКА КЛИКАБЕЛЬНАЯ */}
                                <button
                                    onClick={(e) => goToProfile(req.authorId, e)}
                                    className="w-8 h-8 rounded-full bg-gradient-to-br from-purple-400 to-indigo-500 flex items-center justify-center text-white text-xs font-bold hover:scale-110 transition cursor-pointer shadow-sm"
                                    title={`Профиль ${req.author}`}
                                >
                                    {req.author.charAt(0)}
                                </button>
                                <span className="text-sm font-medium text-gray-700">{req.author}</span>
                            </div>
                            <div className="bg-purple-100 text-purple-700 px-3 py-1.5 rounded-lg font-bold text-sm flex items-center gap-1">
                                {req.price} SC
                            </div>
                        </div>
                        <div className="px-5 pb-5 pt-2">
                            <button className="w-full bg-purple-600 hover:bg-purple-700 text-white font-medium py-2.5 rounded-xl transition shadow-sm hover:shadow-md">
                                Откликнуться
                            </button>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}