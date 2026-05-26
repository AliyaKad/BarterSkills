import { useState } from 'react';
import api from '../api/axios';

export default function Login({ setUser }) {
    const [isRegister, setIsRegister] = useState(false);
    const [form, setForm] = useState({ email: '', password: '', firstName: '', lastName: '' });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            if (isRegister) {
                await api.post('/api/register', new URLSearchParams({
                    email: form.email, password: form.password,
                    firstName: form.firstName, lastName: form.lastName
                }));
            }

            const res = await api.post('/api/login', new URLSearchParams({
                email: form.email, password: form.password
            }));

            setUser(res.data);
        } catch (err) {
            let msg = 'Ошибка авторизации';
            if (err.response?.data?.error) msg = err.response.data.error;
            else if (err.response?.data?.message) msg = err.response.data.message;
            setError(msg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4">
            <div className="w-full max-w-md">
                <div className="bg-white rounded-2xl shadow-xl border border-gray-100 overflow-hidden">
                    <div className="bg-gradient-to-r from-blue-600 to-indigo-600 p-6 text-center">
                        <h2 className="text-2xl font-bold text-white">{isRegister ? 'Присоединяйся' : 'С возвращением!'}</h2>
                        <p className="text-blue-100 text-sm mt-1">{isRegister ? 'Начни обмениваться навыками' : 'Войди в систему'}</p>
                    </div>
                    <form onSubmit={handleSubmit} className="p-6 space-y-5">
                        {error && <div className="bg-red-50 border border-red-200 text-red-700 p-3 rounded-lg text-sm">{error}</div>}
                        {isRegister && (
                            <div className="grid grid-cols-2 gap-4">
                                <input name="firstName" placeholder="Имя *" className="input" required onChange={handleChange} />
                                <input name="lastName" placeholder="Фамилия *" className="input" required onChange={handleChange} />
                            </div>
                        )}
                        <input name="email" type="email" placeholder="Email *" className="input" required onChange={handleChange} />
                        <input name="password" type="password" placeholder="Пароль *" className="input" required onChange={handleChange} />
                        <button type="submit" disabled={loading} className="btn-primary w-full py-3 text-base">
                            {loading ? 'Загрузка...' : (isRegister ? 'Создать аккаунт' : 'Войти')}
                        </button>
                    </form>
                    <div className="bg-gray-50 p-4 text-center border-t border-gray-100">
                        <button type="button" onClick={() => { setIsRegister(!isRegister); setError(''); }} className="text-blue-600 hover:text-blue-700 text-sm font-medium hover:underline">
                            {isRegister ? 'Уже есть аккаунт? Войти' : 'Нет аккаунта? Зарегистрироваться'}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}