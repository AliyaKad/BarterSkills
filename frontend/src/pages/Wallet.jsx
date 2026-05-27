import { useState, useEffect } from 'react';
import { ArrowUpRight, ArrowDownLeft, History, Wallet as WalletIcon } from 'lucide-react';
import { fetchTransactions } from '../api/marketplace';
import { TX_TYPE_LABELS } from '../constants';

export default function Wallet({ user, onUserUpdate }) {
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchTransactions()
            .then(res => setTransactions(res.data))
            .catch(() => setTransactions([]))
            .finally(() => setLoading(false));
    }, []);

    if (!user) return null;

    return (
        <div className="max-w-4xl mx-auto space-y-8">
            <div className="flex items-center gap-3">
                <div className="bg-blue-100 p-2 rounded-lg text-blue-600"><WalletIcon size={24} /></div>
                <h1 className="text-2xl font-bold text-gray-800">Мой кошелёк</h1>
            </div>

            <div className="bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl p-8 text-white shadow-xl">
                <p className="text-blue-100 text-sm">Доступно</p>
                <h2 className="text-4xl font-bold">{user.skillCoins || 0} <span className="text-2xl opacity-80">SC</span></h2>
                {(user.skillCoinHeld > 0) && (
                    <p className="text-blue-200 text-sm mt-2">Заморожено: {user.skillCoinHeld} SC</p>
                )}
            </div>

            <div className="bg-white rounded-2xl border overflow-hidden">
                <div className="p-4 border-b bg-gray-50 flex items-center gap-2 font-bold text-gray-800">
                    <History size={20} className="text-blue-600" /> История операций
                </div>
                {loading ? (
                    <div className="p-12 flex justify-center"><div className="animate-spin h-8 w-8 border-b-2 border-blue-600 rounded-full" /></div>
                ) : transactions.length === 0 ? (
                    <p className="p-12 text-center text-gray-500">Пока нет операций</p>
                ) : (
                    <div className="divide-y">
                        {transactions.map(tx => (
                            <div key={tx.id} className="p-4 flex justify-between items-center hover:bg-gray-50">
                                <div className="flex items-center gap-4">
                                    <div className={`w-10 h-10 rounded-full flex items-center justify-center ${
                                        tx.direction === 'income' ? 'bg-emerald-100 text-emerald-600' : 'bg-rose-100 text-rose-600'
                                    }`}>
                                        {tx.direction === 'income' ? <ArrowDownLeft size={20} /> : <ArrowUpRight size={20} />}
                                    </div>
                                    <div>
                                        <p className="font-medium text-gray-800">{tx.description}</p>
                                        <p className="text-xs text-gray-500">
                                            {TX_TYPE_LABELS[tx.type] || tx.type} · {tx.timestamp}
                                            {tx.counterpartyName && tx.type !== 'BONUS' && ` · ${tx.counterpartyName}`}
                                        </p>
                                    </div>
                                </div>
                                <p className={`font-bold ${tx.direction === 'income' ? 'text-emerald-600' : 'text-rose-600'}`}>
                                    {tx.direction === 'income' ? '+' : '-'}{tx.amount} SC
                                </p>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}
