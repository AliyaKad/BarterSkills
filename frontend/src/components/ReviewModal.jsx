import { useState, useEffect } from 'react';
import { X, Star } from 'lucide-react';
import api from '../api/axios';

export default function ReviewModal({ userId, userName, existingReview, isOpen, onClose, onSuccess }) {
    const [rating, setRating] = useState(existingReview?.rating || 0);
    const [hoveredRating, setHoveredRating] = useState(0);
    const [comment, setComment] = useState(existingReview?.comment || '');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        if (isOpen) {
            setRating(existingReview?.rating || 0);
            setComment(existingReview?.comment || '');
            setError('');
        }
    }, [isOpen, existingReview]);

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (rating < 1 || rating > 5) {
            setError('Поставьте оценку от 1 до 5');
            return;
        }

        try {
            setLoading(true);
            setError('');

            if (existingReview) {
                // Обновление
                await api.put(`/api/reviews/${existingReview.id}`, new URLSearchParams({
                    rating: rating.toString(),
                    comment: comment || ''
                }));
            } else {
                // Создание
                await api.post(`/api/reviews/user/${userId}`, new URLSearchParams({
                    rating: rating.toString(),
                    comment: comment || ''
                }));
            }

            onSuccess();
            onClose();
        } catch (err) {
            setError(err.response?.data?.error || 'Не удалось сохранить отзыв');
        } finally {
            setLoading(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black/40 backdrop-blur-sm z-50 flex items-center justify-center p-4" onClick={onClose}>
            <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg relative animate-in fade-in zoom-in-95 duration-200" onClick={e => e.stopPropagation()}>

                <div className="flex items-center justify-between p-6 border-b border-gray-100">
                    <h2 className="text-xl font-bold text-gray-800">
                        {existingReview ? 'Редактировать отзыв' : `Отзыв о ${userName}`}
                    </h2>
                    <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-full transition">
                        <X size={20} className="text-gray-500" />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="p-6 space-y-5">
                    {error && (
                        <div className="bg-red-50 border border-red-200 text-red-700 p-3 rounded-lg text-sm">
                            {error}
                        </div>
                    )}

                    {/* Рейтинг */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-3">Ваша оценка</label>
                        <div className="flex gap-2">
                            {[1, 2, 3, 4, 5].map((star) => (
                                <button
                                    key={star}
                                    type="button"
                                    onClick={() => setRating(star)}
                                    onMouseEnter={() => setHoveredRating(star)}
                                    onMouseLeave={() => setHoveredRating(0)}
                                    className="transition hover:scale-110"
                                >
                                    <Star
                                        size={36}
                                        className={`${
                                            star <= (hoveredRating || rating)
                                                ? 'fill-yellow-400 text-yellow-400'
                                                : 'fill-gray-200 text-gray-200'
                                        }`}
                                    />
                                </button>
                            ))}
                        </div>
                        <p className="text-sm text-gray-500 mt-2">
                            {rating === 1 && 'Ужасно'}
                            {rating === 2 && 'Плохо'}
                            {rating === 3 && 'Нормально'}
                            {rating === 4 && 'Хорошо'}
                            {rating === 5 && 'Отлично'}
                            {rating === 0 && 'Выберите оценку'}
                        </p>
                    </div>

                    {/* Комментарий */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Комментарий (необязательно)</label>
                        <textarea
                            value={comment}
                            onChange={(e) => setComment(e.target.value)}
                            placeholder="Расскажите о вашем опыте..."
                            rows={4}
                            className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none resize-none"
                        />
                    </div>

                    {/* Кнопки */}
                    <div className="flex gap-3 pt-2">
                        <button
                            type="submit"
                            disabled={loading || rating === 0}
                            className="flex-1 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed text-white font-semibold py-3 rounded-xl transition"
                        >
                            {loading ? 'Сохранение...' : existingReview ? 'Обновить отзыв' : 'Оставить отзыв'}
                        </button>
                        <button
                            type="button"
                            onClick={onClose}
                            className="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold py-3 rounded-xl transition"
                        >
                            Отмена
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}