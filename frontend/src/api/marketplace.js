import api from './axios';

const params = (obj) => new URLSearchParams(
    Object.entries(obj).filter(([, v]) => v !== undefined && v !== null && v !== '')
);

export const fetchOffers = (filters = {}) =>
    api.get('/api/offers', { params: filters });

export const fetchOffer = (id) => api.get(`/api/offers/${id}`);

export const createOffer = (data) =>
    api.post('/api/offers', params(data));

export const fetchRequests = (filters = {}) =>
    api.get('/api/requests', { params: filters });

export const fetchRequest = (id) => api.get(`/api/requests/${id}`);

export const createRequest = (data) =>
    api.post('/api/requests', params(data));

export const fetchDeals = () => api.get('/api/deals');

export const fetchDeal = (id) => api.get(`/api/deals/${id}`);

export const createDealFromOffer = (offerId, amount) =>
    api.post(`/api/deals/from-offer/${offerId}`, amount != null ? params({ amount }) : null);

export const createDealFromRequest = (requestId, amount) =>
    api.post(`/api/deals/from-request/${requestId}`, amount != null ? params({ amount }) : null);

export const acceptDeal = (id) => api.post(`/api/deals/${id}/accept`);

export const cancelDeal = (id) => api.post(`/api/deals/${id}/cancel`);

export const confirmDeal = (id) => api.post(`/api/deals/${id}/complete`);

export const disputeDeal = (id) => api.post(`/api/deals/${id}/dispute`);

export const fetchTransactions = () => api.get('/api/transactions');

export const fetchMe = () => api.get('/api/auth/me');

export const logout = () => api.post('/api/logout');
