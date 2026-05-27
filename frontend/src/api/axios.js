import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080',
    withCredentials: true, // 🚀 Передаёт JSESSIONID
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    // Глобальный Content-Type удалён. Axios сам подставит нужный для каждого метода
});

export default api;