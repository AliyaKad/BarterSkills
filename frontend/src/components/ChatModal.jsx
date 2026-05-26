import { useState, useEffect, useRef } from 'react';
import { X, Send, MessageCircle } from 'lucide-react';
import api from '../api/axios';

export default function ChatModal({ recipientId, recipientName, isOpen, onClose, currentUserId }) {
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState('');
    const [loading, setLoading] = useState(true);
    const messagesEndRef = useRef(null);
    const pollingRef = useRef(null);

    // Загрузка сообщений при открытии
    useEffect(() => {
        if (isOpen && recipientId) {
            loadMessages();

            // Поллинг новых сообщений каждые 3 секунды
            pollingRef.current = setInterval(loadMessages, 3000);

            return () => {
                if (pollingRef.current) {
                    clearInterval(pollingRef.current);
                }
            };
        }
    }, [isOpen, recipientId]);

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    const loadMessages = async () => {
        try {
            const lastMessageId = messages.length > 0 ? messages[messages.length - 1].id : null;
            const url = lastMessageId
                ? `/chat/api/${recipientId}/new?lastMessageId=${lastMessageId}`
                : `/chat/api/${recipientId}/new`;

            const res = await api.get(url);

            if (res.data.length > 0) {
                setMessages(prev => {
                    const existingIds = new Set(prev.map(m => m.id));
                    const newMessages = res.data.filter(m => !existingIds.has(m.id));
                    return [...prev, ...newMessages].sort((a, b) => a.id - b.id);
                });
            }
            setLoading(false);
        } catch (err) {
            console.error('Ошибка загрузки чата:', err);
        }
    };

    const handleSend = async (e) => {
        e.preventDefault();
        if (!newMessage.trim()) return;

        try {
            // 🔥 Используем 'content', как ждёт бэкенд
            await api.post(`/chat/api/${recipientId}`, new URLSearchParams({
                content: newMessage.trim()
            }));

            setNewMessage('');
            loadMessages();
        } catch (err) {
            console.error('Ошибка отправки:', err.response?.data || err.message);
            alert(err.response?.data?.error || 'Не удалось отправить сообщение');
        }
    };

    if (!isOpen) return null;

    const formatTime = (timeStr) => {
        return timeStr;
    };

    return (
        <div className="fixed inset-0 bg-black/40 backdrop-blur-sm z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
            <div className="bg-white w-full sm:w-[480px] h-[60vh] sm:h-[550px] rounded-t-2xl sm:rounded-2xl shadow-2xl flex flex-col animate-in slide-in-from-bottom duration-300">

                {/* Header */}
                <div className="bg-gradient-to-r from-blue-600 to-indigo-600 text-white p-4 rounded-t-2xl flex justify-between items-center">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-white/20 rounded-full flex items-center justify-center font-bold">
                            {recipientName?.charAt(0).toUpperCase() || '?'}
                        </div>
                        <div>
                            <h3 className="font-semibold text-lg">Чат с {recipientName}</h3>
                            <p className="text-xs text-blue-100">По сделке</p>
                        </div>
                    </div>
                    <button
                        onClick={onClose}
                        className="hover:bg-white/20 p-2 rounded-full transition"
                    >
                        <X size={20} />
                    </button>
                </div>

                {/* Messages Area */}
                <div className="flex-1 overflow-y-auto p-4 space-y-3 bg-gray-50">
                    {loading ? (
                        <div className="flex justify-center py-8">
                            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                        </div>
                    ) : messages.length === 0 ? (
                        <div className="text-center text-gray-400 py-12">
                            <MessageCircle size={48} className="mx-auto mb-3 opacity-50" />
                            <p className="text-sm">Пока нет сообщений</p>
                            <p className="text-xs mt-1">Напишите первое сообщение</p>
                        </div>
                    ) : (
                        messages.map((msg) => (
                            <div
                                key={msg.id}
                                className={`flex ${msg.isMine ? 'justify-end' : 'justify-start'}`}
                            >
                                <div className={`max-w-[75%] px-4 py-2.5 rounded-2xl text-sm ${
                                    msg.isMine
                                        ? 'bg-blue-600 text-white rounded-br-sm'
                                        : 'bg-white text-gray-800 border border-gray-200 rounded-bl-sm shadow-sm'
                                }`}>
                                    <p className="break-words">{msg.content}</p>
                                    <p className={`text-[10px] mt-1 text-right ${
                                        msg.isMine ? 'text-blue-100' : 'text-gray-400'
                                    }`}>
                                        {formatTime(msg.sentAt)}
                                    </p>
                                </div>
                            </div>
                        ))
                    )}
                    <div ref={messagesEndRef} />
                </div>

                {/* Input Area */}
                <form onSubmit={handleSend} className="p-4 border-t border-gray-200 bg-white rounded-b-2xl">
                    <div className="flex gap-2">
                        <input
                            type="text"
                            value={newMessage}
                            onChange={(e) => setNewMessage(e.target.value)}
                            placeholder="Введите сообщение..."
                            className="flex-1 px-4 py-2.5 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition"
                        />
                        <button
                            type="submit"
                            disabled={!newMessage.trim()}
                            className="bg-blue-600 hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed text-white p-3 rounded-xl transition flex items-center justify-center shadow-md"
                        >
                            <Send size={18} />
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}