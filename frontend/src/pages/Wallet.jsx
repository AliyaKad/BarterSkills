import { useState, useEffect } from 'react';
import { ArrowUpRight, ArrowDownLeft, History, Wallet  as WalletIcon, Info, Shield, Star, Handshake } from 'lucide-react';

export default function Wallet({ user }) {
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const mockTransactions = [
            { id: 1, type: 'income', amount: 150, desc: 'Выполнение заявки "Дизайн логотипа"', date: 'Сегодня, 14:30' },
            { id: 2, type: 'expense', amount: 50, desc: 'Оплата за консультацию по Java', date: 'Вчера, 09:15' },
            { id: 3, type: 'income', amount: 500, desc: 'Бонус за верификацию профиля', date: '20.05.2026' },
            { id: 4, type: 'expense', amount: 20, desc: 'Поднятие заявки в топ ленты', date: '18.05.2026' },
            { id: 5, type: 'income', amount: 1000, desc: 'Начальный капитал при регистрации', date: '01.05.2026' },
        ];

        setTimeout(() => {
            setTransactions(mockTransactions);
            setLoading(false);
        }, 600);
    }, []);

    if (!user) return null;

    return (
        <div className="max-w-4xl mx-auto p-4 sm:p-6 space-y-8">
            <div className="flex items-center gap-3 mb-2">
                <div className="bg-blue-100 p-2 rounded-lg text-blue-600">
                    <WalletIcon size={24} />
                </div>
                <h1 className="text-2xl font-bold text-gray-800">Мой Кошелёк</h1>
            </div>

            <div className="bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl p-6 sm:p-8 text-white shadow-xl relative overflow-hidden">
                <div className="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full blur-3xl -mr-16 -mt-16 pointer-events-none"></div>
                <div className="absolute bottom-0 left-0 w-40 h-40 bg-black/10 rounded-full blur-2xl -ml-10 -mb-10 pointer-events-none"></div>

                <div className="relative z-10 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-6">
                    <div>
                        <p className="text-blue-100 text-sm font-medium mb-1 uppercase tracking-wider">Доступно</p>
                        <h2 className="text-4xl sm:text-5xl font-bold flex items-baseline gap-2">
                            {user.skillCoins || 0}
                            <span className="text-2xl sm:text-3xl opacity-80 font-normal">SC</span>
                        </h2>
                        <p className="text-blue-200 text-xs mt-2 flex items-center gap-1">
                            <Shield size={12} /> Внутренняя валюта платформы BarterSkills
                        </p>
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex flex-col items-center text-center gap-2">
                    <div className="bg-green-100 text-green-600 p-2 rounded-full"><Handshake size={20} /></div>
                    <h3 className="font-bold text-gray-800 text-sm">Выполняй заявки</h3>
                    <p className="text-xs text-gray-500">Помогай другим пользователям и получай SC за услуги</p>
                </div>
                <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex flex-col items-center text-center gap-2">
                    <div className="bg-purple-100 text-purple-600 p-2 rounded-full"><Star size={20} /></div>
                    <h3 className="font-bold text-gray-800 text-sm">Оставляй отзывы</h3>
                    <p className="text-xs text-gray-500">Получай бонусы за активное участие в жизни сообщества</p>
                </div>
                <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex flex-col items-center text-center gap-2">
                    <div className="bg-blue-100 text-blue-600 p-2 rounded-full"><Info size={20} /></div>
                    <h3 className="font-bold text-gray-800 text-sm">Будущие функции</h3>
                    <p className="text-xs text-gray-500">Скоро SC можно будет тратить на обучение и курсы</p>
                </div>
            </div>

            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
                <div className="p-4 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
                    <h2 className="font-bold text-gray-800 flex items-center gap-2">
                        <History size={20} className="text-blue-600" /> История операций
                    </h2>
                    <span className="text-xs text-gray-500 bg-gray-200 px-2 py-1 rounded-md">{transactions.length} записей</span>
                </div>

                {loading ? (
                    <div className="p-12 flex flex-col items-center justify-center text-gray-400 gap-3">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                        <p>Загрузка транзакций...</p>
                    </div>
                ) : transactions.length === 0 ? (
                    <div className="p-12 text-center text-gray-500 flex flex-col items-center gap-2">
                        <History size={32} className="opacity-20" />
                        <p>Пока нет операций</p>
                    </div>
                ) : (
                    <div className="divide-y divide-gray-50">
                        {transactions.map((tx) => (
                            <div key={tx.id} className="p-4 sm:p-5 flex items-center justify-between hover:bg-gray-50 transition group cursor-pointer">
                                <div className="flex items-center gap-4">
                                    <div className={`w-10 h-10 sm:w-12 sm:h-12 rounded-full flex items-center justify-center shrink-0 ${
                                        tx.type === 'income'
                                            ? 'bg-emerald-100 text-emerald-600 group-hover:bg-emerald-200'
                                            : 'bg-rose-100 text-rose-600 group-hover:bg-rose-200'
                                    }`}>
                                        {tx.type === 'income' ? <ArrowDownLeft size={20} /> : <ArrowUpRight size={20} />}
                                    </div>
                                    <div>
                                        <p className="font-medium text-gray-800 group-hover:text-blue-600 transition">{tx.desc}</p>
                                        <p className="text-xs text-gray-500 mt-0.5">{tx.date}</p>
                                    </div>
                                </div>

                                <div className="text-right">
                                    <p className={`font-bold text-lg ${
                                        tx.type === 'income' ? 'text-emerald-600' : 'text-rose-600'
                                    }`}>
                                        {tx.type === 'income' ? '+' : '-'}{tx.amount} SC
                                    </p>
                                    <p className="text-[10px] text-gray-400 uppercase font-medium">
                                        {tx.type === 'income' ? 'Поступление' : 'Списание'}
                                    </p>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}