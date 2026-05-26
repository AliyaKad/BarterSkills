import { useState, useEffect } from 'react';
import { Star, Edit3, Trash2 } from 'lucide-react';
import ReviewModal from './ReviewModal';
import api from '../api/axios';

export default function ReviewList({ userId, currentUserId, userName, onUpdate }) {
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingReview, setEditingReview] = useState(null);
    const [reviews, setReviews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [averageFromApi, setAverageFromApi] = useState(null);

    // Загрузка отзывов при монтировании
    useEffect(() => {
        loadReviews();
    }, [userId]);

    const loadReviews = async () => {
        try {
            setLoading(true);
            const res = await api.get(`/api/reviews/user/${userId}`);
            const list = (res.data.reviews || []).map(r => ({
                ...r,
                authorName: r.authorName || `${r.authorFirstName || ''} ${r.authorLastName || ''}`.trim(),
            }));
            setReviews(list);
            if (res.data.averageRating != null) {
                setAverageFromApi(res.data.averageRating);
            }
        } catch (err) {
            console.error('Ошибка загрузки отзывов:', err);
            setReviews([]);
        } finally {
            setLoading(false);
        }
    };

    const handleEdit = (review) => {
        setEditingReview(review);
        setIsModalOpen(true);
    };

    const handleDelete = async (reviewId) => {
        if (!confirm('Вы уверены, что хотите удалить этот отзыв?')) return;

        try {
            await api.delete(`/api/reviews/${reviewId}`);
            onUpdate && onUpdate();
            loadReviews();
        } catch (err) {
            alert('Не удалось удалить отзыв: ' + (err.response?.data?.error || err.message));
        }
    };

    const handleSuccess = () => {
        onUpdate && onUpdate();
        loadReviews();
        setEditingReview(null);
    };

    // Проверяем, оставил ли текущий пользователь уже отзыв
    const myReview = reviews.find(r => r.authorId === currentUserId);

    // Считаем средний рейтинг
    const averageRating = averageFromApi != null
        ? averageFromApi
        : (reviews.length > 0
            ? reviews.reduce((sum, r) => sum + r.rating, 0) / reviews.length
            : 0);

    return (
        <div className="space-y-4">
            {/* Заголовок с рейтингом */}
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                    <div className="flex items-center gap-1">
                        <Star size={24} className="fill-yellow-400 text-yellow-400" />
                        <span className="text-2xl font-bold text-gray-900">{averageRating.toFixed(1)}</span>
                    </div>
                    <div className="text-sm text-gray-500">
                        {reviews.length} {reviews.length === 1 ? 'отзыв' : reviews.length < 5 ? 'отзыва' : 'отзывов'}
                    </div>
                </div>

                {/* Кнопка оставить отзыв */}
                {currentUserId !== userId && !myReview && (
                    <button
                        onClick={() => setIsModalOpen(true)}
                        className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-xl font-medium transition flex items-center gap-2"
                    >
                        <Star size={18} />
                        Оставить отзыв
                    </button>
                )}
            </div>

            {/* Список отзывов */}
            {loading ? (
                <div className="flex justify-center py-8">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                </div>
            ) : reviews.length === 0 ? (
                <div className="text-center py-8 text-gray-500 bg-gray-50 rounded-xl">
                    <Star size={48} className="mx-auto mb-3 opacity-20" />
                    <p>Пока нет отзывов</p>
                    <p className="text-sm mt-1">Будьте первым, кто оставит отзыв!</p>
                </div>
            ) : (
                <div className="space-y-3">
                    {reviews.map((review) => {
                        const isMyReview = review.authorId === currentUserId;

                        return (
                            <div key={review.id} className="bg-white border border-gray-100 rounded-xl p-4 hover:shadow-md transition">
                                <div className="flex items-start justify-between gap-4">
                                    <div className="flex-1">
                                        <div className="flex items-center gap-2 mb-2">
                                            <div className="flex">
                                                {[1, 2, 3, 4, 5].map((star) => (
                                                    <Star
                                                        key={star}
                                                        size={14}
                                                        className={`${
                                                            star <= review.rating
                                                                ? 'fill-yellow-400 text-yellow-400'
                                                                : 'fill-gray-200 text-gray-200'
                                                        }`}
                                                    />
                                                ))}
                                            </div>
                                            <span className="text-sm font-medium text-gray-700">{review.authorName}</span>
                                            <span className="text-xs text-gray-400">
                        {new Date(review.createdAt).toLocaleDateString('ru-RU')}
                      </span>
                                        </div>

                                        {review.comment && (
                                            <p className="text-gray-600 text-sm leading-relaxed">{review.comment}</p>
                                        )}
                                    </div>

                                    {/* Кнопки редактирования/удаления */}
                                    {isMyReview && (
                                        <div className="flex gap-1 shrink-0">
                                            <button
                                                onClick={() => handleEdit(review)}
                                                className="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition"
                                                title="Редактировать"
                                            >
                                                <Edit3 size={16} />
                                            </button>
                                            <button
                                                onClick={() => handleDelete(review.id)}
                                                className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition"
                                                title="Удалить"
                                            >
                                                <Trash2 size={16} />
                                            </button>
                                        </div>
                                    )}
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}

            {/* Модальное окно */}
            <ReviewModal
                userId={userId}
                userName={userName || ''}
                existingReview={editingReview}
                isOpen={isModalOpen}
                onClose={() => {
                    setIsModalOpen(false);
                    setEditingReview(null);
                }}
                onSuccess={handleSuccess}
            />
        </div>
    );
}